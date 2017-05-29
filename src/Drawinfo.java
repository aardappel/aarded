import java.awt.*;

public class Drawinfo {
  Graphics g;
  int gxs, gys;
  int refl = 0;
  Treeview tv;

  Drawinfo(Graphics gr, int xs, int ys, Treeview t) {
    g = gr;
    gxs = xs;
    gys = ys;
    tv = t;
  }

  void bigbracket(int x, int y, int xdir, int ys) {
    if (xdir < 0) x -= 2;
    bigbar(x, y, 2, ys);
    if (xdir < 0) {
      x -= 2;
    } else {
      x += 2;
    }
    bigdot(x, y, 2, 2);
    bigdot(x, y + ys - 2, 2, 2);
  }

  void bigdot(int x, int y, int xs, int ys) {
    x += (xs - 2) / 2;
    y += (ys - 2) / 2;
    g.setColor(Color.darkGray);
    g.drawLine(x, y, x, y + 1);
    g.drawLine(x + 1, y, x + 1, y + 1);
  }

  void bigbar(int x, int y, int xs, int ys) {
    g.setColor(Color.darkGray);
    for (int a = 0; a < xs; a++) g.drawLine(x + a, y, x + a, y + ys - 1);
  }

  void bracket(int xp, int yp, int bracketdir, int vsize) {
    vsize--;
    g.setColor(Color.black);
    g.drawLine(xp, yp, xp, yp + vsize);
    g.drawLine(xp, yp, xp + bracketdir, yp);
    g.drawLine(xp, yp + vsize, xp + bracketdir, yp + vsize);
  }

  void fatdottedline(Color c, int x, int y, int ex, int ey) {
    int stepx = (x == ex) ? 0 : 8;
    int stepy = (y == ey) ? 0 : 8;
    g.setColor(c);
    for (; x <= ex && y <= ey; x += stepx, y += stepy) g.fillRect(x, y, 4, 4);
  }

  void fatborder(Color c, int x, int y, Code a, int vskip) {
    g.setColor(c);
    g.drawRect(x, y, a.xs, a.ys - vskip);
    g.drawRect(x + 1, y + 1, a.xs - 2, a.ys - 2 - vskip);
    g.drawRect(x + 2, y + 2, a.xs - 4, a.ys - 4 - vskip);
    g.drawRect(x + 3, y + 3, a.xs - 6, a.ys - 6 - vskip);
  }

  void fatline(int width, int x, int y, int n, Color col) {
    g.setColor(col);
    while (n-- != 0) g.drawLine(x, y, x + width, y++);
  }

  void unshared(Code c) {
    if (c.bborder != 0) refl--;
  }

  void shared(Code c, int x, int y) {
    if (c.bborder != 0) {
      refl++;
      int a = Math.max(255 - refl * 16, 150);
      int b = Math.max(200 - refl * 16, 100);
      g.setColor(
          c.isexample
              ? new Color(a, b, 0)
              : (c.warnval > 0 ? new Color(a, 0, 0) : new Color(a, a, a)));
      g.fillRect(x, y, c.xs, c.ys);
    }
  }

  void dotted_line(int x, int y, int ex, int ey, Color c, int step) {
    int stepx = (x == ex) ? 0 : step;
    int stepy = (y == ey) ? 0 : step;
    g.setColor(c);
    for (; x <= ex && y <= ey; x += stepx, y += stepy) g.drawLine(x, y, x, y);
  }
}
