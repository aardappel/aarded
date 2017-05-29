import jas.*;
import java.util.*;
import r.*;

class Sym extends Codegen {
  public String id, id2, builtin = null;
  int idn;
  int narg;
  int numfreevars = 0;
  Vector rs = new Vector();
  Vector vars = new Vector();
  Vector fvars = new Vector();
  Compiler comp;
  MethodCP thiscp = null;
  Stack locs = new Stack();
  int locnum = 0;
  boolean isdata = false;
  static int labn = 0;
  static int curpatvarid = 1;
  static String sigs[];
  static ClassCP classcps[];
  static FieldCP fieldid, fieldv, fieldc[];
  static String classname, supername;
  static ClassCP rintcp = new ClassCP("r/Rint");
  static ClassCP rbagcp = new ClassCP("r/Rbag");
  static MethodCP rbagmcp = new MethodCP("r/AardappelRuntime", "newbag", "()Lr/Rbag;");
  static MethodCP addnormal = new MethodCP("r/Rbag", "addnormal", "(Lr/Rval;)V");
  static MethodCP waitnormal = new MethodCP("r/Rbag", "waitnormalform", "()V");
  static MethodCP reject = new MethodCP("r/Rbag", "reject", "([Lr/Rval;I)V");
  static MethodCP unreject = new MethodCP("r/Rbag", "unreject", "([Lr/Rval;)V");
  static MethodCP grab = new MethodCP("r/Rbag", "grab", "([I)[Lr/Rval;");
  static MethodCP nfarray = new MethodCP("r/Rbag", "nfarray", "()[Lr/Rval;");
  static MethodCP getparent = new MethodCP("r/AardappelRuntime", "getparent", "()Lr/Rbag;");
  static MethodCP equal =
      new MethodCP("r/AardappelRuntime", "builtin_rule_61_2", "(Lr/Rval;Lr/Rval;)Lr/Rval;");
  static MethodCP newthread =
      new MethodCP("r/AardappelRuntime", "newthread", "(Lr/Rbag;I[Lr/Rval;)V");
  static MethodCP dbgtree = new MethodCP("r/AardappelRuntime", "debugtree", "(I)V");
  static MethodCP dbgarg = new MethodCP("r/AardappelRuntime", "debugarg", "(Lr/Rval;)V");
  static MethodCP rintmcp, rrealmcp;
  static MethodCP rclosmcp;
  static MethodCP methodcps[];
  static String applysigs[];

  class SRule {
    Code p;
    Code e;
  }

  Label newlab() {
    return new Label("lab" + labn++);
  }

  static void error(String s) {
    throw new CompError(s);
  }

  static void intern(String s) {
    error("internal error: " + s);
  }

  void warn(String s) {
    comp.mw.msg(s);
  }

  static void ni() {
    intern("code generation for this construct not implemented");
  }

  boolean occursaspat() {
    return isdata;
  }

  MethodCP methodcp() {
    return thiscp;
  }

  void setthiscp() {
    numfreevars = fvars.size();
    thiscp = new MethodCP(classname, id, sigs[narg + numfreevars]);
  }

  Sym(String i, String i2, int in, int na, Compiler c, int ln) {
    id = i;
    id2 = i2;
    idn = in;
    narg = na;
    comp = c;
    locnum = ln;
  }

  static void staticinit(String cn, String sn) { // called before anything else
    classname = cn;
    supername = sn;
    sigs = new String[AardappelRuntime.maxchildren];
    classcps = new ClassCP[AardappelRuntime.maxchildren + 1];
    fieldc = new FieldCP[AardappelRuntime.maxchildren];
    methodcps = new MethodCP[AardappelRuntime.maxchildren + 1];
    applysigs = new String[AardappelRuntime.maxchildren + 1];
    String sofar = "";
    String cl;
    for (int a = 0; a < AardappelRuntime.maxchildren; a++) {
      sigs[a] = "(" + sofar + ")Lr/Rval;";
      sofar += "Lr/Rval;";
      classcps[a + 1] = new ClassCP(cl = "r/Rtree" + (a + 1));
      fieldc[a] = new FieldCP("r/Rtree" + (a + 1), "c" + (a + 1), "Lr/Rval;");
      methodcps[a + 1] = new MethodCP(cl, "<init>", "(I" + sofar + ")V");
    }
    classcps[0] = new ClassCP(cl = "r/Rval");
    methodcps[0] = new MethodCP(cl, "<init>", "(I)V");
    fieldid = new FieldCP("r/Rval", "id", "I");
    fieldv = new FieldCP("r/Rint", "v", "I");
    rintmcp = new MethodCP(supername, "rint", "(I)Lr/Rint;");
    rrealmcp = new MethodCP(supername, "rreal", "(D)Lr/Rreal;");
    rclosmcp = new MethodCP(supername, "rclosure", "([Lr/Rval;I)Lr/Rclosure;");
  }

  void zapvars() {
    vars.setSize(0);
  }

  int registervar(Code c) {
    vars.addElement(c);
    return vars.size() - 1 + numfreevars + narg;
  }

  int lookupvar(Code c) {
    for (int a = 0; a < vars.size(); a++) if (vars.elementAt(a) == c) return a + numfreevars + narg;
    return -1;
  }

  int registerfv(Code c) {
    int n = findfv(c);
    if (n >= 0) return n;
    fvars.addElement(c);
    return fvars.size() - 1 + narg;
  }

  int findfv(Code c) {
    if (locnum == 0) {
      //intern("free variables in global rule");
    }
    for (int a = 0; a < fvars.size(); a++) if (fvars.elementAt(a) == c) return a + narg;
    return -1;
  }

  void attach(Code p, Code e) {
    int a = 0;
    for (; a < rs.size(); a++) {
      if (p.speccomp(((SRule) rs.elementAt(a)).p) > 0) break;
    }
    SRule r = new SRule();
    r.p = p;
    r.e = e;
    rs.insertElementAt(r, a);
  }

  void pass1() throws jasError {
    if (methodcp() == null) {
      for (int a = 0; a < rs.size(); a++) {
        SRule r = (SRule) rs.elementAt(a);
        zapvars();
        prepatvar(r.p);
        preexpvar(r.e);
      }
      setthiscp();
    }
  }

  void gen(ClassEnv cl, boolean debug) throws jasError {
    pass1();
    startmethod();
    if (debug) {
      for (int a = 0; a < narg; a++) {
        genaload(a);
        genjsrstat(dbgarg);
      }
      genval(idn);
      genjsrstat(dbgtree);
    }
    if (builtin != null) {
      for (int a = 0; a < narg; a++) genaload(a);
      genjsrstat(new MethodCP(supername, builtin, sigs[narg]));
      Label l = newlab();
      gendup();
      genisnull(l);
      genareturn();
      genlab(l);
      gendrop();
    }
    boolean catchall = false;
    int maxvars = 0;
    Label nfl = newlab();
    boolean lindaused = false;
    for (int a = 0; a < rs.size(); a++) {
      start_pm();
      zapvars();
      SRule r = (SRule) rs.elementAt(a);
      Tree t = r.p.headtree();
      catchall = true;
      for (int b = 0; b < t.nchildren(); b++) {
        Code c = (Code) t.child(b);
        genaload(b);
        pm_level++;
        catchall &= genpatvar(c);
      }
      lindaused |= genlindain(r.p.headin(), nfl);
      genexpvar(r.e);
      if (catchall & !lindaused) {
        if (a != rs.size() - 1) warn("warning: dead rule in " + id);
        a = rs.size();
      }
      genareturn();
      finish_pm();
      if (vars.size() > maxvars) maxvars = vars.size();
    }
    if (!catchall || lindaused) {
      genlab(nfl);
      gennew(classcps[narg]);
      gendup();
      genval(idn);
      for (int a = 0; a < narg; a++) genaload(a);
      genjsrspec(methodcps[narg]);
      genareturn();
    }
    endmethod(
        20,
        narg + numfreevars + maxvars,
        ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
        id,
        sigs[narg + numfreevars],
        cl);
  }

  boolean genlindain(Bag inop, Label nfl) throws jasError {
    if (inop == null) return false;
    Label il = newlab();
    Label nn = newlab();
    Label inok = newlab();
    genval(inop.nchildren());
    geniarray();
    for (int x = 0; x < inop.nchildren(); x++) {
      gendup();
      genval(x);
      genval(getid(inop.child(x).headtree()));
      geniastore();
    }
    genlab(il); // ( idn[] )
    gendup();
    genjsrstat(getparent);
    gendupx1();
    genswap();
    genjsrvirt(grab);
    gendup();
    genisnnull(nn);
    gendrop2();
    gendrop();
    gengoto(nfl);
    genlab(nn); // ( idn[] bag rval[] )
    bump1();
    start_pm();
    for (int x = 0; x < inop.nchildren(); x++) {
      gendup();
      genval(x);
      gendupx1();
      genaaload();
      pm_level++;
      genpatvar(inop.child(x));
      gendrop();
    }
    genjsrvirt(unreject);
    gendrop();
    gengoto(inok);
    finish_pm(); // ( idn[] bag rval[] x )
    genjsrvirt(reject);
    gengoto(il);
    unbump();
    genlab(inok);
    return true;
  }

  int getid(Tree t) {
    if (t == null) error("Linda IN currently only implemented for trees");
    Sym s = comp.checksym(t.head().name(), t.nchildren(), 0);
    return s.idn;
  }

  /*----------PM-------------*/

  final int maxpatrec = 16;
  Label labs[] = new Label[maxpatrec], labs2[];
  int pm_level, pm_level2;

  void start_pm() {
    pm_level = 0;
    for (int i = 0; i < maxpatrec; i++) labs[i] = null;
  }

  void bump1() {
    pm_level2 = pm_level;
    labs2 = labs;
    labs = new Label[maxpatrec];
  }

  void unbump() {
    pm_level = pm_level2;
    labs = labs2;
  }

  Label pm_getlab() {
    if (pm_level >= maxpatrec) error("pattern match recursion depth exceeded");
    return (labs[pm_level] == null) ? (labs[pm_level] = newlab()) : labs[pm_level];
  }

  void finish_pm() throws jasError {
    boolean start = false;
    for (int i = maxpatrec - 1; i >= 0; i--) {
      if (labs[i] != null) {
        genlab(labs[i]);
        start = true;
      }
      if (start && i != 0) gendrop();
    }
  }

  /*----------PAT-PRE-------------*/

  void prepatvar(Code c) throws jasError {
    if (c.isexample) {
      if (lookupvar(c) == -1) {
        registervar(c); // or instead if its from parent scope then make it a fv
      }
    } else {
      c.genpatpre(this);
    }
  }

  void genpatpre(Tree t) throws jasError {
    prepatvar(t.h);
    for (int x = 0; x < t.nchildren(); x++) prepatvar(t.child(x));
  }

  void genpatpre(Bag b) throws jasError {
    if (b.io != null) prepatvar(b.io);
    for (int x = 0; x < b.nchildren(); x++) prepatvar(b.child(x));
  }

  void genpatpre(Rules r) throws jasError {
    if (r.e != null) prepatvar(r.e);
  }

  void genpatpre(Atom a) throws jasError {}

  void genpatpre(Int i) throws jasError {}

  void genpatpre(Real r) throws jasError {}

  /*----------EXP-PRE-------------*/

  void preexpvar(Code c) throws jasError {
    if (c.isexample) {
      if (lookupvar(c) == -1) registerfv(c);
    } else {
      c.genexppre(this);
    }
  }

  void genexppre(Tree t) throws jasError {
    preexpvar(t.h);
    for (int x = 0; x < t.nchildren(); x++) preexpvar(t.child(x));
  }

  void genexppre(Bag b) throws jasError {
    if (b.io != null) preexpvar(b.io);
    for (int x = 0; x < b.nchildren(); x++) preexpvar(b.child(x));
  }

  void genexppre(Rules r) throws jasError {
    if (r.e != null) preexpvar(r.e);
    r.locn = comp.getlocnum();
    Vector v = comp.addrules(r, null, r.locn, id);
    for (int i = 0; i < v.size(); i++) {
      Sym s = (Sym) v.elementAt(i);
      s.pass1();
      Vector fv = s.fvars;
      for (int j = 0; j < fv.size(); j++) {
        Code c = (Code) fv.elementAt(j);
        if (lookupvar(c) == -1) registerfv(c);
      }
    }
  }

  void genexppre(Atom a) throws jasError {}

  void genexppre(Int i) throws jasError {}

  void genexppre(Real r) throws jasError {}

  /*----------PAT-------------*/

  boolean genpatvar(Code c) throws jasError {
    boolean isvar = c.isexample;
    if (c.isexample) {
      int varn = lookupvar(c);
      if (c.patvar != curpatvarid) {
        if (varn != -1) intern("var occurs twice? " + c);
        c.patvar = curpatvarid;
        genastore(registervar(c));
        pm_level--;
      } else {
        //warn("varpat: "+c);
        if (varn == -1) varn = registerfv(c);
        genaload(varn);
        genjsrstat(equal);
        gengetfield(fieldid);
        pm_level--;
        gencmpne(AardappelRuntime.idtrue, pm_getlab());
        isvar = false;
      }
    } else {
      c.genpat(this);
    }
    return isvar;
  }

  void genpat(Tree t) throws jasError {
    Sym s = comp.checksym(t.head().name(), t.nchildren(), 0);
    s.isdata = true;
    isval(s.idn, classcps[t.nchildren()]);
    for (int b = 0; b < t.nchildren(); b++) {
      gendup();
      gengetfield(fieldc[b]);
      pm_level++;
      genpatvar((Code) t.child(b));
    }
    gendrop();
    pm_level--;
  }

  void genpat(Int i) throws jasError {
    isval(AardappelRuntime.idint, rintcp);
    gengetfield(fieldv);
    pm_level--;
    gencmpne(i.i, pm_getlab());
  }

  void genpat(Atom a) throws jasError {
    genpat(new Tree(a));
  }

  void genpat(Real r) throws jasError {
    error("pattern matching reals is inacurate, use >");
  }

  void genpat(Bag b) throws jasError {
    if (b.headtree() != null) error("Currently Linda IN only supported at top level");
    isval(AardappelRuntime.idbag, rbagcp);
    genjsrvirt(nfarray);
    for (int i = 0; i < b.nchildren(); i++) { // ( ar )
      pm_level++;
      Label fail = pm_getlab();
      pm_level--;
      bump1();
      start_pm();
      gendup();
      genalen();
      Label bpml = newlab();
      genlab(bpml); // ( ar idx )
      gendup();
      genis0(fail);
      genval(1);
      genisub();
      gendup2();
      genaaload();
      gendup();
      Label nn = newlab();
      genisnnull(nn);
      gendrop();
      gengoto(bpml);
      genlab(nn);
      pm_level++; // ( ar idx-1 rval )
      genpatvar(b.child(i));
      if (i != b.nchildren() - 1) {
        gendup2();
        gennull();
        genaastore();
      }
      Label bpmn = newlab();
      gengoto(bpmn);
      finish_pm();
      gengoto(bpml);
      genlab(bpmn); // ar idx
      unbump();
      gendrop();
    }
    gendrop();
    pm_level--;
  }

  void genpat(Rules r) throws jasError {
    error("cannot pattern match local rules [" + id + "]");
  }

  /*-----------EXP------------*/

  void genexpvar(Code c) throws jasError {
    if (c.isexample) {
      int varn = lookupvar(c);
      if (varn == -1) varn = registerfv(c);
      genaload(varn);
    } else {
      if (c.refc > 1) warn("shared expression computed twice");
      c.genexp(this);
    }
  }

  void genexp(Tree t) throws jasError {
    if (t.h.isexample) error("example used as tree head");
    Sym s = genexp(t, locs);
    if (s == null) s = comp.checksym(t.head().name(), t.nchildren(), 0);
    if (s.methodcp() == null) comp.compilerest();
    for (int b = 0; b < t.nchildren(); b++) {
      genexpvar((Code) t.child(b));
    }
    Vector fv = s.fvars;
    for (int a = 0; a < fv.size(); a++) { // add free vars, if any
      genaload(genexplookup((Code) fv.elementAt(a), t));
    }
    genjsrstat(s.methodcp());
  }

  int genexplookup(Code c, Code context) {
    int vn = lookupvar(c);
    if (vn == -1) {
      vn = findfv(c);
      if (vn == -1)
        intern(
            "call: couldn't find code for free variable in local scope: "
                + c
                + " in context "
                + context);
    }
    return vn;
  }

  Sym genexp(Tree t, Stack st) throws jasError {
    Sym s = null;
    if (!st.empty()) {
      Integer i = (Integer) st.pop();
      int n = i.intValue();
      s = comp.findsym(t.head().name() + "__" + id + n, t.nchildren());
      if (s == null) s = genexp(t, st);
      st.push(i);
    }
    return s;
  }

  void genexp(Int i) throws jasError {
    genval(i.val());
    genjsrstat(rintmcp);
  }

  void genexp(Real r) throws jasError {
    gendouble(r.val());
    genjsrstat(rrealmcp);
  }

  void genexp(Atom a) throws jasError {
    genexp(new Tree(a));
  }

  void genexp(Bag b) throws jasError {
    if (b.io == null) {
      genjsrstat(rbagmcp);
      for (int a = 0; a < b.nchildren(); a++) genbagexp(b.child(a));
      gendup();
      genjsrvirt(waitnormal);
    } else {
      genjsrstat(getparent);
      for (int a = 0; a < b.nchildren(); a++) genbagexp(b.child(a));
      gendrop();
      genexpvar(b.io);
    }
  }

  void genbagexp(Code code) throws jasError { // assumes bag on top of stack for dup
    Rules r = new Rules();
    int bt = comp.getbagtnum();
    String btn = "bagexp__" + bt;
    r.add(new Tree(new Atom(btn)), code);
    comp.addrules(r, "bagexp", 0, null);
    comp.compilerest();
    Sym s = comp.checksym(btn, 0, 0);
    if (s == null) intern("no bagthread sym!");
    comp.setbagtsym(s, bt);
    gendup();
    genval(bt);
    Vector fv = s.fvars;
    genval(fv.size());
    genaarray(classcps[0]);
    for (int aa = 0; aa < fv.size(); aa++) { // add free vars, if any
      gendup();
      genval(aa);
      genaload(genexplookup((Code) fv.elementAt(aa), code));
      genaastore();
    }
    genjsrstat(newthread);
  }

  void genexp(Rules r) throws jasError {
    comp.compilerest();
    locs.push(new Integer(r.locn));
    genexpvar(r.e);
    locs.pop();
  }

  void isval(int i, ClassCP castcp) throws jasError {
    gendup();
    gengetfield(fieldid);
    gencmpne(i, pm_getlab());
    gencast(castcp);
  }
}
