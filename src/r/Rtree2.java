package r;

public class Rtree2 extends Rtree1 {
  public Rval c2;

  public Rtree2(int i, Rval a, Rval b) {
    super(i, a);
    c2 = b;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c2, ((Rtree2) a).c2);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
