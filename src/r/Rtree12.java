package r;

public class Rtree12 extends Rtree11 {
  public Rval c12;

  public Rtree12(
      int i,
      Rval a,
      Rval b,
      Rval c,
      Rval d,
      Rval e,
      Rval f,
      Rval g,
      Rval h,
      Rval j,
      Rval k,
      Rval l,
      Rval m) {
    super(i, a, b, c, d, e, f, g, h, j, k, l);
    c12 = m;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5 + "," + c6 + "," + c7 + ","
        + c8 + "," + c9 + "," + c10 + "," + c11 + "," + c12 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c12, ((Rtree12) a).c12);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
