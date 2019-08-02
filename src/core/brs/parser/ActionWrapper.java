package core.brs.parser;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cyberPhysical_Incident.Entity;

public class ActionWrapper implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5906901715923235705L;

	private String actionName;

	private String reactName;

	private BigraphWrapper precondition;

	private BigraphWrapper postcondition;

	// list of entities and their occurrence in the action PRE
	private List<Map.Entry<String, Long>> preEntities;

	// list of entities and their occurrence in the action POST
	private List<Map.Entry<String, Long>> postEntities;

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public BigraphWrapper getPrecondition() {
		return precondition;
	}

	public void setPrecondition(BigraphWrapper precondition) {
		this.precondition = precondition;
	}

	public BigraphWrapper getPostcondition() {
		return postcondition;
	}

	public void setPostcondition(BigraphWrapper postcondition) {
		this.postcondition = postcondition;
	}

	public String getReactName() {
		return reactName;
	}

	public void setReactName(String reactName) {
		this.reactName = reactName;
	}

	public List<Map.Entry<String, Long>> getPreEntities() {

		return preEntities;
	}

	/**
	 * Finds all entities in the action with their occurrences(count) with the action
	 * @param excluding
	 * @param topK
	 * @param isAscending
	 * @return
	 */
	public List<Map.Entry<String, Long>> findAllEntities(List<String> excluding, int topK, boolean isAscending) {

		List<Map.Entry<String, Long>> allEntities = new LinkedList<Map.Entry<String,Long>>();
		
		allEntities.addAll(findPreEntities(excluding, topK, isAscending));
		allEntities.addAll(findPostEntities(excluding, topK, isAscending));
		
		return allEntities;

	}

	/**
	 * Finds all entities in the precondition and their count. It can exclude
	 * entities specified in the excluding list. It can retun the topK entities
	 * 
	 * @param excluding
	 * @param topK
	 * @return
	 */
	public List<Map.Entry<String, Long>> findPreEntities(List<String> excluding, int topK, boolean isAscending) {

		if (preEntities == null) {
			preEntities = new LinkedList<Map.Entry<String, Long>>();
		} else {
			preEntities.clear();
		}

		List<String> entities = new LinkedList<String>();

		if (precondition != null) {
			Set<Entity> ents = precondition.getControlMap().keySet();

			for (Entity ent : ents) {

				String name = ent.getName();

				// if it is a term to exclude, then continue
				if (excluding != null && excluding.contains(name)) {
					continue;
				}

				// if it is a reaction name that is used to identify the
				// action (i.e. an extra)
				if (name.equalsIgnoreCase(actionName)) {
					continue;
				}

				entities.add(name);
			}
		}

		Map<String, Long> map = entities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		if (topK > 0) {
			if (isAscending) {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.limit(topK).collect(Collectors.toList());
			} else {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.limit(topK).collect(Collectors.toList());
			}

		} else {

			if (isAscending) {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.collect(Collectors.toList());
			} else {
				preEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.collect(Collectors.toList());
			}
		}

		return preEntities;

	}

	public List<Map.Entry<String, Long>> getPostEntities() {

		return postEntities;
	}

	/**
	 * Finds all entities in the postcondition and their count. It can exclude
	 * entities specified in the excluding list. It can retun the topK entities
	 * 
	 * @param excluding
	 * @param topK
	 * @return
	 */
	public List<Map.Entry<String, Long>> findPostEntities(List<String> excluding, int topK, boolean isAscending) {

		if (postEntities == null) {
			postEntities = new LinkedList<Map.Entry<String, Long>>();
		} else {
			postEntities.clear();
		}

		List<String> entities = new LinkedList<String>();

		if (postcondition != null) {
			Set<Entity> ents = postcondition.getControlMap().keySet();

			for (Entity ent : ents) {

				String name = ent.getName();

				// if it is a term to exclude, then continue
				if (excluding != null && excluding.contains(name)) {
					continue;
				}

				// if it is a reaction name that is used to identify the
				// action (i.e. an extra)
				if (name.equalsIgnoreCase(actionName)) {
					continue;
				}

				entities.add(name);
			}
		}

		Map<String, Long> map = entities.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		if (topK > 0) {
			if (isAscending) {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.limit(topK).collect(Collectors.toList());
			} else {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.limit(topK).collect(Collectors.toList());
			}

		} else {

			if (isAscending) {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
						.collect(Collectors.toList());
			} else {
				postEntities = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
						.collect(Collectors.toList());
			}
		}

		return postEntities;

	}
}
