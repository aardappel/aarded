import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MainWin extends Frame {
  Code buffer = null;
  Project proj = new Project(this);
  Vector allviews = new Vector();
  java.awt.List plist = new java.awt.List(7);
  BorderLayout borderLayout1 = new BorderLayout();
  MenuBar menuBar1 = new MenuBar();
  Menu menuFile = new Menu();
  MenuItem menuFileExit = new MenuItem();
  Menu menuHelp = new Menu();
  MenuItem menuHelpAbout = new MenuItem();
  MenuItem menuItem1 = new MenuItem();
  MenuItem menuItem2 = new MenuItem();
  MenuItem menuItem3 = new MenuItem();
  Menu menu1 = new Menu();
  MenuItem menuItem4 = new MenuItem();
  MenuItem menuItem5 = new MenuItem();
  TextArea textArea1 = new TextArea();
  Menu menu2 = new Menu();
  MenuItem addfile = new MenuItem();
  MenuItem addmodule = new MenuItem();
  MenuItem addbag = new MenuItem();
  Menu menu3 = new Menu();
  MenuItem menuItem9 = new MenuItem();
  MenuItem menuItem10 = new MenuItem();
  String prefsfile = "AardEd.prefs";
  String lastproject = null;
  Hashtable seenatoms = new Hashtable();

  void quit() {
    saveprefs();
    System.exit(1);
  }

  public MainWin() {
    try {
      jbInit();
    } catch (Exception e) {
      System.out.println("AardEd internal exception: " + e);
      e.printStackTrace();
    } catch (Error f) {
      System.out.println("AardEd internal error: " + f);
      f.printStackTrace();
    }
  }

  public void jbInit() throws Exception {
    setLayout(borderLayout1);
    setBackground(SystemColor.window);
    //this.setSize(new Dimension(300, 200));
    setTitle("Aardappel Project Editor");
    menuFile.setLabel("Project");
    menuFileExit.setLabel("Exit");
    menuFileExit.addActionListener(new MainWin_menuFileExit_ActionAdapter(this));
    menuHelp.setLabel("Help");
    menuHelpAbout.setLabel("About");
    menuItem1.setLabel("New");
    menuItem1.addActionListener(new MainWin_menuItem1_actionAdapter(this));
    menuItem2.setLabel("Open ...");
    menuItem2.addActionListener(new MainWin_menuItem2_actionAdapter(this));
    menuItem3.setLabel("Save All");
    menuItem3.setShortcut(new MenuShortcut(65));
    menuItem3.addActionListener(new MainWin_menuItem3_actionAdapter(this));
    menu1.setLabel("Edit");
    menuItem4.setLabel("Remove");
    menuItem5.setLabel("Rename");
    textArea1.setBackground(SystemColor.menu);
    textArea1.setEditable(false);
    menu2.setLabel("Add");
    addfile.setLabel("File...");
    addfile.addActionListener(new MainWin_addfile_actionAdapter(this));
    addmodule.setLabel("New Module");
    addmodule.addActionListener(new MainWin_addmodule_actionAdapter(this));
    addbag.setLabel("New Bag");
    addbag.addActionListener(new MainWin_addbag_actionAdapter(this));
    menu3.setLabel("Deploy");
    menuItem9.setLabel("JVM Class");
    menuItem9.addActionListener(new MainWin_menuItem9_actionAdapter(this));
    menuItem10.setLabel("JVM Standalone Jar");
    menuItem10.addActionListener(new MainWin_menuItem10_actionAdapter(this));
    menuItem5.addActionListener(new MainWin_menuItem5_actionAdapter(this));
    menuItem4.addActionListener(new MainWin_menuItem4_actionAdapter(this));
    plist.addActionListener(new MainWin_plist_actionAdapter(this));
    menuHelpAbout.addActionListener(new MainWin_menuHelpAbout_ActionAdapter(this));
    menuFile.add(menuItem1);
    menuFile.add(menuItem2);
    menuFile.add(menu2);
    menuFile.add(menu3);
    menuFile.add(menuItem3);
    menuFile.add(menuFileExit);
    menuHelp.add(menuHelpAbout);
    menuBar1.add(menuFile);
    menuBar1.add(menu1);
    menuBar1.add(menuHelp);
    this.add(plist, BorderLayout.NORTH);
    this.add(textArea1, BorderLayout.CENTER);
    this.setMenuBar(menuBar1);
    plist.setBackground(Color.white);
    this.addWindowListener(new MainWin_this_windowAdapter(this));
    menu1.add(menuItem4);
    menu1.add(menuItem5);
    menu2.add(addfile);
    menu2.add(addmodule);
    menu2.add(addbag);
    menu3.add(menuItem9);
    menu3.add(menuItem10);
    pack();
    setLocation(100, 100);
    setSize(300, 250);
    show();
    //for(int i = 0;i<1000000000;i++) { String s = "jgjgj" + i; }
    loadatomimages();
    loadprefs();
    loadlastproject();
  }

  void loadatomimages() {
    File f = new File("atomimages");
    String names[] = f.list();
    MediaTracker mt = new MediaTracker(this);
    for (int a = 0; a < names.length; a++) {
      String atom = names[a];
      String name = (new File(f.getAbsolutePath(), atom)).toString();
      msg("loading atom: " + name);
      Image i = Toolkit.getDefaultToolkit().getImage(name);
      int cut = atom.lastIndexOf('.');
      if (cut > 0) atom = atom.substring(0, cut);
      try {
        atom = "" + (char) Integer.parseInt(atom);
      } catch (Exception e) {
      }
      checkatom(atom, i);
      mt.addImage(i, a);
    }
    try {
      mt.waitForAll();
    } catch (Exception e) {
    }
  }

  class AtomInfo {
    Image i;
  }

  Image checkatom(String name, Image i) {
    AtomInfo a = (AtomInfo) seenatoms.get(name);
    if (a == null) seenatoms.put(name, a = new AtomInfo());
    if (i != null) a.i = i;
    return a.i;
  }

  void loadprefs() {
    try {
      BufferedReader in = new BufferedReader(new FileReader(prefsfile));
      //DataInputStream in = new�DataInputStream(new�FileInputStream(prefsfile));
      String s;
      while ((s = in.readLine()) != null) {
        int t = s.charAt(0);
        if (s.charAt(1) != ':') {
          msg("prefs file format error (missing ':')");
          break;
        }
        s = s.substring(2);
        if (t == 'L') {
          lastproject = s;
        } else {
          msg("prefs file format error (unknown code)");
          break;
        }
      }
      in.close();
    } catch (IOException i) {
      msg("couldn't load prefs file " + prefsfile);
    }
  }

  void saveprefs() {
    try {
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(prefsfile));
      if (lastproject != null) dos.writeBytes("L:" + lastproject + "\n");
      dos.close();
    } catch (IOException i) {
      msg("problem while saving " + prefsfile);
    }
  }

  void loadlastproject() {
    if (lastproject != null) proj.loadproject(new File(lastproject), plist);
  }

  void msg(String s) {
    textArea1.append(s + "\n");
  }

  void haschanged(Code c, Treeview orig) {
    for (int a = 0; a < allviews.size(); a++) {
      Treeview tv = (Treeview) allviews.elementAt(a);
      // todo: check wether tv displays c
      // needs lock because of paint()
      if (tv != orig) tv.refreshtree(false);
    }
  }

  void viewclosing(Treeview tv) {
    allviews.removeElement(tv);
  }

  void plist_actionPerformed(ActionEvent e) {
    String s = e.getActionCommand();
    allviews.addElement(new Treeview(this, s, proj.get(s).code()));
  }

  void delete_actionPerformed(ActionEvent e) {
    String s = plist.getSelectedItem();
    if (s != null) {
      if ((new StringRequest(this, "Are you sure?", null, true)).result != null) {
        proj.remove(s);
        plist.delItem(plist.getSelectedIndex());
      }
    }
  }

  void rename_actionPerformed(ActionEvent e) {
    String s = plist.getSelectedItem();
    if (s != null) {
      s = proj.rename(s);
      plist.replaceItem(s, plist.getSelectedIndex());
    }
  }

  void clear() {
    plist.removeAll();
    proj = new Project(this);
  }

  void new_actionPerformed(ActionEvent e) {
    clear();
    proj.newp();
  }

  void open_actionPerformed(ActionEvent e) {
    clear();
    proj.load(plist);
  }

  void fileExit_actionPerformed(ActionEvent e) {
    quit();
  }

  void helpAbout_actionPerformed(ActionEvent e) {
    new StringRequest(this, "AardEd (april 2000) by Wouter van Oortmerssen", null, false);
  }

  void saveall_actionPerformed(ActionEvent e) {
    proj.saveall();
  }

  void addfile_actionPerformed(ActionEvent e) {
    proj.loadadd(plist);
  }

  void addbag_actionPerformed(ActionEvent e) {
    proj.newbag(plist);
  }

  void addmodule_actionPerformed(ActionEvent e) {
    proj.newmodule(plist);
  }

  void this_windowClosing(WindowEvent e) {
    quit();
  }

  void menuItem9_actionPerformed(ActionEvent e) {} //class

  void menuItem10_actionPerformed(ActionEvent e) {} //jar
}

class MainWin_menuFileExit_ActionAdapter implements ActionListener {
  MainWin adaptee;

  MainWin_menuFileExit_ActionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.fileExit_actionPerformed(e);
  }
}

class MainWin_menuHelpAbout_ActionAdapter implements ActionListener {
  MainWin adaptee;

  MainWin_menuHelpAbout_ActionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.helpAbout_actionPerformed(e);
  }
}

class MainWin_plist_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_plist_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.plist_actionPerformed(e);
  }
}

class MainWin_menuItem1_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem1_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.new_actionPerformed(e);
  }
}

class MainWin_menuItem2_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem2_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.open_actionPerformed(e);
  }
}

class MainWin_menuItem3_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem3_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.saveall_actionPerformed(e);
  }
}

class MainWin_menuItem4_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem4_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.delete_actionPerformed(e);
  }
}

class MainWin_this_windowAdapter extends java.awt.event.WindowAdapter {
  MainWin adaptee;

  MainWin_this_windowAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void windowClosing(WindowEvent e) {
    adaptee.this_windowClosing(e);
  }
}

class MainWin_menuItem5_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem5_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.rename_actionPerformed(e);
  }
}

class MainWin_addbag_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_addbag_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.addbag_actionPerformed(e);
  }
}

class MainWin_addmodule_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_addmodule_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.addmodule_actionPerformed(e);
  }
}

class MainWin_addfile_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_addfile_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.addfile_actionPerformed(e);
  }
}

class MainWin_menuItem9_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem9_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.menuItem9_actionPerformed(e);
  }
}

class MainWin_menuItem10_actionAdapter implements java.awt.event.ActionListener {
  MainWin adaptee;

  MainWin_menuItem10_actionAdapter(MainWin adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.menuItem10_actionPerformed(e);
  }
}
