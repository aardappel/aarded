package r;

public class Rtree9 extends Rtree8 {
  public Rval c9;

  public Rtree9(int i, Rval a, Rval b, Rval c, Rval d, Rval e, Rval f, Rval g, Rval h, Rval j) {
    super(i, a, b, c, d, e, f, g, h);
    c9 = j;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5 + "," + c6 + "," + c7 + ","
        + c8 + "," + c9 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c9, ((Rtree9) a).c9);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
