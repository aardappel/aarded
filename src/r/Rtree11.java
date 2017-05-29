package r;

public class Rtree11 extends Rtree10 {
  public Rval c11;

  public Rtree11(
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
      Rval l) {
    super(i, a, b, c, d, e, f, g, h, j, k);
    c11 = l;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5 + "," + c6 + "," + c7 + ","
        + c8 + "," + c9 + "," + c10 + "," + c11 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c11, ((Rtree11) a).c11);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
