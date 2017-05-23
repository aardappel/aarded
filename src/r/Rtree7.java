package r;


public class Rtree7  extends Rtree6  {
  public Rval c7;
  public Rtree7(int i, Rval a, Rval b, Rval c, Rval d, Rval e, Rval f, Rval g) {
    super(i,a,b,c,d,e,f);
    c7=g;
  }
  public String toString() {
    return id+"("+c1+","+c2+","+c3+","+c4+","+c5+","+c6+","+c7+")";
  }
  public Object code(Access a) { return a.rv(this); }
  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c7,((Rtree7)a).c7);
    return t==AardappelRuntime.rtrue?super.equal(a):t;
  };
};
