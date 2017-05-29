import jas.*;
import java.io.*;
import java.rmi.*;
import java.util.*;
import r.*;

class Compiler extends Codegen {
  static final String classname = "CompiledAardappel";
  static final String supername = "r/AardappelRuntime";
  public MainWin mw;
  Hashtable syms = new Hashtable();
  Vector symtodo = new Vector();
  int ids = AardappelRuntime.idtreestart;
  Vector idsv = new Vector();
  int curloc = 0;
  int curbagt = 0;
  int biggestyet = -1;
  Vector bagtsyms = new Vector();
  ClassEnv cl;
  CodeAccess ca = null;
  Class cls = null;
  static Compiler self = null;
  boolean debug = false;
  byte[] cldata;

  int getlocnum() {
    return ++curloc;
  }

  int getbagtnum() {
    return curbagt++;
  }

  void setbagtsym(Sym s, int n) {
    if (n > biggestyet) {
      biggestyet = n;
      bagtsyms.setSize(n + 1);
    }
    bagtsyms.setElementAt(s, n);
  }

  Compiler(Code targetexp, MainWin w) {
    self = this;
    mw = w;
    Sym.staticinit(classname, supername);
    checksym("true", 0, 0); // always the first 4
    checksym("false", 0, 0);
    checksym("nil", 0, 0);
    checksym("void", 0, 0);
    builtin("-", 2);
    builtin("*", 2);
    builtin("+", 2);
    builtin("/", 2);
    builtin("mod", 2);
    builtin("<", 2);
    builtin(">", 2);
    builtin("=", 2);
    builtin("odd", 1);
    builtin("real", 1);
    builtin("int", 1);
    builtin("gfxwindow", 2);
    builtin("plot", 5);
    builtin("flatten", 1);
    Rules r = new Rules();
    r.add(new Tree(new Atom("aarded_main")), targetexp);
    addrules(r, "mainexp", 0, null);
  }

  Vector addrules(Rules r, String name, int localnum, String localspec) {
    Vector v = r.allexps();
    Vector symsadded = new Vector();
    for (int a = 0; a < v.size(); a += 2) {
      Code p = (Code) v.elementAt(a);
      Code e = (Code) v.elementAt(a + 1);
      Tree t = p.headtree();
      if (t == null) throw new CompError("top level of a rule should be a tree");
      if (t.isexample) throw new CompError("catch-all rule not allowed");
      int narg = t.nchildren();
      String idn = t.head().name();
      if (narg > AardappelRuntime.maxchildren)
        throw new CompError("current max number of children exceeded for " + idn);
      if (localspec != null) idn += "__" + localspec + localnum;
      Sym s = checksym(idn, narg, localnum);
      s.attach(p, e);
      symsadded.addElement(s);
    }
    return symsadded;
  }

  String makeident(String s) {
    String t = "";
    for (int a = 0; a < s.length(); a++) {
      char c = s.charAt(a);
      t +=
          ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || (c >= '0' && c <= '9'))
              ? "" + c
              : ("" + ((int) c));
    }
    return t;
  }

  void builtin(String id, int narg) {
    Sym s = checksym(id, narg, 0);
    s.builtin = "builtin_" + s.id;
  }

  Sym findsym(String id, int narg) {
    return (Sym) syms.get(id + "_" + narg);
  }

  Sym checksym(String id, int narg, int locnum) {
    Sym s;
    String name = id + "_" + narg;
    if ((s = (Sym) syms.get(name)) == null) {
      String methodname = "rule_" + makeident(name);
      s = new Sym(methodname, id, ids++, narg, this, locnum);
      idsv.addElement(s);
      syms.put(name, s);
      symtodo.addElement(s);
    }
    return s;
  }

  void genbagtsyms(ClassEnv cl) throws jasError {
    startmethod();
    geniload(1);
    Label labs[] = new Label[bagtsyms.size()];
    Label def = new Label("bagtdef");
    for (int a = 0; a < bagtsyms.size(); a++) {
      Sym s = (Sym) bagtsyms.elementAt(a);
      if (s == null) throw new CompError("assertion: null sym");
      labs[a] = new Label("bagt__" + a);
    }
    gentable(0, bagtsyms.size() - 1, def, labs);
    for (int a = 0; a < bagtsyms.size(); a++) {
      Sym s = (Sym) bagtsyms.elementAt(a);
      genlab(labs[a]);
      Vector fv = s.fvars;
      for (int i = 0; i < fv.size(); i++) {
        genaload(2);
        genval(i);
        genaaload();
      }
      genjsrstat(s.methodcp());
      genareturn();
    }
    genlab(def);
    geniload(1);
    genjsrstat(new MethodCP(supername, "interntab", "(I)V"));
    gennull();
    genareturn();
    endmethod(100, 3, ACC_PUBLIC, "runcode", "(I[Lr/Rval;)Lr/Rval;", cl);
  }

  void compilerest() throws jasError {
    int n;
    while ((n = symtodo.size()) > 0) {
      Sym s = (Sym) symtodo.elementAt(n - 1);
      symtodo.removeElementAt(n - 1);
      s.gen(cl, debug);
    }
  }

  void compile(boolean standalone, Treeview tv) {
    try {
      debug = (tv != null);
      cl = new ClassEnv();
      Sym.curpatvarid++;
      compilerest();
      genbagtsyms(cl);
      addinit(cl, ids);
      addrun(cl);
      addrunrule(cl);
      addmain(cl);
      cl.setClassAccess((short) (ACC_PUBLIC | ACC_SUPER));
      cl.setClass(new ClassCP(classname));
      cl.setSuperClass(new ClassCP(supername));
      cl.setSource("aarded.bag");
      ByteArrayOutputStream bytestr = new ByteArrayOutputStream();
      cl.write(new DataOutputStream(bytestr));
      cldata = bytestr.toByteArray();
      if (standalone || true) {
        FileOutputStream fo = new FileOutputStream(classname + ".class");
        fo.write(cldata);
        fo.close();
        mw.msg("written to " + classname + ".class");
      }
      if (!standalone) {
        RuntimeLoader loader = new RuntimeLoader(cldata);
        ca = new CodeAccess(idsv, AardappelRuntime.idtreestart);
        if (tv != null) {
          AardEdDebug d = new AardEdDebug();
          d.dbg = d;
          d.tv = tv;
          d.ca = ca;
          d.syms = idsv;
        }
        cls = loader.loadClass();
        mw.msg("running...");
        Thread t =
            new Thread() {
              public void run() {
                try {
                  AardappelRuntime ar = (AardappelRuntime) self.cls.newInstance();
                  long start = System.currentTimeMillis();
                  String[] servers = new String[0]; // passing servers here will turn AardEd into
                  ar.run(servers, self.cldata); // an Applet, courtesy of RMISecurityManager
                  mw.msg("done [" + (System.currentTimeMillis() - start) / 1000.0 + " seconds].");
                  Code c = ca.code(ar.finalbag);
                  ar.cleanup();
                  if (c != null) new Treeview(mw, "evaluation result", c);
                } catch (IllegalAccessException e) {
                  mw.msg("compile: class not valid");
                } catch (InstantiationException e) {
                  mw.msg("compile: couldn't launch code");
                } catch (Exception e) {
                  mw.msg("compile: general exception: " + e.getMessage());
                  e.printStackTrace();
                }
              }
            };
        t.start();
      }
    } catch (jasError e) {
      mw.msg("compile: jas: problem generating bytecode: " + e);
    } catch (IOException e) {
      mw.msg("compile: io problem");
    } catch (CompError e) {
      mw.msg("compile: " + e.msg);
    } catch (VerifyError e) {
      mw.msg("compile internal error: JVM VerifyError");
    } catch (Exception e) {
      mw.msg("compile: exception: " + e);
      //e.printStackTrace();
    }
  }

  void addinit(ClassEnv cl, int maxid) throws jasError {
    startmethod();
    genaload(0);
    genval(maxid);
    genjsrnonv(new MethodCP(supername, "<init>", "(I)V"));
    gengetstat(new FieldCP("java/lang/System", "out", "Ljava/io/PrintStream;"));
    genldc(new StringCP("Compiled Aardappel Runtime v0.1 initialized"));
    genjsrvirt(new MethodCP("java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    genreturn();
    endmethod(10, 1, ACC_PUBLIC, "<init>", "()V", cl);
  }

  void addrun(ClassEnv cl) throws jasError {
    startmethod();
    genaload(0);
    genaload(1);
    genaload(2);
    genjsrvirt(new MethodCP(supername, "start", "([Ljava/lang/String;[B)Lr/Rval;"));
    gendup();
    genaload(0);
    genswap();
    genputfield(new FieldCP(supername, "finalbag", "Lr/Rval;"));
    genjsrstat(new MethodCP(supername, "debug", "(Lr/Rval;)V"));
    genreturn();
    endmethod(10, 3, ACC_PUBLIC, "run", "([Ljava/lang/String;[B)V", cl);
  }

  void addrunrule(ClassEnv cl) throws jasError {
    startmethod();
    genjsrstat(new MethodCP(classname, "rule_aarded_main_0", "()Lr/Rval;"));
    genareturn();
    endmethod(10, 2, ACC_PUBLIC, "runrule", "()Lr/Rval;", cl);
  }

  void addmain(ClassEnv cl) throws jasError {
    startmethod();
    gennew(new ClassCP(classname));
    gendup();
    genjsrnonv(new MethodCP(classname, "<init>", "()V"));
    gendup();
    genaload(0);
    genjsrvirt(new MethodCP(supername, "loadclass", "([Ljava/lang/String;)[B"));
    genaload(0);
    genswap();
    genjsrvirt(new MethodCP(classname, "run", "([Ljava/lang/String;[B)V"));
    genjsrstat(new MethodCP(supername, "cleanup", "()V"));
    genreturn();
    endmethod(10, 1, ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", cl);
  }
}

class AardEdDebug extends Debug {
  static Debug dbg;
  Treeview tv;
  CodeAccess ca;
  Vector syms;
  Vector v = new Vector();

  public void arg(Rval a) {
    v.addElement(a);
  }

  public void tree(int id) {
    Tree t = new Tree(new Atom(((Sym) syms.elementAt(id - AardappelRuntime.idtreestart)).id2));
    for (int i = 0; i < v.size(); i++) t.add(ca.code((Rval) v.elementAt(i)));
    tv.tvc = t;
    tv.refreshtree(false);
    v.removeAllElements();
  }

  public static Debug getdbg() {
    return dbg;
  }
}

class CodeAccess implements Access {
  Vector syms;
  int offs;
  Rval culprit = null;

  CodeAccess(Vector v, int o) {
    syms = v;
    offs = o;
  }

  Code code(Rval r) {
    Code c = (Code) r.code(this);
    if (culprit == r) c.warnval = 1;
    return c;
  }

  String n(Rval a) {
    Sym s = (Sym) syms.elementAt(a.id - offs);
    if (!s.occursaspat()) culprit = a;
    return s.id2;
  }

  public Object rv(Rval a) {
    return new Atom(n(a));
  }

  public Object rv(Rint a) {
    return new Int(a.v);
  }

  public Object rv(Rreal a) {
    return new Real(a.v);
  }

  public Object rv(Rbag a) {
    Bag b = new Bag();
    try {
      Rval nf[] = a.nfarray();
      for (int i = 0; i < nf.length; i++) b.add(code(nf[i]));
    } catch (RemoteException e) {
      System.out.println("codeaccess: remote exception: " + e);
    }
    return b;
  }

  public Object rv(Rtree1 a) {
    Code t = new Tree(new Atom(n(a)), code(a.c1));
    return t;
  }

  public Object rv(Rtree2 a) {
    return ((Tree) rv((Rtree1) a)).add(code(a.c2));
  }

  public Object rv(Rtree3 a) {
    return ((Tree) rv((Rtree2) a)).add(code(a.c3));
  }

  public Object rv(Rtree4 a) {
    return ((Tree) rv((Rtree3) a)).add(code(a.c4));
  }

  public Object rv(Rtree5 a) {
    return ((Tree) rv((Rtree4) a)).add(code(a.c5));
  }

  public Object rv(Rtree6 a) {
    return ((Tree) rv((Rtree5) a)).add(code(a.c6));
  }

  public Object rv(Rtree7 a) {
    return ((Tree) rv((Rtree6) a)).add(code(a.c7));
  }

  public Object rv(Rtree8 a) {
    return ((Tree) rv((Rtree7) a)).add(code(a.c8));
  }

  public Object rv(Rtree9 a) {
    return ((Tree) rv((Rtree8) a)).add(code(a.c9));
  }

  public Object rv(Rtree10 a) {
    return ((Tree) rv((Rtree9) a)).add(code(a.c10));
  }

  public Object rv(Rtree11 a) {
    return ((Tree) rv((Rtree10) a)).add(code(a.c11));
  }

  public Object rv(Rtree12 a) {
    return ((Tree) rv((Rtree11) a)).add(code(a.c12));
  }
}
