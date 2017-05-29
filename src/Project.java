import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class ProjectItem {
  String name;

  Code code() {
    return null;
  }

  Rules getrules() {
    return null;
  }
}

class TopBag extends ProjectItem {
  Bag b = new Bag();

  TopBag(int n) {
    name = "untitled" + n + ".bag";
    gendummy();
  }

  TopBag(String s) {
    name = s;
    gendummy();
  }

  TopBag(String s, Bag x) {
    name = s;
    b = x;
  }

  Code code() {
    return b;
  }

  void gendummy() {
    b.add(new Tree(new Atom("fac"), new Int(5)));
  }
}

class Module extends ProjectItem {
  Rules r = new Rules();

  Module(int n) {
    name = "untitled" + n + ".m";
    gendummy();
  }

  Module(String s) {
    name = s;
    gendummy();
  }

  Module(String s, Rules rs) {
    name = s;
    r = rs;
  }

  Rules getrules() {
    return r;
  }

  Code code() {
    return r;
  }

  void gendummy() {
    r.add(new Tree(new Atom("fac"), new Int(1)), new Int(1));
    Code sh5 = (new Int(5)).example();
    sh5.inc();
    sh5.inc();
    r.add(
        new Tree(new Atom("fac"), sh5),
        new Tree(
            new Atom("*"),
            sh5,
            new Tree(new Atom("fac"), new Tree(new Atom("-"), sh5, new Int(1)))));
  }
}

class Project {
  Hashtable elems = new Hashtable();
  File prjname = new File("project.ap");
  int newcount = 0;
  MainWin mw;
  Compiler ac = null;

  Project(MainWin mainw) {
    mw = mainw;
  }

  ProjectItem get(String s) {
    return (ProjectItem) elems.get(s);
  }

  void addb(java.awt.List l, String s) {
    add(l, new TopBag(s), s);
  }

  void addm(java.awt.List l, String s) {
    add(l, new Module(s), s);
  }

  void add(java.awt.List plist, ProjectItem x, String s) {
    plist.add(s);
    elems.put(s, x);
  }

  void newbag(java.awt.List l) {
    newitem(l, new TopBag(newcount++));
  }

  void newmodule(java.awt.List l) {
    newitem(l, new Module(newcount++));
  }

  void remove(String s) {
    elems.remove(s);
  }

  void newitem(java.awt.List l, ProjectItem p) {
    p.name = rename(p.name);
    elems.put(p.name, p);
    l.add(p.name);
  }

  void compile(Code target, boolean standalone, Treeview tv) {
    mw.msg("compiling...");
    try {
      ac = new Compiler(target, mw);
      for (Enumeration e = elems.elements(); e.hasMoreElements(); ) {
        ProjectItem p = ((ProjectItem) e.nextElement());
        Rules r;
        if ((r = p.getrules()) != null) ac.addrules(r, p.name, 0, null);
      }
    } catch (CompError e) {
      mw.msg("compile startup: " + e.msg);
      return;
    }
    ac.compile(standalone, tv);
  }

  String rename(String s) {
    String nn = (new StringRequest(mw, "please enter new name:", s, false)).result;
    if (nn != null) {
      ProjectItem p = get(s);
      if (p != null) {
        remove(s);
        elems.put(nn, p);
        p.name = nn;
      }
      s = nn;
    }
    return s;
  }

  Code newexample(String s) {
    Code sofar = new Tree(new Atom(s)); // can we do better?
    for (Enumeration e = elems.elements(); e.hasMoreElements(); ) {
      Rules r = ((ProjectItem) e.nextElement()).getrules();
      if (r != null) sofar = r.bestmatch(s, sofar);
    }
    return sofar.copy();
  }

  void saveall() {
    for (Enumeration e = elems.elements(); e.hasMoreElements(); ) {
      ProjectItem p = ((ProjectItem) e.nextElement());
      File item = new File(prjname.getParent(), p.name);
      try {
        FileOutputStream f = new FileOutputStream(item);
        ObjectOutput s = new ObjectOutputStream(f);
        s.writeObject(p.code());
        s.flush();
        s.close();
        f.close();
        mw.msg("wrote " + p.name);
      } catch (IOException i) {
        new StringRequest(mw, "saving " + item + " unsuccesful", null, false);
      }
    }
    try {
      FileOutputStream prj = new FileOutputStream(prjname);
      DataOutputStream dos = new DataOutputStream(prj);
      dos.writeBytes("/* Aardappel project file */\n");
      for (Enumeration e = elems.elements(); e.hasMoreElements(); ) {
        ProjectItem p = ((ProjectItem) e.nextElement());
        dos.writeBytes("file \"" + p.name + "\";\n");
      }
      dos.flush();
      dos.close();
      prj.close();
      mw.msg("wrote project file " + prjname);
    } catch (IOException i) {
      new StringRequest(mw, "saving project file " + prjname + " unsuccesful", null, false);
    }
  }

  void loadadd(java.awt.List plist) {
    FileDialog fd = new FileDialog(mw, "select a .bag or .m to add", FileDialog.LOAD);
    fd.setDirectory(prjname.getParent());
    fd.show();
    String name = fd.getFile();
    if (name == null) return;
    loadadd(plist, new File(fd.getDirectory(), name), name);
  }

  void loadadd(java.awt.List plist, File fn, String name) {
    mw.msg("adding " + name);
    Code o = null;
    try {
      FileInputStream in = new FileInputStream(fn);
      ObjectInputStream s = new ObjectInputStream(in);
      o = (Code) s.readObject();
      s.close();
      in.close();
      try {
        add(plist, new Module(name, (Rules) o), name);
      } catch (ClassCastException cc1) {
        try {
          add(plist, new TopBag(name, (Bag) o), name);
        } catch (ClassCastException cc2) {
          new StringRequest(mw, name + " doesn't contain rules or a bag", null, false);
        }
      }
    } catch (IOException i) {
      new StringRequest(mw, "couldn't load " + fn, null, false);
    } catch (ClassNotFoundException nf) {
      new StringRequest(mw, fn + " is not a valid Aardappel file", null, false);
    }
  }

  void loadproject(File name, java.awt.List plist) {
    try {
      Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
      StreamTokenizer st = new StreamTokenizer(in);
      st.slashStarComments(true);
      st.nextToken();
      while (st.ttype != StreamTokenizer.TT_EOF) {
        if (st.ttype != StreamTokenizer.TT_WORD) {
          mw.msg("project file parse error: missing directive");
          break;
        } else {
          if (st.sval.compareTo("file") == 0) {
            st.nextToken();
            if (st.ttype != '"') {
              mw.msg("project file parse error: missing filename");
              break;
            }
            loadadd(plist, new File(name.getParent(), st.sval), st.sval);
            st.nextToken();
          } else {
            mw.msg("project file parse error: unrecognized directive");
          }
        }
        if (st.ttype != ';') mw.msg("project file parse error: missing ';'");
        st.nextToken();
      }
      in.close();
      setname(name);
    } catch (IOException i) {
      mw.msg("couldn't load project file " + name);
    }
  }

  void setname(File name) {
    mw.lastproject = name.getAbsolutePath();
    prjname = new File(mw.lastproject);
    mw.saveprefs();
  }

  void newp() {
    FileDialog fd = new FileDialog(mw, "Select a location for the project", FileDialog.SAVE);
    fd.setDirectory(prjname.getParent());
    fd.setFile("project.ap");
    fd.show();
    String name = fd.getFile();
    if (name == null) return;
    prjname = new File(fd.getDirectory(), name);
    saveall();
    setname(prjname);
  }

  void load(java.awt.List plist) {
    FileDialog fd = new FileDialog(mw, "Select a project file to load", FileDialog.LOAD);
    //File adir = new File(prjname.getParent());
    //System.out.println(adir);
    //String pdir = adir.getParent();
    //fd.setDirectory((new File(pdir,"projects")).getPath());
    fd.setDirectory(prjname.getParent());
    fd.setFile("project.ap");
    fd.show();
    String name = fd.getFile();
    if (name == null) return;
    prjname = new File(fd.getDirectory(), name);
    loadproject(prjname, plist);
  }
}
