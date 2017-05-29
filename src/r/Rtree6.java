package r;

public class Rtree6 extends Rtree5 {
  public Rval c6;

  public Rtree6(int i, Rval a, Rval b, Rval c, Rval d, Rval e, Rval f) {
    super(i, a, b, c, d, e);
    c6 = f;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5 + "," + c6 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c6, ((Rtree6) a).c6);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
