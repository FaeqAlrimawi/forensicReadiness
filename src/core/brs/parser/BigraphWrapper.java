package core.brs.parser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.brs.parser.utilities.BigraphNode;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Entity;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

/**
 * This class is used to hold information about a Bigraph. A bigraph can be a
 * condition of an activity. Information: Number of entities, modified entity
 * names, Controls, map between entities and controls, Connections, Connections
 * map (i.e. connections between entities and entities connections), roots,
 * Bigraph representation,
 * 
 * @author Faeq
 *
 */
public class BigraphWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5880531546378390072L;

	// determines if this Bigraph represents a condition (true) or a state
	// (false)
	private boolean isCondition;

	// given bigraph expxression in BigraphER syntax
	private String bigraphERString;

	// modified bigraph expression. It replaces controls with variables defining
	// the position of each control starting from 1
	// e.g., Room.Actor => Room1.Actor2
	private String modifiedBigraphERString;

	// generated BigraphExpression object
	private BigraphExpression bigraphExpression;

	// Bigraph object representation of this
	private Bigraph bigraphObj;

	// original state that this bigraphWrapper would refer to.
	// used when a trace is applied to this
	private int OriginalState;

	// incident pattern condition it matches to
	private String incidentPatternCondition;

	// used to set the number of automatically created outernames for the
	// signature of a Bigraph Object
	protected int maxOuterNameNumber = 10;

	private static final String BIGRAPH_ROOT_CNTRL = "BigraphRoot";

	// all entities (with numbering as its order in the string), i.e. modified
	private List<String> entities;

	// connections as is (no modifications to the con name)
	private List<String> connections;

	// roots whether added or not
	private List<String> roots;

	// key is the entity (name holds the control), value is the entity name
	// (modified e.g., Room => Room1)
	private Map<Entity, String> controlMap;

	// private int controlNum = 1;

	// key is entity name, value is a list of all contained entities
	private Map<String, List<String>> containedEntitiesMap;

	// key is entity name, value is parent entity (if empty then it is a root)
	private Map<String, String> containerEntitiesMap;

	// key is connection name, value is a list of TWO entities that are the ends
	private Map<String, List<String>> connectivityMap;

	// key is entity name, value is a list all connection names
	private Map<String, List<String>> entityConnectivityMap;

	// key is entity name, value is site existence (true it exists, false it
	// does not)
	private Map<String, Boolean> entitySiteMap;

	// number of root ids (sites) e.g., Actor || id || id
	int numberOfRootSites = 0;

	int numOfRoots = 0;

	// signature
	Signature signature;

	public BigraphWrapper() {
		entities = new LinkedList<String>();
		connections = new LinkedList<String>();
		controlMap = new HashMap<Entity, String>();
		containedEntitiesMap = new HashMap<String, List<String>>();
		containerEntitiesMap = new HashMap<String, String>();
		connectivityMap = new HashMap<String, List<String>>();
		entityConnectivityMap = new HashMap<String, List<String>>();
		roots = new LinkedList<String>();
		entitySiteMap = new HashMap<String, Boolean>();

	}

	public String getIncidentPatternCondition() {

		return incidentPatternCondition;
	}

	public void setIncidentPatternCondition(String conditionName) {

		incidentPatternCondition = conditionName;
	}

	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature sig) {
		signature = sig;
	}

	public int getOriginalState() {
		return OriginalState;
	}

	public void setOriginalState(int originalState) {
		OriginalState = originalState;
	}

	public boolean isCondition() {
		return isCondition;
	}

	public void setCondition(boolean isCondition) {
		this.isCondition = isCondition;
	}

	public BigraphExpression getBigraphExpression() {
		return bigraphExpression;
	}

	public void setBigraphExpression(BigraphExpression bigraphExpression) {
		this.bigraphExpression = bigraphExpression;
	}

	public String getBigraphERString() {
		return bigraphERString;
	}

	public void setBigraphERString(String bigraphERString) {
		this.bigraphERString = bigraphERString;
	}

	public String getModifiedBigraphERString() {
		return modifiedBigraphERString;
	}

	public void setModifiedBigraphERString(String modifiedBigraphERString) {
		this.modifiedBigraphERString = modifiedBigraphERString;
	}

	public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public void addEntity(String entityName) {

		if (entities == null) {
			return;
		}

		entities.add(entityName);
	}

	public List<String> getConnections() {
		return connections;
	}

	public void setConnections(List<String> connections) {
		this.connections = connections;
	}

	public void addConnection(String conName) {

		if (connections == null) {
			return;
		}

		connections.add(conName);
	}

	public List<String> getRoots() {
		return roots;
	}

	public void setRoots(List<String> roots) {
		this.roots = roots;
	}

	public void addRoot(String root) {

		if (roots != null) {
			roots.add(root);
		}
	}

	public Map<Entity, String> getControlMap() {
		return controlMap;
	}

	public void setControlMap(Map<Entity, String> controlMap) {
		this.controlMap = controlMap;
	}

	/**
	 * Adds the control to the control map and also the entity name to the
	 * entityName list
	 * 
	 * @param entity
	 *            Entity object
	 * @param entityName
	 *            String representing the entity name (modified, i.e. not
	 *            control)
	 */
	public void addControl(Entity entity, String entityName) {

		if (controlMap != null) {
			controlMap.put(entity, entityName);
			entities.add(entityName);
		}
	}

	public boolean hasControl(String control) {

		for (Entity ent : controlMap.keySet()) {
			if (ent.getName().equals(control)) {
				return true;
			}
		}

		return false;
	}

	public Map<String, List<String>> getContainedEntitiesMap() {
		return containedEntitiesMap;
	}

	public void setContainedEntitiesMap(Map<String, List<String>> containedEntitiesMap) {
		this.containedEntitiesMap = containedEntitiesMap;
	}

	public Map<String, String> getContainerEntitiesMap() {
		return containerEntitiesMap;
	}

	public void setContainerEntitiesMap(Map<String, String> containerEntitiesMap) {
		this.containerEntitiesMap = containerEntitiesMap;
	}

	public void addContainedEntity(String entityParent, String entityContained) {

		if (containedEntitiesMap == null) {
			return;
		}

		if (containedEntitiesMap.containsKey(entityParent)) {
			List<String> containedEntities = containedEntitiesMap.get(entityParent);
			containedEntities.add(entityContained);
		} else {
			List<String> containedEntities = new LinkedList<String>();
			containedEntities.add(entityContained);
			containedEntitiesMap.put(entityParent, containedEntities);
		}

		if (containedEntitiesMap.containsKey(entityParent)) {
			// add a container relation
			containerEntitiesMap.put(entityContained, entityParent);
		}
	}

	public Map<String, List<String>> getConnectivityMap() {
		return connectivityMap;
	}

	public void setConnectivityMap(Map<String, List<String>> connectivityMap) {
		this.connectivityMap = connectivityMap;
	}

	public Map<String, List<String>> getEntityConnectivityMap() {
		return entityConnectivityMap;
	}

	public void setEntityConnectivityMap(Map<String, List<String>> entityConnectivityMap) {
		this.entityConnectivityMap = entityConnectivityMap;
	}

	public void addConnection(String conName, String end1, String end2) {

		List<String> connectionEnds = new LinkedList<String>();

		connectionEnds.add(end1);
		connectionEnds.add(end2);

		// all connection names
		connections.add(conName);

		// connection -> end1,end2
		connectivityMap.put(conName, connectionEnds);

		// update entity connectivity map
		// add connectivity to end1
		if (entityConnectivityMap.containsKey(end1)) {
			entityConnectivityMap.get(end1).add(conName);
		} else {
			List<String> cons = new LinkedList<String>();
			cons.add(conName);
			entityConnectivityMap.put(end1, cons);
		}

		// add connectivity to end1
		if (entityConnectivityMap.containsKey(end2)) {
			entityConnectivityMap.get(end2).add(conName);
		} else {
			List<String> cons = new LinkedList<String>();
			cons.add(conName);
			entityConnectivityMap.put(end2, cons);
		}
	}

	public Map<String, Boolean> getEntitySiteMap() {
		return entitySiteMap;
	}

	public void setEntitySiteMap(Map<String, Boolean> entitySiteMap) {
		this.entitySiteMap = entitySiteMap;
	}

	public void addSite(String entityName, boolean hasSite) {

		entitySiteMap.put(entityName, hasSite);
	}
	

	public int getNumberOfRootSites() {
		return numberOfRootSites;
	}

	public void setNumberOfRootSites(int numberOfRootSites) {
		this.numberOfRootSites = numberOfRootSites;
	}

	public void incrementRootSites() {

		numberOfRootSites++;
	}

	/**
	 * returns a Bigraph representation of this object. If not created it will
	 * create it and then return it
	 * 
	 * @param isGrounded
	 *            If true then the representation contains no sites. If false it
	 *            means it can contain sites
	 * @param sig
	 *            Signature
	 * @return Bigraph object
	 */
	public Bigraph getBigraphObject(boolean isGrounded, Signature sig) {

		if (bigraphObj == null) {
			if (bigraphExpression != null) {
				// bigraphObj = bigraphExpression.createBigraph(false);
				bigraphObj = createBigraph(isGrounded, sig);
			}
		}

		return bigraphObj;
	}

	/**
	 * returns a Bigraph representation of this object. If not created it will
	 * create it and then return it. By default the Bigraph is not grounded and
	 * the default signature is used
	 * 
	 * @return Bigraph object
	 */
	public Bigraph getBigraphObject() {

		if (bigraphObj == null) {
			if (bigraphExpression != null) {
				// bigraphObj = bigraphExpression.createBigraph(false);
				bigraphObj = createBigraph(false, signature);
			}
		}

		return bigraphObj;
	}

	public void setBigraphObject(Bigraph bigraphObject) {

		bigraphObj = bigraphObject;
	}

	public Bigraph createBigraph(boolean isGround, Signature sig) {

		signature = sig;

		BigraphNode node;
		Map<String, BigraphNode> nodes = new HashMap<String, BigraphNode>();
		// SignatureBuilder sigBuilder = new SignatureBuilder();

		// System.out.println(entities);
		// System.out.println(roots);
		// System.out.println(sig);
		// System.out.println(containedEntitiesMap);
		for (Entity ent : controlMap.keySet()) {

			String entityName = controlMap.get(ent);

			// just roots are dealt with
			if ((!roots.contains(entityName) && entities.contains(entityName))) {
				continue;
			}

			node = new BigraphNode();

			node.setId(entityName);

			/*
			 * // add site node.setSite(ent.getSite() != null ? true : false);
			 */

			// add parent
			node.setParentRoot(numOfRoots);
			numOfRoots++;

			// add control (currently same as the name of the entity)
			// System.out.println("-root: " + entityName);
			if (controlMap.containsKey(ent)) {
				node.setControl(ent.getName());
				// System.out.println("\tControl: " + ent.getName());
			} else {
				// if the root is an extra one, then create BigraphRoot as an
				// extra entity to represent it
				node.setControl(BIGRAPH_ROOT_CNTRL);
			}

			// add connectivity (outernames)
			if (entityConnectivityMap.get(entityName) != null) {
				for (String con : entityConnectivityMap.get(entityName)) {
					node.addOuterName(con, false); // default of connection to
													// be not-closed
				}
			}
			nodes.put(node.getId(), node);

			// create a bigraph signature out of each entity and max arity
			// number
			// sigBuilder.add(ent.getName(), true, maxOuterNameNumber);

			addChildrenNew(node, containedEntitiesMap.get(entityName), nodes, isGround);

			if (!isGround) {
				// add site
				// by defualt it has site
				node.setSite(true);
			}
		}

		// check roots
		for (String root : roots) {
			if (!entities.contains(root)) {
				// if root, then add all contained entities with root parent
				addChildrenNew(null, containedEntitiesMap.get(root), nodes, isGround);
				numOfRoots++;
			}
		}

		// System.out.println("-num of roots: " + numOfRoots);
		// System.out.println("-actual num of roots: " + roots.size());
		Signature signature;

		if (sig == null) {
			signature = createBigraphSignature();
		} else {
			signature = sig;
		}

		return BuildBigraph(nodes, signature);

	}

	public Signature createBigraphSignature() {

		Signature signature;

		setMaxNumberOfuterNames();

		SignatureBuilder sigBuilder = new SignatureBuilder();
		// EList<IncidentEntity> entities = new BasicEList<IncidentEntity>();
		//
		// entities.addAll(getAsset());
		// entities.addAll(getResource());
		// entities.addAll(getActor());

		for (Entity ent : controlMap.keySet()) {
			// create a bigraph signature out of each entity and max arity
			// number
			System.out.println(ent);
			sigBuilder.add(ent.getName(), true, maxOuterNameNumber);

			// addChildren(node, ent.getEntity(), nodes, sigBuilder);
		}

		// add the roots if they don't exist in the map. They are added with
		// default control as "BigraphRoot"
		// for(String root: roots) {
		// if(!entities.contains(root)) {
		sigBuilder.add(BIGRAPH_ROOT_CNTRL, true, maxOuterNameNumber);
		// }
		// }
		// entities = null;
		signature = sigBuilder.makeSignature();

		return signature;

	}

	protected void setMaxNumberOfuterNames() {

		int maxNum = 0;
		for (Entity ent : controlMap.keySet()) {
			if (ent.getConnectivity().size() > maxNum) {
				maxNum = ent.getConnectivity().size();
			}
		}

		if (maxNum == 0) {
			maxOuterNameNumber = 5; // default
		} else {
			maxOuterNameNumber = maxNum;
		}
	}

	protected Bigraph BuildBigraph(Map<String, BigraphNode> nodes, Signature signature) {

		LinkedList<BigraphNode.OuterName> outerNames = new LinkedList<BigraphNode.OuterName>();
		LinkedList<BigraphNode.InnerName> innerNames = new LinkedList<BigraphNode.InnerName>();
		HashMap<String, it.uniud.mads.jlibbig.core.std.OuterName> libBigOuterNames = new HashMap<String, it.uniud.mads.jlibbig.core.std.OuterName>();
		HashMap<String, it.uniud.mads.jlibbig.core.std.InnerName> libBigInnerNames = new HashMap<String, it.uniud.mads.jlibbig.core.std.InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();

		// create bigraph
		BigraphBuilder biBuilder = new BigraphBuilder(signature);

		// create roots for the bigraph
		for (int i = 0; i < getRoots().size(); i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}

		int difference;
		int arity;
		int newSize = 0;
		LinkedList<BigraphNode.OuterName> names;

		///// To avoid the issue of matching using outernames, I don't create
		///// outernames
		//// but if there are outernames for a node then I add a special node
		///// called "connected" which donates that this node is connected to
		///// the installation bus
		///// this solution should be temporary and we should find a way to use
		///// the outernames (links) to match connectivity based on it

		for (BigraphNode n : nodes.values()) {

			// create bigraph outernames
			arity = maxOuterNameNumber;
			names = n.getOuterNamesObjects();
			difference = names.size() - arity;
			// if the node has more outernames than that in the signature and
			// knowledge is partial, then only add outernames equal to the arity
			// other option is to leave it, then the other extra outernames will
			// be defined as empty i.e. XX:o<-{}
			if (difference > 0 && n.isKnowledgePartial()) {
				newSize = arity;
			} else {
				newSize = names.size();
			}
			for (int i = 0; i < newSize; i++) {
				if (!outerNames.contains(names.get(i))) {
					libBigOuterNames.put(names.get(i).getName(), biBuilder.addOuterName(names.get(i).getName()));
					// biBuilder.closeOuterName(names.get(i).getName());
					outerNames.add(names.get(i));
				}

			}

			// create bigraph inner names
			for (BigraphNode.InnerName in : n.getInnerNamesObjects()) {
				if (!innerNames.contains(in)) {
					libBigInnerNames.put(in.getName(), biBuilder.addInnerName(in.getName()));
					innerNames.add(in);
				}
			}
		}

		// initial creation of bigraph nodes
		for (BigraphNode nd : nodes.values()) {
			if (libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);
		}

		// close outernames after creating nodes of the Bigraph
		// this turns them into edges (or links) in the Bigraph object
		for (BigraphNode.OuterName out : outerNames) {
			if (out.isClosed()) {
				biBuilder.closeOuterName(out.getName());
			}
		}

		/*
		 * LinkedList<String> visited = new LinkedList<String>();
		 * for(BigraphNode nd : nodes.values()) { for(BigraphNode.OuterName ot :
		 * nd.getOuterNamesObjects()) { if(ot.isClosed() &&
		 * libBigOuterNames.containsKey(ot.getName()) &&
		 * !visited.contains(ot.getName())) {
		 * biBuilder.closeOuterName(ot.getName()); visited.add(ot.getName()); }
		 * } }
		 */

		// close every outername....should be removed...it is just for testing
		/*
		 * for(OuterName ot : libBigOuterNames.values()) {
		 * biBuilder.closeOuterName(ot); }
		 */

		// close innernames after creating nodes of the Bigraph
		for (BigraphNode.InnerName in : innerNames) {
			if (in.isClosed()) {
				biBuilder.closeInnerName(in.getName());
			}
		}

		// add sites to bigraph
		for (BigraphNode n : nodes.values()) {
			if (n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}

		// System.out.println("a "+biBuilder.makeBigraph());
		return biBuilder.makeBigraph();
	}

	protected Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots,
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {

		LinkedList<Handle> names = new LinkedList<Handle>();
		OuterName tmp;
		// find the difference between the outernames (i.e. connections) of the
		// node and the outernames defined for that node in the signature
		int difference = node.getOuterNames().size() - maxOuterNameNumber;

		// if knowledge is partial for the node,
		if (node.isKnowledgePartial()) {
			// then if number of outernames less than that in the signature,
			while (difference < 0) {
				// then the rest are either:
				// 1-created, added for that node.
				tmp = biBuilder.addOuterName();
				outerNames.put(tmp.getName(), tmp);
				node.addOuterName(tmp.getName());
				difference++;
				// 2-create, added, then closed for that node (they become links
				// or edges i.e. XX:e)
			}
			// if it is more than that in the signature, then

		} else {
			// if knowledge is exact and number of outernames are different,
			while (difference < 0) {
				// then create and close for that node.
				tmp = biBuilder.addOuterName();
				// close outernames
				biBuilder.closeOuterName(tmp);
				outerNames.put(tmp.getName(), tmp);
				node.addOuterName(tmp.getName());
				difference++;
			}
		}

		for (String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}

		// if the parent is a root
		if (node.isParentRoot()) { // if the parent is a root
			// System.out.println(node.getId());
			Node n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);

			nodes.put(node.getId(), n);
			return n;
		}

		// if the parent is already created as a node in the bigraph
		if (nodes.containsKey(node.getParent().getId())) {
			Node n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);

			nodes.put(node.getId(), n);
			return n;
		}

		// a node will take as outernames only the number specified in the
		// bigraph signature
		// for example, if a node has arity 2, then it will take only two
		// outernames (the first two) and ignore any other that might exist in
		// the names variable
		// if the number of outernames defined are less than in the signature,
		// then the rest of outernames will be defined as links (i.e. XX:e)
		// System.out.println("*node: " + node.getId() + " ctrl: " +
		// node.getControl());
		Node n = biBuilder.addNode(node.getControl(),
				createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);

		nodes.put(node.getId(), n);
		return n;

	}

	// protected void addChildren(BigraphNode parent, EList<Entity> entities,
	// Map<String, BigraphNode> nodes,
	// boolean isGround) {
	//
	// BigraphNode node;
	//
	// for (Entity entity : entities) {
	// node = new BigraphNode();
	//
	// node.setId(entity.getName());
	//
	// if (!isGround) {
	// // add site
	// node.setSite(entity.getSite() != null ? true : false);
	// }
	//
	// // add parent
	// node.setParent(parent);
	//
	// // add control (currently same as the name of the entity
	// node.setControl(entity.getName());
	//
	// // add connectivity (outernames)
	// if (entityConnectivityMap.get(entity) != null) {
	// for (Connectivity con : entity.getConnectivity()) {
	// node.addOuterName(con.getName(), con.isIsClosed());
	// }
	// }
	// nodes.put(node.getId(), node);
	//
	// addChildren(node, entity.getEntity(), nodes, isGround);
	// }
	// }

	protected void addChildrenNew(BigraphNode parent, List<String> entities, Map<String, BigraphNode> nodes,
			boolean isGround) {

		BigraphNode node;

		if (entities == null) {
			return;
		}

		boolean isFound = false;
		// int numRoots = numOfRoots;

		// if(parent== null) {
		// numRoots++;
		// numOfRoots++;
		// }

		for (String entity : entities) {

			isFound = false;
			node = new BigraphNode();

			node.setId(entity);

			if (!isGround) {
				// add site
				// by defualt it has site
				node.setSite(true);
			}

			// add parent
			if (parent != null) {
				node.setParent(parent);
			} else {
				node.setParentRoot(numOfRoots);
				// numOfRoots++;
			}

			// add control (currently same as the name of the entity
			node.setControl(getControl(entity));

			// add connectivity (outernames)
			if (entityConnectivityMap.get(entity) != null) {
				for (String con : entityConnectivityMap.get(entity)) {
					node.addOuterName(con, false);
				}
			}

			nodes.put(node.getId(), node);

			addChildrenNew(node, containedEntitiesMap.get(entity), nodes, isGround);
		}
	}

	public void modifyBrsExpression() {

		if (bigraphERString == null || bigraphERString.isEmpty()) {
			modifiedBigraphERString = generateModifiedBigraphERState();
			return;
		}

		// replace controls with equivlent entity names used
		// List<Token> tokens = brsTokenizer.getTokens();
		int fromIndex = 0;

		// String entityName = entities.get(index);
		// S
		// modifiedBrsExpression = brsExpression;
		StringBuffer temp = new StringBuffer(bigraphERString);
		int start = 0;
		int end = 0;

		for (String entityName : entities) {
			String ctrl = getControl(entityName);
			start = temp.indexOf(ctrl, fromIndex);
			end = start + ctrl.length();
			fromIndex = end;
			// System.out.println("entity: " + entityName + " ctrl: "+ctrl);
			temp.delete(start, end);
			temp.insert(start, entityName);

			// modifiedBrsExpression = modifiedBrsExpression.replace
		}

		modifiedBigraphERString = temp.toString();
		// for(Token t : tokens) {
		//
		// if(t)
		// }
	}

	public String generateBigraphERState() {

		if (modifiedBigraphERString == null || modifiedBigraphERString.isEmpty()) {
			modifiedBigraphERString = generateModifiedBigraphERState();
			// return;
		}

		// replace controls with equivlent entity names used
		// List<Token> tokens = brsTokenizer.getTokens();
		int fromIndex = 0;

		// String entityName = entities.get(index);
		// S
		// modifiedBrsExpression = brsExpression;
		StringBuffer temp = new StringBuffer(modifiedBigraphERString);
		int start = 0;
		int end = 0;

		for (String entityName : entities) {
			String ctrl = getControl(entityName);
			start = temp.indexOf(entityName, fromIndex);
			end = start + entityName.length();
			fromIndex = end;
			// System.out.println("entity: " + entityName + " ctrl: "+ctrl);
			temp.delete(start, end);
			temp.insert(start, ctrl);

			// modifiedBrsExpression = modifiedBrsExpression.replace
		}

		String bigrapher = temp.toString();
		// for(Token t : tokens) {
		//
		// if(t)
		// }

		return bigrapher;
	}

	public String getControl(String entityName) {

		for (Entry<Entity, String> entry : controlMap.entrySet()) {
			if (entry.getValue().equals(entityName)) {
				return entry.getKey().getName();
			}
		}
		return null;
	}

	public void clear() {
		entities.clear();
		connections.clear();
		containerEntitiesMap.clear();
		containedEntitiesMap.clear();
		connectivityMap.clear();
		entityConnectivityMap.clear();
		controlMap.clear();
		roots.clear();
		entitySiteMap.clear();
		bigraphERString = "";
		modifiedBigraphERString = "";
		bigraphExpression = null;

	}

	/**
	 * Generate a string representation of the Bigraph using BigraphER syntax.
	 * 
	 * @return String representation of the Bigraph
	 */
	public String generateModifiedBigraphERState() {

		StringBuilder bldr = new StringBuilder();

		int index = 0;

		for (String root : roots) {

			// add root
			bldr.append(root);

			// add connections
			List<String> cons = entityConnectivityMap.get(root);
			if (cons != null && !cons.isEmpty()) {

				bldr.append("{");

				int ind = 0;
				for (String con : cons) {
					bldr.append(con);

					ind++;

					if (ind < cons.size()) {
						bldr.append(", ");
					}
				}

				bldr.append("}");
			}

			// add children
			List<String> child = containedEntitiesMap.get(root);

			if (child != null && !child.isEmpty()) {

				bldr.append(".(");
				addChildren(root, child, bldr);
				bldr.append(")");
			} else {
				boolean hasSite = entitySiteMap.get(root);

				if (hasSite) {
					bldr.append(".id");
				}
			}

			index++;

			if (index < roots.size()) {
				// add bigraph juxtaposition ||
				bldr.append(" || ");
			}

		}

		// add root sites
		for (int i = 0; i < numberOfRootSites; i++) {
			bldr.append(" || id");
		}

		return bldr.toString();
	}

	protected void addChildren(String parentEntityName, List<String> entities, StringBuilder bldr) {

		// BigraphNode node;

		int index = 0;

		for (String entity : entities) {

			// add entity
			bldr.append(entity);

			// add connections
			// add connections
			List<String> cons = entityConnectivityMap.get(entity);
			if (cons != null && !cons.isEmpty()) {

				bldr.append("{");

				int ind = 0;
				for (String con : cons) {
					bldr.append(con);

					ind++;

					if (ind < cons.size()) {
						bldr.append(", ");
					}
				}

				bldr.append("}");
			}

			// add contained entities
			List<String> child = containedEntitiesMap.get(entity);

			if (child != null && !child.isEmpty()) {

				bldr.append(".(");

				// add contained entities
				addChildren(entity, child, bldr);

				// add site as entity juxtaposition i.e. | id
				Boolean hasSite = entitySiteMap.get(entity);

				if (hasSite != null && hasSite.booleanValue()) {
					bldr.append(" | id");
				}

				bldr.append(")");
			}

			else {
				Boolean hasSite = entitySiteMap.get(entity);

				if (hasSite != null && hasSite.booleanValue()) {
					bldr.append(".id");
				}
			}

			index++;

			if (index < entities.size()) {
				// add entity juxtaposition |
				bldr.append(" | ");
			}

		}
	}

	public void printAll() {

		System.out.println("//===== BRS expression");
		System.out.println("original: " + bigraphERString);
		modifyBrsExpression();
		System.out.println("modified: " + modifiedBigraphERString);

		System.out.println("\n//===== All entities");
		System.out.println(entities);
		System.out.println("\nControl map");
		System.out.println("\"entity\" => \"Control\"");
		for (Entry<Entity, String> entry : controlMap.entrySet()) {
			System.out.println(entry.getValue() + " => " + entry.getKey().getName());
		}
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("//===== Roots");
		System.out.println(roots);
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("//===== All containment relations");
		System.out.println("\n=Container relations (entity1's container is entity2)");
		System.out.println(containerEntitiesMap);
		System.out.println("\n=Contained entities relations (entity1 contains entities)");
		System.out.println(containedEntitiesMap);
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("//===== All connections");
		System.out.println(connections);
		System.out.println("\n=Connection and its entities");
		System.out.println(connectivityMap);
		System.out.println("\n=Entity and its connections");
		System.out.println(entityConnectivityMap);
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

	}

}
