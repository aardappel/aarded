package r;

public class Rreal extends Rval {
  public static final int idc = 2;
  public double v;

  public Rreal(double r) {
    super(idc);
    v = r;
  }

  public String toString() {
    return "" + v;
  }

  public Object code(Access a) {
    return a.rv(this);
  }
}
