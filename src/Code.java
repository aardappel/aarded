import java.io.*;
import java.awt.*;
import jas.*;

public class Code implements Serializable {
  static final long serialVersionUID = 8879156993735980258L;
  int refc=1;
  boolean isexample = false;
  transient int warnval = 0;
  transient int patvar = 0;
  transient public int xs,ys;
  transient int drawmode = mtree;
  transient int bborder = 0;
  static final int mtree = 0;
  static final int mflat = 1;
  static final int mflatbr = 2;
  static final int mcons = 3;
  static final int bborder_size = 3;

  void inc() { refc++; }
  void dec() { refc--; if(refc==0) for(int a = 0;a<nchildren();a++) child(a).dec(); }
  Tree headtree() { return null; }
  Bag headin() { return null; }
  Code copy() { return null; }
  Code add(Code x) { return this; }
  Code example() { isexample=!isexample; return this; }
  void genpat(Sym s) throws jasError { throw new CompError("no genpat"); }
  void genexp(Sym s) throws jasError { throw new CompError("no genexp"); }
  void genpatpre(Sym s) throws jasError { throw new CompError("no genpatpre"); }
  void genexppre(Sym s) throws jasError { throw new CompError("no genexppre"); }
  Code bag() { return null; }
  int nchildren() { return 0; }
  Code child(int i) { return null; }
  void calcsize(Graphics g, Treeview tv) {}
  int calcpos() { return ys/2; }
  boolean partof(Code x) { return x == this; }
  void draw(Drawinfo d, int x, int y, boolean parcons) {}
  Code find(Findinfo f, int x, int y) { return hit(f.fx,f.fy,x,y)?this:null; }
  void select(Selectinfo s, int x, int y) { s.box(this,x,y); }
  void precalcsize() { bborder = (refc>1 || isexample || warnval>0)?bborder_size:0; }
  void postcalcsize() { xs += bborder*2; ys += bborder*2; }
  boolean iscons() { return false; };
  boolean isnil() { return false; };
  int structural_complexity() { return 1; }
  int speclevel() { return isexample?0:2; }   // var = 0, tree = 1, rest = 2
  int speccomp(Code c) { return speclevel()-c.speclevel(); } // pos = morespec than c, 0 = equal, neg = lessspec
  void specsort() {};

  boolean hit(int mx, int my, int x, int y) {
    return mx>=x && my>=y && mx<x+xs && my<y+ys;
  }

  boolean boxhit(int mx, int my, int x, int y, int s) {
    return !(mx>=x+s && mx<x+xs-s && my>=y+s && my<y+ys-s);
  }

}


