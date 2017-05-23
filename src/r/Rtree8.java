package r;


public class Rtree8  extends Rtree7  {
  public Rval c8;
  public Rtree8(int i, Rval a, Rval b, Rval c, Rval d, Rval e, Rval f, Rval g, Rval h) {
    super(i,a,b,c,d,e,f,g);
    c8=h;
  }
  public String toString() {
    return id+"("+c1+","+c2+","+c3+","+c4+","+c5+","+c6+","+c7+","+c8+")";
  }
  public Object code(Access a) { return a.rv(this); }
  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c8,((Rtree8)a).c8);
    return t==AardappelRuntime.rtrue?super.equal(a):t;
  };
};
