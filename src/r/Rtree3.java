package r;

public class Rtree3 extends Rtree2 {
  public Rval c3;

  public Rtree3(int i, Rval a, Rval b, Rval c) {
    super(i, a, b);
    c3 = c;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c3, ((Rtree3) a).c3);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
