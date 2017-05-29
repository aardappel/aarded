package r;

public class Rtree4 extends Rtree3 {
  public Rval c4;

  public Rtree4(int i, Rval a, Rval b, Rval c, Rval d) {
    super(i, a, b, c);
    c4 = d;
  }

  public String toString() {
    return id + "(" + c1 + "," + c2 + "," + c3 + "," + c4 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    Rval t = AardappelRuntime.builtin_rule_61_2(c4, ((Rtree4) a).c4);
    return t == AardappelRuntime.rtrue ? super.equal(a) : t;
  }
}
