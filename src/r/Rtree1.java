package r;

public class Rtree1 extends Rval {
  public Rval c1;

  public Rtree1(int i, Rval a) {
    super(i);
    c1 = a;
  }

  public String toString() {
    return id + "(" + c1 + ")";
  }

  public Object code(Access a) {
    return a.rv(this);
  }

  public Rval equal(Rval a) {
    return AardappelRuntime.builtin_rule_61_2(c1, ((Rtree1) a).c1);
  }
}
