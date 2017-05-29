package r;

public class Rtree5 extends Rtree4 {
  public Rval c5;

  public Rtree5(int i, Rval a, Rval b, Rval c, Rval d, Rval e) {
    super(i, a, b, c, d);
    c5 = e;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + "," + c5 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c5, ((Rtree5) a).c5);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
