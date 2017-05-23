import jas.*;

public class Int extends Atom {
  static final long serialVersionUID = -224228207242994894L;
  int i=0;

  public Int(int n) { super(new Integer(n).toString()); i = n; };
  Code copy() { return new Int(i); };
  int val() { return i; };
  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
}
