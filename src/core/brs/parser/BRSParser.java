package core.brs.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicSliderUI.ActionScroller;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Connectivity;
import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;

public class BRSParser {

	private Tokenizer brsTokenizer;
	private BigraphWrapper bigWrapper;

	private static final int ACTION_NAME_INDEX = 0;
	private static final int ACTION_PRE_INDEX = 1;
	private static final int ACTION_POST_INDEX = 2;

	private int controlNum = 1;

	public BRSParser() {
		// bigWrapper = new BigraphWrapper();
		controlNum = 1;
	}

	/**
	 * Parses a given action string written using BigraphER syntax.
	 * 
	 * @param action
	 *            String of the action
	 * @return ActionWrapper object which contains information about the action
	 *         (e.g., name, pre, and post)
	 */
	public ActionWrapper parseBigraphERAction(String action) {

		ActionWrapper actionWrapper = new ActionWrapper();

		BigraphWrapper preWrapper = null;
		BigraphWrapper postWrapper = null;

		List<String> actionComps = preProcessAction(action);

		if (actionComps == null) {
			return null;
		}

		String actionName = actionComps.get(ACTION_NAME_INDEX);
		String pre = actionComps.get(ACTION_PRE_INDEX);
		String post = actionComps.get(ACTION_POST_INDEX);

		if (pre != null) {
			preWrapper = parseBigraph(pre);
		}

		if (post != null) {
			postWrapper = parseBigraph(post);
		}

		actionWrapper.setActionName(actionName);
		actionWrapper.setPrecondition(preWrapper);
		actionWrapper.setPostcondition(postWrapper);

		return actionWrapper;

	}

	protected List<String> preProcessAction(String action) {

		// returns in the list
		// 0: action name
		// 1: pre
		// 2: post

		if (action == null || action.isEmpty()) {
			return null;
		}

		List<String> components = new LinkedList<String>();
		// parse a BigraphER action with format "react action_name = redex ->
		// reactum;"
		String[] parts = action.split("=");

		String actionName = null;
		String pre = null;
		String post = null;

		// parts[0] is the action name
		if (parts.length > 1) {

			// ===get action name
			actionName = parts[0];

			if (actionName.contains("react")) {
				actionName = actionName.replace("react", "");
			}

			actionName = actionName.trim();

			// ===get conditions
			String[] conditions = null;

			if (parts[1].contains("->")) {
				conditions = parts[1].split("->");
			} else if (parts[1].contains("-->")) {
				conditions = parts[1].split("-->");
			}

			if (conditions != null && conditions.length > 1) {
				pre = conditions[0];
				post = conditions[1];

				// remove any ; and [] from post
				if (post != null) {
					if (post.contains("[")) {
						post = post.substring(0, post.lastIndexOf("["));
					}

					if (post.contains(";")) {
						post = post.replace(";", "");
					}

				}

			}
		}

		components.add(ACTION_NAME_INDEX, actionName);
		components.add(ACTION_PRE_INDEX, pre);
		components.add(ACTION_POST_INDEX, post);

		return components;

	}

	/**
	 * Parses the given condition in BRS format to identify entities and
	 * connectivity then creates a new condition based on that
	 * 
	 * @param BRSexp
	 *            Bigraph expressed as a BigraphExpression object
	 * @return BigraphWrapper object that contains various information about the
	 *         given BRS
	 */
	public BigraphWrapper parseBigraph(BigraphExpression BRSexp) {

		BigraphWrapper bigWrpr = new BigraphWrapper();
		bigWrapper = bigWrpr;

		// clear data if any
		clear();

		bigWrapper.setBigraphExpression(BRSexp);

		// int numOfRoots = 0;

		for (Entity ent : BRSexp.getEntity()) {

			// ===add control
			String entityName = addControl(ent);

			// ===add root
			addEntityRoot(entityName);

			// ===add site
			if (ent.isHasSite()) {
				addSite(entityName);
			}

			// ===add connectivity
			for (Connectivity con : ent.getConnectivity()) {
				updateConnectivity(con.getName(), entityName);
			}

			addChildren(entityName, ent.getEntity());
		}

		return bigWrapper;

	}

	protected void addChildren(String parentEntityName, EList<Entity> entities) {

		// BigraphNode node;

		for (Entity entity : entities) {

			// ===add control
			String entityName = addControl(entity);

			// ===add parent
			updateEntityContainer(entityName, parentEntityName);

			// ===add site
			if (entity.isHasSite()) {
				addSite(entityName);
			}

			// add connectivity
			for (Connectivity con : entity.getConnectivity()) {
				updateConnectivity(con.getName(), entityName);
			}

			addChildren(entityName, entity.getEntity());
		}
	}

	/**
	 * Parses the given condition in BRS format to identify entities and
	 * connectivity then creates a new condition based on that
	 * 
	 * @param BRScondition
	 *            as a string
	 * @return BigraphWrapper object that contains various information about the
	 *         given BRS
	 */
	public BigraphWrapper parseBigraph(String BigrapherState) {

		if (brsTokenizer == null) {
			createBRSTokenizer();
		}

		// brsExpression = BRScondition;
		BigraphWrapper bigWrpr = new BigraphWrapper();
		bigWrapper = bigWrpr;

		// clear data if any
		clear();

		bigWrapper.setBigraphERString(BigrapherState);

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		int rootNum = 0;

		LinkedList<Entity> rootEntities = new LinkedList<Entity>();
		LinkedList<Entity> allEntities = new LinkedList<Entity>();
		LinkedList<Entity> containers = new LinkedList<Entity>();
		LinkedList<String> closedConnectivities = new LinkedList<String>();

		// boolean isBracketContainment = false;
		boolean isContainment = false;
		boolean isFirstEntity = true;
		boolean isBigraphJuxta = false;
		boolean isEntityJuxta = false;
		boolean hasSite = false;
		boolean isConnectivity = false;
		boolean isClosedConnectivity = false;

		// ===tokenize
		brsTokenizer.tokenize(BigrapherState);
		for (Tokenizer.Token tok : brsTokenizer.getTokens()) {
			switch (tok.token) {

			case BigraphERTokens.CONTAINMENT: // .

				// add to the container the last entity in all entities
				containers.addFirst(allEntities.getLast());
				isContainment = true;

				break;

			case BigraphERTokens.OPEN_BRACKET: // (

				if (!containers.isEmpty()) {
					// isBracketContainment = true;
					isContainment = false;
				}

				break;

			case BigraphERTokens.CLOSED_BRACKET: // )

				// remove a container from the list of containers
				if (!containers.isEmpty()) {

					// check if it has site
					if (!hasSite) {
						containers.getFirst().setSite(null);
						containers.getFirst().setHasSite(false);
					} else { // reset
						hasSite = false;
					}

					containers.pop();
				}

				if (containers.isEmpty()) {
					// isBracketContainment = false;
				}

				break;

			case BigraphERTokens.ENTITY_JUXTAPOSITION: // |
				// next element should be contained in the same entity as the
				// previous
				isEntityJuxta = true;

				break;
			case BigraphERTokens.BIGRAPH_JUXTAPOSITION: // ||

				// next element should be a root element
				isBigraphJuxta = true;

				break;

			case BigraphERTokens.SITE:// id

				// by default a site is created with each entity

				// if the token is site then if it is containment add site to
				// last add to all entities
				// done by default

				// else if container is not empty then add to the head of the
				// container list
				// done by default

				// just look for cases where site needs to be removed

				// maybe you should cover when site is in bigraph juxtaposition
				if (isBigraphJuxta) {
					// get last added root
					// to be done
					incrementRootSites();
				} else if (isEntityJuxta) {

					// get current container
					Entity ent = containers.getFirst();
					String entityName = bigWrapper.getControlMap().get(ent);

					addSite(entityName);

				} else {
					// add site to last added entity
					Entity ent = allEntities.getLast();
					//
					String entityName = bigWrapper.getControlMap().get(ent);

					// System.out.println("adding site to " + entityName);

					addSite(entityName);

					// need to remove a container
					containers.removeFirst();
				}

				// hasSite = true;

				break;

			case BigraphERTokens.OPEN_BRACKET_CONNECTIVITY:// {

				isConnectivity = true;
				// name or words are recognised as connectivity for last added
				// entity in all entities until closing the bracket
				break;

			case BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY: // }

				// connectivity names ended
				isConnectivity = false;
				break;

			case BigraphERTokens.CLOSED_CONNECTIVITY: // e.g., /con

				// next token should be a name that relates to a connectivity
				// but it is closed
				isClosedConnectivity = true;

				break;
			case BigraphERTokens.COMMA: // ,
				// defines different names
				// nothing to be done
				break;

			case BigraphERTokens.WORD: // entity or connectivity

				// if closed connectivity token appeared, then the word is a
				// connectivity name that
				if (isClosedConnectivity) {
					closedConnectivities.add(tok.sequence);
					isClosedConnectivity = false;
				}
				// if it is connectivity
				else if (isConnectivity) {
					// create connectivity for last added entity in all entities
					Entity lastAdded = allEntities.getLast();

					Connectivity tmpCon = instance.createConnectivity();
					tmpCon.setName(tok.sequence);

					lastAdded.getConnectivity().add(tmpCon);

					// ===update connectivity value
					updateConnectivity(tok.sequence, bigWrapper.getControlMap().get(lastAdded));

					// close connectivity
					if (!closedConnectivities.isEmpty()) {
						if (tok.sequence.equalsIgnoreCase(closedConnectivities.getLast())) {
							tmpCon.setIsClosed(true);
							isClosedConnectivity = false;
							closedConnectivities.removeLast(); // remove last
						}
					}

					// if it is entity
				} else {
					// create an entity
					Entity tmp = instance.createEntity();
					tmp.setName(tok.sequence);
					allEntities.add(tmp);

					// ===add to contro map
					String entityName = addControl(tmp);

					// check if containers are not empty, if so, then get the
					// head (first element as the current container)
					if (!containers.isEmpty()) {

						Entity currentContainer = containers.getFirst();

						currentContainer.getEntity().add(tmp);

						// ===update entities and containers
						updateEntityContainer(entityName, bigWrapper.getControlMap().get(currentContainer));

						// System.out.println("entity " + tok.sequence + " is
						// contained in " + currentContainer.getName());

						// if containment is not within brackets ()
						if (isContainment) {
							// System.out.println("removing container: " +
							// currentContainer.getName());
							// it has no site then! so remove it
							currentContainer.setSite(null);
							currentContainer.setHasSite(false);
							containers.removeFirst();
							isContainment = false;
						}

						// consume entity juxta if it is true
						if (isEntityJuxta) {
							isEntityJuxta = false;
						}

					} else if (isBigraphJuxta) { // if entity after ||
						rootEntities.add(tmp);

						// ===update entity roots
						addEntityRoot(entityName);

						isBigraphJuxta = false;

					} else if (isEntityJuxta) { // if entity after |
						// then the last added entity should be remove from the
						// root and a new entity created that combines both
						Entity lastRoot = rootEntities.removeLast();
						Entity newRoot = instance.createEntity();

						newRoot.setName("Root-" + rootNum);
						newRoot.getEntity().add(lastRoot);
						newRoot.getEntity().add(tmp);

						// ===update entities and containers
						addExtraRoot(newRoot.getName());

						updateEntityContainer(entityName, newRoot.getName());

						if (bigWrapper.getControlMap().containsKey(lastRoot)) {
							removeRoot(bigWrapper.getControlMap().get(lastRoot));
							updateEntityContainer(bigWrapper.getControlMap().get(lastRoot), newRoot.getName());
						}

						// for now root is not added to all entities
						rootEntities.add(newRoot);

						isEntityJuxta = false;

						rootNum++;
					}

					else { // if entity is not contained anywhere

						// if entity is the first one
						if (isFirstEntity) {
							rootEntities.add(tmp);

							// ===update entity roots
							addEntityRoot(entityName);

							isFirstEntity = false;
						}

					}
				}
				break;
			default:
				// nothing
				// System.out.println("ignoring " + tok.sequence);

			}
		}

		// printAll();
		// ===create bigraph expression
		BigraphExpression newBRS = instance.createBigraphExpression();

		newBRS.getEntity().addAll(rootEntities);

		bigWrapper.setBigraphExpression(newBRS);

		return bigWrapper;
	}

	protected void addSite(String entityName) {

		if (entityName == null) {
			return;
		}

		Map<String, Boolean> sites = bigWrapper.getEntitySiteMap();

		sites.put(entityName, true);
	}

	protected void incrementRootSites() {

		bigWrapper.incrementRootSites();
	}

	protected String addControl(Entity entity) {

		if (entity == null) {
			return null;
		}

		String uniqName = "";

		uniqName = entity.getName() + controlNum;

		controlNum++;

		bigWrapper.getControlMap().put(entity, uniqName);

		return uniqName;
	}

	protected void addEntityRoot(String entityRoot) {

		updateEntityContainer(entityRoot, null);

		// add to root
		if (!bigWrapper.getRoots().contains(entityRoot)) {
			bigWrapper.getRoots().add(entityRoot);
		}

	}

	protected void addExtraRoot(String root) {

		// add to root
		if (!bigWrapper.getRoots().contains(root)) {
			bigWrapper.getRoots().add(root);
		}

	}

	protected void removeRoot(String root) {

		if (bigWrapper.getRoots() == null) {
			return;
		}

		bigWrapper.getRoots().remove(root);

	}

	protected void updateConnectivity(String conName, String entityName) {

		// add connection to the list of connections
		if (!bigWrapper.getConnections().contains(conName)) {
			bigWrapper.getConnections().add(conName);
		}

		// add to map of connections
		if (bigWrapper.getConnectivityMap().containsKey(conName)) {
			List<String> cons = bigWrapper.getConnectivityMap().get(conName);
			if (!cons.contains(entityName)) {
				cons.add(entityName);
			}
		} else {
			// new connection
			List<String> cons = new LinkedList<String>();
			cons.add(entityName);
			bigWrapper.getConnectivityMap().put(conName, cons);
		}

		// add to entity connection map
		if (bigWrapper.getEntityConnectivityMap().containsKey(entityName)) {
			List<String> cons = bigWrapper.getEntityConnectivityMap().get(entityName);
			if (!cons.contains(conName)) {
				cons.add(conName);
			}
		} else {
			// new connection for the entity
			List<String> cons = new LinkedList<String>();
			cons.add(conName);
			bigWrapper.getEntityConnectivityMap().put(entityName, cons);
		}
	}

	protected void updateEntityContainer(String entityName, String entityContainer) {

		// add to all entities
		if (!bigWrapper.getEntities().contains(entityName)) {
			bigWrapper.getEntities().add(entityName);
		}

		bigWrapper.getContainerEntitiesMap().put(entityName, entityContainer);

		// add to the control map
		// controlMap.put(uniqName, entityName);

		// update parent entity
		if (entityContainer == null) {
			return;
		}

		// update contained entities for parent
		if (bigWrapper.getContainedEntitiesMap().containsKey(entityContainer)) {
			List<String> children = bigWrapper.getContainedEntitiesMap().get(entityContainer);
			if (!children.contains(entityName)) {
				children.add(entityName);
			}
		} else {
			// new entity
			List<String> children = new LinkedList<String>();
			children.add(entityName);
			bigWrapper.getContainedEntitiesMap().put(entityContainer, children);
		}

	}

	protected void createBRSTokenizer() {

		brsTokenizer = new Tokenizer();

		// order has importance

		brsTokenizer.add(BigraphERTokens.TOKEN_CONTAINMENT, BigraphERTokens.CONTAINMENT);
		brsTokenizer.add(BigraphERTokens.TOKEN_COMPOSITION, BigraphERTokens.COMPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_BIGRAPH_JUXTAPOSITION, BigraphERTokens.BIGRAPH_JUXTAPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_ENTITY_JUXTAPOSITION, BigraphERTokens.ENTITY_JUXTAPOSITION);
		brsTokenizer.add(BigraphERTokens.TOKEN_SITE, BigraphERTokens.SITE);
		brsTokenizer.add(BigraphERTokens.TOKEN_OPEN_BRACKET, BigraphERTokens.OPEN_BRACKET);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_BRACKET, BigraphERTokens.CLOSED_BRACKET);
		brsTokenizer.add(BigraphERTokens.TOKEN_OPEN_BRACKET_CONNECTIVITY, BigraphERTokens.OPEN_BRACKET_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_BRACKET_CONNECTIVITY,
				BigraphERTokens.CLOSED_BRACKET_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_CLOSED_CONNECTIVITY, BigraphERTokens.CLOSED_CONNECTIVITY);
		brsTokenizer.add(BigraphERTokens.TOKEN_COMMA, BigraphERTokens.COMMA);
		brsTokenizer.add(BigraphERTokens.TOKEN_SMALL_SPACE, BigraphERTokens.SMALL_SPACE);
		brsTokenizer.add(BigraphERTokens.TOKEN_WORD, BigraphERTokens.WORD);

	}

	public void clear() {

		if (bigWrapper != null) {
			bigWrapper.clear();
		}

		controlNum = 1;
	}

	// public String getBrsExpression() {
	// return brsExpression;
	// }

	// public void modifyBrsExpression() {
	//
	// //replace controls with equivlent entity names used
	//// List<Token> tokens = brsTokenizer.getTokens();
	// int fromIndex = 0;
	//
	//// String entityName = entities.get(index);
	//// S
	// modifiedBrsExpression = brsExpression;
	// StringBuffer temp = new StringBuffer(brsExpression);
	// int start = 0;
	// int end = 0;
	//
	// for(String entityName : entities) {
	// String ctrl = getControl(entityName);
	// start = temp.indexOf(ctrl, fromIndex);
	// end = start+ctrl.length();
	// fromIndex = end;
	//// System.out.println("entity: " + entityName + " ctrl: "+ctrl);
	// temp.delete(start, end);
	// temp.insert(start, entityName);
	//
	//// modifiedBrsExpression = modifiedBrsExpression.replace
	// }
	//
	// modifiedBrsExpression = temp.toString();
	//// for(Token t : tokens) {
	////
	//// if(t)
	//// }
	// }

	// protected String getControl(String entityName) {
	//
	// for(Entry<Entity, String> entry : controlMap.entrySet()) {
	// if(entry.getValue().equals(entityName)){
	// return entry.getKey().getName();
	// }
	// }
	// return null;
	// }

}
