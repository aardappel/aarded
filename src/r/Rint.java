package r;

public class Rint extends Rval {
  public static final int idc = 0;
  public int v;

  public Rint(int i) {
    super(idc);
    v = i;
  }

  public String toString() {
    return "" + v;
  }

  public Object code(Access a) {
    return a.rv(this);
  }
}
