import java.awt.*;
import java.util.*;

public class Findinfo {
  boolean delete;
  boolean headonly;
  int fx, fy;
  Code putin;
  Frame win;
  Stack rules = new Stack();

  Findinfo(int x, int y, Code p, Frame w, boolean del, boolean ho) {
    fx = x;
    fy = y;
    putin = p;
    win = w;
    delete = del;
    headonly = ho;
  }

  Code domid(Code c, Code p, Code a, Vector elems, int n) {
    if (c == a) {
      if (headonly) {
        if (putin != null) {
          Tree tc = c.headtree();
          Tree ti = putin.headtree();
          if (tc != null && ti != null) {
            tc.h.dec();
            if (!Rules.checkscope(rules, ti.h, tc.h)) {
              tc.h = (Atom) ti.h.copy();
            } else {
              ti.h.inc();
              tc.h = ti.h;
            }
          }
        }
        return c;
      }
      if (delete && elems != null) {
        elems.removeElementAt(n);
        return c;
      }
      if (putin != null) {
        if (putin.partof(p)) {
          new StringRequest(win, "Can't create cycle", null, false);
          return c;
        }
        if (!Rules.checkscope(rules, putin, c)) {
          return putin.copy();
        } else {
          a.dec();
          putin.inc();
          return putin;
        }
      }
    }
    return c;
  }
}
