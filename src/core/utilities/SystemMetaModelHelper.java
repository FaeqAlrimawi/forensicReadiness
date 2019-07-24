package core.utilities;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import environment.CyberPhysicalSystemPackage;

public class SystemMetaModelHelper {

	private static Map<String, List<String>> systemTypes;

	public static Collection<String> getSystemTypes() {

	    if (systemTypes == null || systemTypes.isEmpty()) {
	      createSystemTypes();
	    }

	    if (systemTypes != null) {
	      return systemTypes.keySet();
	    }

	    return null;
	  }
	
	protected static Collection<String> createSystemTypes() {

		systemTypes = new HashMap<String, List<String>>();

		// read the system meta-model and identify all classes and convert them
		// into types
		Method[] packageMethods = CyberPhysicalSystemPackage.class.getDeclaredMethods();

		// Map<String, List<String>> classMap = new HashMap<String,
		// List<String>>();

		String className = null;

		for (Method mthd : packageMethods) {

			className = mthd.getName();
			Class cls = mthd.getReturnType();

			// only consider EClass as the classes
			if (!cls.getSimpleName().equals("EClass")) {
				continue;
			}

			// remove [get] at the beginning
			// if it contains __ then it is not a class its an attribute
			if (className.startsWith("get")) {
				className = className.replace("get", "");

				// create a class from the name
				String fullClassName = "environment.impl." + className + "Impl";

				Class potentialClass;
				int numOfLevels = 100; // determines how many superclasses to
										// add

				try {

					potentialClass = Class.forName(fullClassName);

					// get superclasses
					List<String> classHierarchy = new LinkedList<String>();
					int cnt = 0;

					do { // loop over superclasses

						String clsName = potentialClass.getSimpleName().replace("Impl", "");
						classHierarchy.add(clsName);
						potentialClass = potentialClass.getSuperclass();
						cnt++;
					} while (potentialClass != null && !potentialClass.getSimpleName().equals("Container")
							&& cnt < numOfLevels);

					// add new entry to the map
					systemTypes.put(className, classHierarchy);

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					// if it is not a class then skip
				}
			}

		}

		return systemTypes.keySet();
	}

}
