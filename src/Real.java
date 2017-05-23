import jas.*;

public class Real extends Atom {
  static final long serialVersionUID = -224228207242994894L;
  double r=0.0;

  public Real(double d) { super(new Double(d).toString()); r = d; };
  Code copy() { return new Real(r); };
  double val() { return r; };
  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
}
