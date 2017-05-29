import java.awt.*;

public class Selectinfo {
  Code sel;
  Color col, second;
  Graphics g;
  int hx, hy, xo, yo;
  boolean doall;

  public Selectinfo(
      Code s, Color c, Color sec, Graphics gr, int hitx, int hity, boolean all, int x, int y) {
    sel = s;
    col = c;
    g = gr;
    hx = hitx;
    hy = hity;
    second = sec;
    doall = all;
    xo = x;
    yo = y;
  }

  void box(Code c, int x, int y) {
    x -= xo;
    y -= yo;
    Color r = col;
    if (hx < x || hy < y || hx > x + c.xs || hy > y + c.ys) {
      if (!doall && c.refc > 1) return;
      r = second;
    }
    if (c == sel) {
      //g.setColor(r);
      //g.drawRect(x,y,c.xs,c.ys);
      g.setColor(Color.white);
      g.setXORMode(r);
      g.fillRect(x, y, c.xs, c.ys);
      g.setPaintMode();
    }
  }
}
