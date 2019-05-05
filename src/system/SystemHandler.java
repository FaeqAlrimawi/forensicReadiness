package system;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import com.eteks.sweethome3d.adaptive.security.assets.Asset;
import com.eteks.sweethome3d.adaptive.security.buildingGraph.BuildingLinkEdge;
import com.eteks.sweethome3d.adaptive.security.buildingGraph.BuildingRoomNode;
import com.eteks.sweethome3d.adaptive.security.buildingGraph.BuildingSecurityGraph;
import com.eteks.sweethome3d.adaptive.security.buildingGraph.CyberLinkEdge;

import environment.Action;
import environment.Connection;
import environment.CyberPhysicalSystemFactory;
import environment.CyberPhysicalSystemPackage;
import environment.DigitalAsset;
import environment.EnvironmentDiagram;
import environment.PhysicalAsset;

public class SystemHandler {

  public static final String                EXTENSION               = "cps";
  private static final EPackage             MODEL_PACKAGE_EINSTANCE = CyberPhysicalSystemPackage.eINSTANCE;
  private static final String               MODEL_PACKAGE_ENS_URI   = CyberPhysicalSystemPackage.eNS_URI;

  private static List<String>               systemAstsIDs           = new LinkedList<String>();
  private static EnvironmentDiagram         systemModel;
  private static Map<String, List<String>>  systemTypes;
  private static int                        conCount;
  private static Map<String, Action>        systemActions           = new HashMap<String, Action>();
  private static CyberPhysicalSystemFactory instance                = CyberPhysicalSystemFactory.eINSTANCE;
  private static Map<String, Action>        catalogActions;
  private static Map<String, Action>        deletedActions          = new HashMap<String, Action>();
  private static String                     filePath;

  private static BigraphERHandler bigHandler = new BigraphERHandler();
  
  public static String getSystemModelName() {

    if (systemModel != null) {
      return systemModel.getName();
    }

    return "";
  }

  public static String getFilePath() {
    return filePath;
  }
  
  public static String getBigraphERFilePath() {
    
    if(bigHandler != null) {
    return bigHandler.getFilePath();
    } else {
      return null;
    }
  }
  
public static String getBigraphERFileName() {
    
    if(bigHandler != null) {
    return bigHandler.getFileName();
    } else {
      return null;
    }
  }

  public static boolean generateSystemModel() {

    BuildingSecurityGraph graph = BuildingSecurityGraph.getInstance();
    extractSystemModel(graph);

    if (systemModel != null) {
      return true;
    }

    return false;
  }

  public static boolean generateSystemModel(String filePath) {

    BuildingSecurityGraph graph = BuildingSecurityGraph.getInstance();
    extractSystemModel(graph);

    if (systemModel != null) {
      return saveToFile(filePath);
    }

    return false;
  }

  public static boolean generateBigraphER() {

//    BigraphERHandler handler = new BigraphERHandler();

    return bigHandler.createBigraphERFile(systemModel);

  }

  public static EnvironmentDiagram extractSystemModel(BuildingSecurityGraph graph) {

    // ===create new system model
    // create environmentDiagram

    systemModel = instance.createEnvironmentDiagram();

    List<environment.Asset> systemAsts = new LinkedList<environment.Asset>();
    // List<String> systemAstsIDs = new LinkedList<String>();

    // create system types if not created
    if (systemTypes == null || systemTypes.isEmpty()) {
      createSystemTypes();
    }

    // ===reset
    systemAstsIDs.clear();
    conCount = 0;

    // === create assets
    Set<Asset> assets = graph.getSetOfBuildingObjects();
    List<BuildingRoomNode> roomList = graph.getRoomNodeList();

    if (assets == null || assets.isEmpty()) {
      System.out.println("home Assets are empty");
    } else {
      System.out.println("home Assets # " + assets.size());
    }

    if (roomList == null || roomList.isEmpty()) {
      System.out.println("home roomList is empty");
    } else {
      System.out.println("home rooms # " + roomList.size());
    }

    for (BuildingRoomNode room : roomList) {

      System.out.println("model room " + room.getId());

      if (!systemAstsIDs.contains(room.getId())) {
        systemAsts.addAll(extractSystemRoom(room));
      }

    }

    // add assets to model
    systemModel.getAsset().addAll(systemAsts);

    // === create connections

    // create room connections
    List<BuildingLinkEdge> connectionsBtwRooms = graph.getLinkEdgeList();

    if (connectionsBtwRooms == null || connectionsBtwRooms.isEmpty()) {
      System.out.println("connections between rooms are empty");
    } else {
      System.out.println("room connections # " + connectionsBtwRooms.size());
    }

    List<Connection> roomConnections = extractRoomConnections(connectionsBtwRooms);

    // add new room connections to system model
    systemModel.getConnection().addAll(roomConnections);

    Set<CyberLinkEdge> componentConnections = graph.getCyberLinks();

    if (componentConnections == null || componentConnections.isEmpty()) {
      System.out.println("component connections are empty");
    } else {
      System.out.println("component connections # " + componentConnections.size());
    }

    // create components connections
    List<Connection> systemComponentConnections = extractComponentConnections(componentConnections);

    // add new component connections to system model
    systemModel.getConnection().addAll(systemComponentConnections);

    // add actions to the system model
    if (systemActions != null) {
      systemModel.getAction().clear();
      systemModel.getAction().addAll(systemActions.values());
    }

    // print identified list
    printModelList();

    return systemModel;
  }

  protected static List<environment.Asset> extractSystemRoom(BuildingRoomNode room) {

    CyberPhysicalSystemFactory instance = CyberPhysicalSystemFactory.eINSTANCE;

    List<environment.Asset> systemAsts = new LinkedList<environment.Asset>();
    environment.Asset sysAsset = null;

    // create room
    sysAsset = instance.createRoom();

    // set name to room id
    sysAsset.setName(room.getId());

    // set parent
    // parent could be a floor that is created enternally

    // add new room to asset list
    systemAsts.add(sysAsset);

    // add new asset id to the ID list
    systemAstsIDs.add(room.getId());

    System.out.println("Room [" + sysAsset.getClass().getName() + "] added");

    // get contained assets
    List<Asset> containedAssets = room.getObjectsInside();

    for (Asset asset : containedAssets) {

      if (!systemAstsIDs.contains(asset.getId())) {
        systemAsts.addAll(extractSystemAssets(asset, sysAsset));
      }

    }

    return systemAsts;
  }

  protected static List<environment.Asset> extractSystemAssets(Asset asset, environment.Asset parentAsset) {

    CyberPhysicalSystemFactory instance = CyberPhysicalSystemFactory.eINSTANCE;

    List<environment.Asset> systemAsts = new LinkedList<environment.Asset>();
    environment.Asset sysAsset = null;

    String assetType = asset.getType() != null
        ? asset.getType().originalName()
        : null;
    // System.out.println("SH3D asst " + assetType);
    // if asset type is defined as a class in the system meta-model
    String sysType = hasType(assetType);

    if (sysType != null) {

      try {

        // System.out.println("sys asst " + sysType);
        EClassifier classif = CyberPhysicalSystemPackage.eINSTANCE.getEClassifier(sysType);// Class.forName(fullClassName);

        if (classif != null && classif instanceof EClass) {
          // create asset
          sysAsset = (environment.Asset)instance.create((EClass)classif);

        } else {
          // create a PhysicalAsset by default
          System.out.println("no object");
          sysAsset = instance.createPhysicalAsset();
        }

      } catch (Exception e) {
        // if calss is not recognised then create a PhysicalAsset by default
        sysAsset = instance.createPhysicalAsset();
      }
      // else create a default PhysicalAsset of the given asset
    } else {
      sysAsset = instance.createPhysicalAsset();
    }

    // set name
    sysAsset.setName(asset.getId());

    // set parent asset only if physical
    if (sysAsset instanceof PhysicalAsset && parentAsset instanceof PhysicalAsset) {
      // is physical
      ((PhysicalAsset)sysAsset).setParentAsset((PhysicalAsset)parentAsset);

      // or the asset is digital then it can have phys or dig parent
    } else if (sysAsset instanceof DigitalAsset) {
      ((DigitalAsset)sysAsset).setParentAsset(parentAsset);
    }

    // add new asset
    systemAsts.add(sysAsset);

    // add asset id to ID list
    systemAstsIDs.add(asset.getId());

    System.out.println("Asset [" + sysAsset.getClass().getName() + "] added");

    // return all contained assets
    for (Asset containedAsset : asset.getObjectContained()) {

      System.out.println("Asset " + sysAsset.getClass().getName() + "\n\tchecking asset " + containedAsset.getId());
      List<environment.Asset> allDescendants = extractSystemAssets(containedAsset, sysAsset);
      environment.Asset child = allDescendants != null && !allDescendants.isEmpty()
          ? allDescendants.get(0)
          : null;

      if (child != null) {

        if (sysAsset instanceof PhysicalAsset) { // check if the current asset
                                                 // is physical
          ((PhysicalAsset)sysAsset).getContainedAssets().add(child);

          // or the asset is digital then it should contain digital assets only
        } else if (sysAsset instanceof DigitalAsset && child instanceof DigitalAsset) {
          ((DigitalAsset)sysAsset).getContainedAssets().add((DigitalAsset)child);
        }
      }

      // the first in the list is the contained asset. the rest are descendants
      // of the contained asset
      for (environment.Asset ast : allDescendants) {
        systemAsts.add(ast);
        systemAstsIDs.add(ast.getName());
      }

    }

    // asset.get

    return systemAsts;
  }

  /**
   * Returns all asset types from the system meta-model
   * 
   * @param self
   * @return
   */
  protected static Collection<String> createSystemTypes() {

    systemTypes = new HashMap<String, List<String>>();

    // read the system meta-model and identify all classes and convert them
    // into types
    Method [] packageMethods = CyberPhysicalSystemPackage.class.getDeclaredMethods();

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
          } while (potentialClass != null && !potentialClass.getSimpleName().equals("Container") && cnt < numOfLevels);

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

  /**
   * Returns all available class names (as type)
   * @return collection of types as strings
   */
  public static Collection<String> getSystemTypes() {

    if (systemTypes == null || systemTypes.isEmpty()) {
      createSystemTypes();
    }

    if (systemTypes != null) {
      return systemTypes.keySet();
    }

    return null;
  }

  public static String hasType(String type) {

    if (type == null || type.isEmpty()) {
      return null;
    }

    for (String sysType : systemTypes.keySet()) {

      if (sysType.equalsIgnoreCase(type)) {
        return sysType;
      }
    }

    return null;
  }

  protected static List<Connection> extractRoomConnections(List<BuildingLinkEdge> roomCon) {

    CyberPhysicalSystemFactory instance = CyberPhysicalSystemFactory.eINSTANCE;
    List<Connection> connections = new LinkedList<Connection>();

    for (BuildingLinkEdge con : roomCon) {
      // create new physical connection
      Connection newCon = instance.createPhysicalConnection();

      // get asset 1 from room con
      environment.Asset asset1 = systemModel.getAsset(con.getFirstRoom());

      // get asset 2 from room con
      environment.Asset asset2 = systemModel.getAsset(con.getSecondRoom());

      // set con name
      if (con.getId() == null || con.getId().isEmpty()) {
        newCon.setName(newCon.getClass().getSimpleName().replace("Impl", "") + "_" + conCount);
        conCount++;
      }

      // set asset in connection
      newCon.setAsset1(asset1);
      newCon.setAsset2(asset2);

      // set connection in assets
      if (asset1 != null) {
        asset1.getConnections().add(newCon);
      }

      if (asset2 != null) {
        asset2.getConnections().add(newCon);
      }

      connections.add(newCon);
    }

    return connections;
  }

  protected static List<Connection> extractComponentConnections(Set<CyberLinkEdge> componentCon) {

    CyberPhysicalSystemFactory instance = CyberPhysicalSystemFactory.eINSTANCE;
    List<Connection> connections = new LinkedList<Connection>();

    for (CyberLinkEdge con : componentCon) {
      // create new physical connection
      Connection newCon = instance.createDigitalConnection();

      // get asset 1 from room con
      environment.Asset asset1 = systemModel.getAsset(con.getIdObject1());

      // get asset 2 from room con
      environment.Asset asset2 = systemModel.getAsset(con.getIdObject2());

      // set con name (it should start with lower-case letter)
      if (con.getName() == null || con.getName().isEmpty()) {
        String name = newCon.getClass().getSimpleName().replace("Impl", "") + "_" + conCount;
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1, name.length());
        newCon.setName(name);
        conCount++;
      }

      // set asset in connection
      newCon.setAsset1(asset1);
      newCon.setAsset2(asset2);

      // set connection in assets
      if (asset1 != null) {
        asset1.getConnections().add(newCon);
      }

      if (asset2 != null) {
        asset2.getConnections().add(newCon);
      }

      connections.add(newCon);
    }

    return connections;
  }

  public static boolean updateAndSaveSystemModel() {

    boolean isGenerated = generateSystemModel();

    if (!isGenerated) {
      return false;
    }

    return updateInFile();
  }

  public static boolean updateInFile() {

    return save(filePath);
  }

  public static boolean saveToFile(String path) {

    filePath = path;

    // set name
    int index = filePath.lastIndexOf("\\");

    if (index == -1) {
      // try the other way around
      index = filePath.lastIndexOf("/");
    }

    // not found
    if (index != -1) {
      String name = filePath.substring(index + 1, filePath.length());

      systemModel.setName(name);
    }

    boolean isSaved = save(path);

    return isSaved;
  }

  public static boolean extractAndSaveBigraphERFile(String fileName) {

    filePath = fileName;

    return extractAndSaveBigraphERFile();

  }

  public static boolean extractAndSaveBigraphERFile() {

//    BigraphERHandler brs = new BigraphERHandler();

    if (systemModel == null) {
      BuildingSecurityGraph graph = BuildingSecurityGraph.getInstance();
      extractSystemModel(graph);
    }

    boolean isExracted = bigHandler.extractBigraphERAndSave(systemModel, filePath);

    return isExracted;

  }

  protected static boolean save(String path) {

    // check that the path ends with system extension

    if (!path.endsWith(EXTENSION)) {
      path = path + "." + EXTENSION;
    }

    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

    ResourceSet rs = new ResourceSetImpl();
    // enable extended metadata
    final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
    rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);

    EPackage.Registry.INSTANCE.put(MODEL_PACKAGE_ENS_URI, MODEL_PACKAGE_EINSTANCE);

    Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {

      public Resource createResource(URI uri) {

        XMIResource xmiResource = new XMIResourceImpl(uri);

        return xmiResource;
      }
    });

    try {
      Resource r = rs.createResource(URI.createFileURI(path));

      r.getContents().add(systemModel);

      r.save(Collections.EMPTY_MAP);

      return true;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;

  }

  protected static void printModelList() {

    if (systemModel == null) {
      System.out.println("System is empty (NULL)");
      return;

    }

    System.out.println("***************** ASSETS **********************************");
    // assets
    for (environment.Asset ast : systemModel.getAsset()) {
      System.out.println("*" + ast.getName() + " type [" + ast.getClass().getSimpleName() + "]");
      List<environment.Asset> tmp = (List)ast.getContainedAssets();

      environment.Asset parent = ast.getParentAsset();
      String parentName = parent != null
          ? parent.getName()
          : null;

      // parent
      System.out.println("\t=Parent " + parentName);

      // children
      for (environment.Asset a : tmp) {
        System.out.println("\t=child " + a.getName());
      }

      // connections
      for (Connection con : ast.getConnections()) {
        System.out.println("\t=Connection " + con.getName());
      }
    }

    System.out.println("\n***************** CONNECTIONS *****************************");
    // connections
    for (Connection con : systemModel.getConnection()) {
      System.out.println("*" + con.getName() + " type [" + con.getClass().getSimpleName() + "]");

      // assets
      String asset1 = con.getAsset1() != null
          ? con.getAsset1().getName()
          : "null";
      String asset2 = con.getAsset2() != null
          ? con.getAsset2().getName()
          : "null";

      System.out.println("\t=Asset1 " + asset1);
      System.out.println("\t=Asset2 " + asset2);
    }

  }

  public static boolean addAction(String name, String pre, String post) {

    if (systemActions == null) {
      systemActions = new HashMap<String, Action>();
    }

    // create new action
    Action newAct = instance.createAction();

    newAct.setName(name);
    newAct.getPreconditions().add(pre);
    newAct.getPostconditions().add(post);

    systemActions.put(name, newAct);

    // System.out.println("Action: "+ newAct.getName() + " is added");
    // System.out.println("pre: "+ newAct.getPreconditions().get(0));
    // System.out.println("post: "+ newAct.getPostconditions().get(0));
    //
    return true;
  }

  public static boolean addActionFromCatalog(String name) {

    if (systemActions == null) {
      systemActions = new HashMap<String, Action>();
    }

    // get action from catalog if it exists

    Action act = catalogActions.get(name);

    if (act == null) {
      return false;
    }

    // copy to new action
    Action newAct = instance.createAction();

    // name
    newAct.setName(act.getName());

    // precondition
    String pre = !act.getPreconditions().isEmpty()
        ? act.getPreconditions().get(0)
        : null;
    newAct.getPreconditions().add(pre);

    // postcondition
    String post = !act.getPostconditions().isEmpty()
        ? act.getPostconditions().get(0)
        : null;
    newAct.getPostconditions().add(post);

    // description
    newAct.setDescription(act.getDescription());

    systemActions.put(name, newAct);

    // System.out.println("Action: "+ newAct.getName() + " is added");
    // System.out.println("pre: "+ newAct.getPreconditions().get(0));
    // System.out.println("post: "+ newAct.getPostconditions().get(0));

    return true;
  }

  public static String [] getActions() {

    if (systemActions == null) {
      return null;
    }

    return systemActions.keySet().toArray(new String [systemActions.size()]);
  }

  protected static Action getAction(String actionName) {

    if (systemActions == null) {
      return null;
    }

    // if action is not found
    if (!systemActions.containsKey(actionName)) {
      return null;
    }

    return systemActions.get(actionName);
  }

  public static String getActionPre(String actionName) {

    if (systemActions == null) {
      return null;
    }

    // if action is not found
    if (!systemActions.containsKey(actionName)) {
      return null;
    }

    Action act = systemActions.get(actionName);

    String pre = !act.getPreconditions().isEmpty()
        ? act.getPreconditions().get(0)
        : null;

    return pre;
  }

  public static String getActionPost(String actionName) {

    if (systemActions == null) {
      return null;
    }

    // if action is not found
    if (!systemActions.containsKey(actionName)) {
      return null;
    }

    Action act = systemActions.get(actionName);

    String post = !act.getPostconditions().isEmpty()
        ? act.getPostconditions().get(0)
        : null;

    return post;
  }

  public static Action getActionFromCatalog(String actionName) {

    if (catalogActions == null) {
      createCatalogActions();
    }

    // if action is not found
    if (!catalogActions.containsKey(actionName)) {
      return null;
    }

    return catalogActions.get(actionName);
  }

  public static boolean removeAction(String actionName) {

    if (actionName != null && systemActions.containsKey(actionName)) {
      Action action = systemActions.remove(actionName);

      if (deletedActions.size() > 100) {
        deletedActions.clear();
      }

      deletedActions.put(actionName, action);

      return true;
    }

    return false;
  }

  public static boolean deleteActions(List<String> actionNames) {

    // delete list of action with the given names
    // return true if succeeded

    return false;
  }

  public static String [] getCatalogActions() {

    // dummy for now

    if (catalogActions == null || catalogActions.isEmpty()) {
      createCatalogActions();
    }

    if (catalogActions == null) {
      return null;
    }

    return catalogActions.keySet().toArray(new String [catalogActions.size()]);
  }

  public static String getCatalogActionPrecondition(String actionName) {

    String pre = null;

    if (catalogActions != null) {
      if (catalogActions.containsKey(actionName)) {
        Action act = catalogActions.get(actionName);
        pre = !act.getPreconditions().isEmpty()
            ? act.getPreconditions().get(0)
            : null;
      }
    }

    return pre;
  }

  public static String getCatalogActionPostcondition(String actionName) {

    String post = null;

    if (catalogActions != null) {
      if (catalogActions.containsKey(actionName)) {
        Action act = catalogActions.get(actionName);
        post = !act.getPostconditions().isEmpty()
            ? act.getPostconditions().get(0)
            : null;
      }
    }

    return post;
  }

  protected static void createCatalogActions() {

    if (catalogActions == null) {
      catalogActions = new HashMap<String, Action>();
    }

    // add some dummy actions
    Action dummy1 = instance.createAction();
    Action dummy2 = instance.createAction();
    Action dummy3 = instance.createAction();

    dummy1.setName("enter");
    dummy1.getPreconditions().add("Room.Actor | Room");
    dummy1.getPostconditions().add("Room | Room.Actor ");

    dummy2.setName("connect");
    dummy2.getPreconditions().add("connect-pre");
    dummy2.getPostconditions().add("connect-post");

    dummy3.setName("disconnect");
    dummy3.getPreconditions().add("disconnect pre");
    dummy3.getPostconditions().add("disconnect post");

    catalogActions.put("enter", dummy1);
    catalogActions.put("connect", dummy2);
    catalogActions.put("disconnect", dummy3);
  }

  public static boolean hasAction(String actionName) {

    if (systemActions == null || systemActions.isEmpty()) {
      return false;
    }

    return systemActions.containsKey(actionName);
  }

  public static boolean restoreAction(String actionName) {

    // move action from deleted to system actions
    Action action = deletedActions.remove(actionName);

    if (action == null) {
      return false;
    }

    systemActions.put(actionName, action);

    return true;
  }

  public static boolean isRemoved(String actionName) {

    return deletedActions.containsKey(actionName);
  }

  public static boolean isSystemModelGenerated() {

    if (systemModel == null) {
      return false;
    }

    return true;
  }

  public static boolean deleteSystemModel() {

    if (systemModel == null) {
      return false;
    }

    systemModel = null;

    boolean isDeleted = false;

    if (filePath != null) {
      File file = new File(filePath);
      isDeleted = file.delete();
    }

    if(isDeleted) {
      bigHandler.delete();
    }
    return isDeleted;

  }
}
