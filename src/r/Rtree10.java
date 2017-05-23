package r;


public class Rtree10 extends Rtree9  {
  public Rval c10;
  public Rtree10(int i, Rval a, Rval b, Rval c, Rval d, Rval e, Rval f, Rval g, Rval h, Rval j, Rval k) {
    super(i,a,b,c,d,e,f,g,h,j);
    c10=k;
  }
  public String toString() {
    return id+"("+c1+","+c2+","+c3+","+c4+","+c5+","+c6+","+c7+","+c8+","+c9+","+c10+")";
  }
  public Object code(Access a) { return a.rv(this); }
  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c10,((Rtree10)a).c10);
    return t==AardappelRuntime.rtrue?super.equal(a):t;
  };
};