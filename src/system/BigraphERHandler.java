package system;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.eteks.sweethome3d.adaptive.forensics.SystemHandler;

import environment.Action;
import environment.Connection;
import environment.EnvironmentDiagram;

public class BigraphERHandler {

  // bigrapher file syntax
  public static final String        BIGRAPHER_CONTROL     = "ctrl";
  public static final String        BIGRAPHER_ATOMIC      = "atomic";
  public static final String        BIGRAPHER_REACT       = "react";
  public static final String        BEGIN                 = "begin";
  public static final String        TYPE_BRS              = "brs";
  public static final String        TYPE_PBRS             = "pbrs";
  public static final String        TYPE_SBRS             = "sbrs";
  public static final String        INITIAL_STATE         = "initial_status";
  public static final String        INIT                  = "init";
  public static final String        RULES                 = "rules";
  public static final String        BIG                   = "big";
  public static final String        EXTENSION             = "big";
  public static final String        END                   = "end";

  public static final String        CONTAINS              = ".";
  public static final String        NODE_JUXTAPOSITION    = "|";
  public static final String        BIGRAPH_JUXTAPOSITION = "||";
  public static final String        BIGRAPHER_REACT_ARROW = "->";

  public static final String        SPACE                 = " ";
  public static final String        EQUALS                = " = ";
  public static final String        SEMICOLON             = ";";
  public static final String        COMMENT_SIGN          = "#";
  public static final String        GROUND                = "1";
  public static final String        NEWLINE               = System.getProperty("line.separator");

  private static final String       CONTAINMENT_HEAD      = "HeadOfTree";
  private static final String       BIGRAPHER_NAME_TAIL   = "BigraphER";

  private EnvironmentDiagram        systemModel;

  // key is a the name of the control
  private Map<String, Control>      controls;
  private Map<String, ReactionRule> reactions;

  private ContainmentTree<String>   containment;
  private Map<String, List<String>> connectivityMap;

  // key is asset name, and key is Class name (or Control)
  private Map<String, String>       assetControlMap;

  private int                       connectionNum         = 0;
  private int                       paddedConNum          = 0;
  private String                    connectionName        = "con_";
  private String                    paddedConName         = "paddedCon_";
  private String                    initialStateString;
  private String                    systemFileName;
  private String                    brsFileName;

  private String                    bigraphERStatement;
  private boolean                   isTesting             = true;
  private String                    outputFolder;
  private String                    bigrapherValidateCmd  = "bigrapher validate -n ";
  private String                    validBigrapherString  = "model file parsed correctly";
  private String                    bigrapherFileName;
  // private String bigrapherOutputFormat = "json"; // json
  // (others:
  // svg,
  // txt)
  private int                       maxNumOfStates;

  public BigraphERHandler() {

    controls = new HashMap<String, Control>();
    reactions = new HashMap<String, BigraphERHandler.ReactionRule>();
    connectivityMap = new HashMap<String, List<String>>();
    assetControlMap = new HashMap<String, String>();

  }

  public boolean extractBigraphERAndSave(EnvironmentDiagram systemModel, String fileName) {

    boolean isCreated = createBigraphERFile(systemModel);

    if (!isCreated) {
      return false;
    }

    systemFileName = fileName;
    saveToFile(fileName);

    return true;
  }

  public boolean createBigraphERFile(EnvironmentDiagram systemModel) {

    this.systemModel = systemModel;

    if (systemModel == null) {
      System.out.println("system model is null!");
      return false;
    }

    clearData();

    // === Define controls for the brs
    // controls are classes of all assets that are in the model
    for (environment.Asset ast : systemModel.getAsset()) {

      String cls = ast.getClass().getSimpleName().replace("Impl", "");

      // connections number depends on how many connection the asset has in the
      // model
      int connectivity = ast.getConnections().size();
      boolean isAtomic = false;

      // if the control already exists check if the number of connectivity is
      // different
      // if the new connectivity is more then set it in the control
      if (controls.containsKey(cls)) {
        Control cont = controls.get(cls);
        if (connectivity > cont.getConnectivity()) {
          cont.setConnectivity(connectivity);
        }
      } else {
        Control ctl = new Control(cls, connectivity, isAtomic);
        controls.put(cls, ctl);
      }

    }

    // define reaction rules
    for (Action act : systemModel.getAction()) {

      String name = act.getName();
      String pre = !act.getPreconditions().isEmpty()
          ? act.getPreconditions().get(0)
          : "";
      String post = !act.getPostconditions().isEmpty()
          ? act.getPostconditions().get(0)
          : "";

      ReactionRule rule = new ReactionRule(name, pre, post);
      reactions.put(name, rule);

    }

    // ==== create initial state
    initialStateString = createInitialState();

    return true;
  }

  protected void clearData() {

    controls.clear();
    reactions.clear();
    initialStateString = null;
  }

  /**
   * creates initial state of containment and connectivity
   */
  protected String createInitialState() {

    StringBuilder bldr = new StringBuilder();

    // hold containment of for all assets
    containment = new ContainmentTree<String>(CONTAINMENT_HEAD);

    // assets
    List<environment.Asset> assets = systemModel.getAsset();

    // === containment
    // for each asset add as a leaf with the given parent
    for (environment.Asset ast : assets) {
      String astName = ast.getName();
      String astClassName = ast.getClass().getSimpleName().replace("Impl", "");

      assetControlMap.put(astName, astClassName);

      environment.Asset parent = ast.getParentAsset();

      if (parent != null) {
        String parentName = parent.getName();
        containment.addLeaf(parentName, astName);
      } else {
        containment.addLeaf(astName);
      }
    }

    // ==== connectivity
    List<Connection> cons = systemModel.getConnection();

    // create a map of connections in which each asset has a list of connections
    for (Connection con : cons) {

      String conName = con.getName();

      if (conName == null || conName.isEmpty()) {
        conName = connectionName + connectionNum;
        connectionNum++;
      }
      // make sure that the first letter is lower case
      else {
        conName = Character.toLowerCase(conName.charAt(0)) + conName.substring(1, conName.length());
      }

      String ast1 = con.getAsset1() != null
          ? con.getAsset1().getName()
          : null;
      String ast2 = con.getAsset2() != null
          ? con.getAsset2().getName()
          : null;

      if (ast1 != null) {
        if (connectivityMap.containsKey(ast1)) {
          connectivityMap.get(ast1).add(conName);
        } else {
          List<String> conList = new LinkedList<String>();
          conList.add(conName);
          connectivityMap.put(ast1, conList);
        }

        if (ast2 != null) {
          if (connectivityMap.containsKey(ast2)) {
            connectivityMap.get(ast2).add(conName);
          } else {
            List<String> conList = new LinkedList<String>();
            conList.add(conName);
            connectivityMap.put(ast2, conList);
          }
        }

      }
    }

    // convert to bigraphER format
    int index = 0;
    int lastIndex = containment.getSubTrees().size() - 1;
    // all head leafs are in bigraph juxtaposition (||)
    for (ContainmentTree<String> root : containment.getSubTrees()) {

      if (index != lastIndex) {
        bldr.append(getBigraphERString(root)).append(BIGRAPH_JUXTAPOSITION).append(NEWLINE);
      } else {
        bldr.append(getBigraphERString(root));
      }

      index++;
    }

    return bldr.toString();

  }

  protected String getBigraphERString(ContainmentTree<String> tree) {

    StringBuilder containmentBldr = new StringBuilder();
    StringBuilder bldr = new StringBuilder();
    StringBuilder conBldr = new StringBuilder();

    int index = 0;
    int lastIndex = tree.getSubTrees().size() - 1;

    for (ContainmentTree<String> leaf : tree.getSubTrees()) {
      if (index != lastIndex) {
        containmentBldr.append(getBigraphERString(leaf)).append(NODE_JUXTAPOSITION);
      } else {
        containmentBldr.append(getBigraphERString(leaf));
      }

      index++;
    }

    String assetName = tree.getHead();
    String control = assetControlMap.get(assetName);
    Control ctrl = controls.get(control);

    // set connectivity
    List<String> connectivity = connectivityMap.get(assetName);

    if (connectivity != null) {

      // add padded connectivity to the asset
      if (ctrl != null) {
        int ctrlCon = ctrl.getConnectivity();
        int astCon = connectivity.size();
        for (int i = (ctrlCon - astCon); i > 0; i--) {
          connectivity.add(paddedConName + paddedConNum);
          paddedConNum++;
        }
      }

      // create connectivity BigraphER string
      int conIndex = 0;
      int conLastIndex = connectivity.size() - 1;

      conBldr.append("{");
      for (String con : connectivity) {

        if (conIndex != conLastIndex) {
          conBldr.append(con).append(", ");
        } else {
          conBldr.append(con);
        }

        conIndex++;
      }
      conBldr.append("}");
    }

    // if it contains other entities or not
    if (containmentBldr.length() > 0) {

      // if there's connectivity
      if (conBldr.length() > 0) {
        bldr.append(control).append(conBldr.toString()).append(CONTAINS).append("(").append(containmentBldr.toString())
            .append(")");
      } else {
        bldr.append(control).append(CONTAINS).append("(").append(containmentBldr.toString()).append(")");
      }

      // it does not contain anything, hence, a 1 should be added if control is
      // not atomic
    } else {

      boolean isAtomic = false;
      if (ctrl != null) {
        isAtomic = ctrl.isAtomic();
      }

      // if is atomic then nothing will be added. otherwise, a ground (1) should
      // be added
      if (isAtomic) {
        if (conBldr.length() > 0) {
          bldr.append(control).append(conBldr.toString());
        } else {
          bldr.append(control);
        }

        // is not atomic (ground is added)
      } else {
        if (conBldr.length() > 0) {
          bldr.append(control).append(conBldr.toString()).append(CONTAINS).append(GROUND);
        } else {
          bldr.append(control).append(CONTAINS).append(GROUND);
        }
      }

    }

    return bldr.toString();

  }

  // protected String getConnectivityString(String controlName) {
  //
  // }

  // protected void addChildern(environment.Asset asset, ContainmentTree<String>
  // tree) {
  //
  //
  // for(environment.Asset ast : asset.getContainedAssets()) {
  // tree.addLeaf(leaf)
  // }
  // }

  public String saveToFile(String fileName) {

    // if file name ends with cps then remove it first
    if (fileName.endsWith(SystemHandler.EXTENSION)) {
      fileName = fileName.replace("." + SystemHandler.EXTENSION, "");
    }

    // add a bigrapher
    fileName = fileName + BIGRAPHER_NAME_TAIL;

    if (!fileName.endsWith(EXTENSION)) {
      fileName = fileName + "." + EXTENSION;
    }

    try {

      brsFileName = fileName;

      StringBuilder bldr = new StringBuilder();

      // if (!brsFile.exists()) {
      // brsFile.createNewFile();

      /** === meta information about the system model **/
      bldr.append(COMMENT_SIGN).append("BRS file created from system model [").append(systemModel.getName()).append("]")
          .append(NEWLINE).append(COMMENT_SIGN).append("System model file path [").append(systemFileName).append("]")
          .append(NEWLINE);

      // === Controls
      bldr.append(NEWLINE).append(COMMENT_SIGN).append("=====Controls").append(NEWLINE);

      for (Control ctl : controls.values()) {

        bldr.append(ctl.toString()).append(NEWLINE);
      }

      /** === Initial state **/
      bldr.append(NEWLINE).append(COMMENT_SIGN).append("=====Initial Status").append(NEWLINE);

      // big initial_status = [status]
      bldr.append(BIG).append(SPACE).append(INITIAL_STATE).append(EQUALS).append(NEWLINE).append(initialStateString)
          .append(NEWLINE).append(SEMICOLON).append(NEWLINE);

      /** === Reaction rules **/
      bldr.append(NEWLINE).append(COMMENT_SIGN).append("=====Reaction rules").append(NEWLINE);

      for (ReactionRule react : reactions.values()) {
        bldr.append(react.toString()).append(NEWLINE);
      }

      /** === setup information about the brs **/
      bldr.append(NEWLINE).append(COMMENT_SIGN).append("=====BRS").append(NEWLINE);

      // begin brs_type (brs, pbrs, sbrs)
      // init [initial state]
      // rules =
      // [{rulename,...}];
      // end

      // brs type
      bldr.append(BEGIN).append(SPACE).append(TYPE_BRS).append(NEWLINE);
      // initial state
      bldr.append(INIT).append(SPACE).append(INITIAL_STATE).append(SEMICOLON).append(NEWLINE);

      // reaction rules
      bldr.append(RULES).append(EQUALS).append(NEWLINE).append("[{").append(NEWLINE);

      if (reactions.size() > 0) {
        int index = 0;
        int lastIndex = reactions.size() - 1;

        for (String actName : reactions.keySet()) {
          if (index != lastIndex) { // reaction rule names
            bldr.append(actName).append(",").append(NEWLINE);
          } else {
            bldr.append(actName).append(NEWLINE);
          }
          index++;
        }
      } else { // no reaction rules
        bldr.append(COMMENT_SIGN).append("No reaction rules specified. *Reaction rules are required for analysis.")
            .append(NEWLINE);
      }

      bldr.append("}];").append(NEWLINE);

      // end
      bldr.append(NEWLINE).append(END).append(NEWLINE);

      // write to file
      try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(brsFileName), "utf-8"))) {
        writer.write(bldr.toString());
      }

    } catch (IOException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    return "";
  }

  public boolean delete() {

    boolean isDeleted = false;

    if (brsFileName != null) {
      File file = new File(brsFileName);

      isDeleted = false;

      isDeleted = file.delete();

    }

    clearData();

    return isDeleted;
  }

  public String getFilePath() {
    return brsFileName;
  }

  public String getFileName() {

    int index = -1;

    index = brsFileName.lastIndexOf('\\');

    if (index == -1) {
      index = brsFileName.lastIndexOf('/');
    }

    if (index == -1) {
      return null;
    }

    String name = brsFileName.substring(index + 1, brsFileName.length());

    return name;
  }

  public String execute() {

    return execute(bigraphERStatement);
  }

  /**
   * Validates and then executes the Bigrapher file (*.big)
   * 
   * @return output folder name if execution is successeful. Otherwise, it
   *         returns null
   */
  public String execute(String bigrapherCommand) {

    if (isTesting) {
      return "";
    }

    if (validateBigraph()) {

      Process proc;
      // String cmd = bigraphERStatement;

      Runtime r = Runtime.getRuntime();
      try {
        r.exec("mkdir " + outputFolder);

        // for future development this could run in own thread for
        // multiprocessing
        proc = r.exec(bigrapherCommand);

        // check the output of the command, if it has something then
        // there
        // are errors otherwise its ok
        @SuppressWarnings("resource")
        Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
        String result = s.hasNext()
            ? s.next()
            : "";

        if (result != null) {
          if (!result.toLowerCase().isEmpty()) {
            System.out.println("Execution could not be completed. Please see possible issues below:");
            System.out.println(result);
          } else {
            System.out.println("Execution is Done");

            // should be a step taken by the main program
            // createDigraph();

          }
        }

        return "";

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return null;
  }

  protected boolean validateBigraph() {
    boolean isValid = false;
    Process proc;
    Runtime r = Runtime.getRuntime();
    try {

      proc = r.exec(bigrapherValidateCmd + bigrapherFileName);

      @SuppressWarnings("resource")
      Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
      String result = s.hasNext()
          ? s.next()
          : "";

      if (result != null) {
        if (result.toLowerCase().contains(validBigrapherString)) {
          System.out.println(bigrapherFileName + " is valid");
          isValid = true;
        } else {
          System.out.println(bigrapherFileName + " is not valid. Please see possible issues below:");
          System.out.println(result + "");
          isValid = false;

        }
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      isValid = false;
    }
    return isValid;
  }

  public String createBigrapherExecutionCmd(String bigrapherFileName, int maximumNumOfStates, String outputFolder) {

    return createBigrapherExecutionCmd(bigrapherFileName, maximumNumOfStates, outputFolder, false, false, false, false,
        false);

  }

  public String createBigrapherExecutionCmd(String bigrapherFileName, int maximumNumOfStates, String outputFolder,
                                            boolean isSVG, boolean isTXT, boolean isDeclarations) {

    return createBigrapherExecutionCmd(bigrapherFileName, maximumNumOfStates, outputFolder, isSVG, isTXT,
        isDeclarations, false, false);

  }

  protected String createBigrapherExecutionCmd(String bigrapherFileName, int maximumNumOfStates, String outputFolder,
                                               boolean isSVG, boolean isTXT, boolean isDeclarations,
                                               boolean isPredicate, boolean isLabel) {

    maxNumOfStates = maximumNumOfStates;
    this.bigrapherFileName = bigrapherFileName;

    while (outputFolder.contains("\\")) {
      outputFolder = outputFolder.replace('\\', '/');
    }

    while (bigrapherFileName.contains("\\")) {
    	bigrapherFileName = bigrapherFileName.replace('\\', '/');
      }

    
    if (outputFolder.endsWith("/")) {
      outputFolder = outputFolder.substring(0, outputFolder.length() - 1);
    }

    this.outputFolder = outputFolder;

    StringBuilder res = new StringBuilder();
    res.append("bigrapher full -M ").append(maxNumOfStates).append(" -t ").append(outputFolder).append("/transitions")
        .append(" -s ").append(outputFolder).append(" -f json");

    // add other formats
    if (isSVG) {
      res.append(",svg");
    }

    if (isTXT) {
      res.append(",txt");
    }

    // generate delcarations
    if (isDeclarations) {
      res.append(" -d ").append(outputFolder).append("/declarations");
    }

    // transition system in prism
    if (isPredicate) {
      res.append(" -p ").append(outputFolder).append("/prism");
    }

    // labelling function in prism
    if (isLabel) {
      res.append(" -l ").append(outputFolder).append("/label");
    }

    res.append(" ").append(bigrapherFileName);

    bigraphERStatement = res.toString();

    return bigraphERStatement;
  }

  /**
   * 
   * 
   * 
   * @author Faeq
   */
  public class ReactionRule {

    String name;
    String redex;
    String reactum;

    public ReactionRule() {

    }

    public ReactionRule(String name, String redex, String reactum) {
      this.name = name;
      this.redex = redex;
      this.reactum = reactum;
    }

    public String getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getRedex() {
      return this.redex;
    }

    public void setRedex(String redex) {
      this.redex = redex;
    }

    public String getReactum() {
      return this.reactum;
    }

    public void setReactum(String reactum) {
      this.reactum = reactum;
    }

    public String toString() {

      StringBuilder bldr = new StringBuilder();

      /**
       * react name = redex -> reactum ;
       */
      bldr.append(BIGRAPHER_REACT).append(SPACE).append(name).append(SPACE).append(EQUALS).append(NEWLINE).append(redex)
          .append(NEWLINE).append(BIGRAPHER_REACT_ARROW).append(NEWLINE).append(reactum).append(NEWLINE)
          .append(SEMICOLON);

      return bldr.toString();

    }

  }

  public class Control {

    String  name;
    int     connectivity;
    boolean isAtomic;

    public Control() {

    }

    public Control(String name, int connectivity, boolean isAtomic) {
      this.name = name;
      this.connectivity = connectivity;
      this.isAtomic = isAtomic;
    }

    public String getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getConnectivity() {
      return this.connectivity;
    }

    public void setConnectivity(int connectivity) {
      this.connectivity = connectivity;
    }

    public boolean isAtomic() {
      return this.isAtomic;
    }

    public void setAtomic(boolean isAtomic) {
      this.isAtomic = isAtomic;
    }

    public String toString() {

      StringBuilder bldr = new StringBuilder();

      if (isAtomic) {
        bldr.append(BIGRAPHER_ATOMIC).append(SPACE);
      }

      // if -1 means it's not known how many connections control can have
      if (connectivity == -1) {
        // if so, then find how many connections it can have (to be implemented)
        // for now it is assumed to be 1
        connectivity = 1;
      }

      bldr.append(BIGRAPHER_CONTROL).append(SPACE).append(name).append(EQUALS).append(connectivity).append(SEMICOLON);

      return bldr.toString();
    }

  }

}
