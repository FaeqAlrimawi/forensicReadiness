package core.instantiation.analysis;

import java.util.LinkedList;
import java.util.List;

import core.brs.parser.BigraphRelationType;
import core.brs.parser.BigraphWrapper;
import core.brs.parser.utilities.JSONTerms;
import cyberPhysical_Incident.CyberPhysicalIncidentFactory;
import cyberPhysical_Incident.Entity;

/**
 * The aim of this class is to identify common and different structure(s)
 * between given Bigraph objects
 * 
 * @author Faeq
 *
 */
public class BigraphStructureAnalyser {

	// defines how much matching should there be between given bigraphs ALL
	// matching, most, some, etc.
	private Commonality commonality;

	/**
	 * Finds the commonalities between the given BigraphWarppers
	 * 
	 * @param bigraphWrappers
	 *            List of bigraphWrapper objects to search for commonalities
	 *            within
	 * @param commonality
	 *            Level of commonality (ALL, Most, etc.)
	 * @return
	 */
	public List<BigraphStructure> findCommonalities(List<BigraphWrapper> bigraphWrappers, Commonality commonality) {

		if (bigraphWrappers == null || bigraphWrappers.isEmpty()) {
			return null;
		}

		CyberPhysicalIncidentFactory instance = CyberPhysicalIncidentFactory.eINSTANCE;

		BigraphWrapper bigWrapper1 = bigraphWrappers.get(0);
		BigraphWrapper bigWrapper2 = bigraphWrappers.get(1);

		BigraphWrapper bigTemp = new BigraphWrapper();

		// ====focus on whatever I can find... so commonality at the moment is
		// not important
		// one (could be slow way) is to create bigraphs from initial elements
		// which grow to be the largest common

		List<BigraphStructure> result = new LinkedList<BigraphStructure>();

		// ==start from root of one bigraph wrapper and start building
		// == building another bigraph starts by adding a root, then start
		// adding an entity contained in that root, then connectivity

		for (String root : bigWrapper1.getRoots()) {
			System.out.println("checking root: " + root);

			//get control of root
			String rootControl = bigWrapper1.getControl(root);
			
			//ignore any of the irrelevant terms (e.g., KeyWords)
			if(JSONTerms.BIG_IRRELEVANT_TERMS.contains(rootControl)) {
				continue;
				
			}
			// add root
			bigTemp.addRoot(root);

			// get root entities
			List<String> rootEntities = bigWrapper1.getContainedEntitiesMap().get(root);

			if (rootEntities == null) {
				continue;
			}

			for (String entityName : rootEntities) {
				System.out.println("checking entity: " + entityName);
				// for each entity increment the wrapper by that entity
				String control = bigWrapper1.getControl(entityName);

				Entity ent = instance.createEntity();

				ent.setName(control);

				// add entity and its control
				bigTemp.addControl(ent, entityName);

				// do basic matching with the other bigwrapper to see if it
				// exists
				// no bigraph matching required just check if the entity control
				// exists in the other bigraphWrapper
				if (!bigWrapper2.hasControl(control)) {
					// create a bigraph structure
					BigraphStructure struct = new BigraphStructure();
					struct.setMainEntityName(control);
					struct.setStructureType(BigraphRelationType.ENTITY);
					result.add(struct);
					continue;
				}

			}
		}

		return result;
	}

	public Commonality getCommonality() {
		return commonality;
	}

	public void setCommonality(Commonality commonality) {
		this.commonality = commonality;
	}

}
