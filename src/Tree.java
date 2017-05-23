import java.util.*;
import java.io.*;
import java.awt.*;
import jas.*;

public class Tree extends Code {
  static final long serialVersionUID = -7775464705404605041L;
  Atom h=null;
  Vector c=new Vector();
  transient int hy, hx, xdot, first, last;
  static boolean nobracketrender = true;
  final static int vert_inter_elem_space = 4;
  final static int horiz_atom_tree_space = 4;
  final static int dotted_line_space = 5;              // odd
  final static int horiz_tree_child_space = 4;
  final static int flat_inter_elem_space = 5;
  final static int flat_bracketsize = 4;
  final static int flat_nexttobracket = 5;
  final static int cons_bracketsize = 4;
  final static int cons_nexttobracket = 5;
  final static int cons_barsize = 2;

  Tree(Atom a) { h=a;};
  Tree(Atom a, Code e) { h=a; add(e); };
  Tree(Atom a, Code x, Code y) { h=a; add(x); add(y); }
  Code bag() { return (new Bag()).add(this); }
  Atom head() { return h; }
  int nchildren() { return c.size(); };
  Code child(int i) { return ((Branch)c.elementAt(i)).e; }
  Code add(Code e) { c.addElement(new Branch(e)); return this; };
  Tree headtree() { return this; };
  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
  boolean iscons() { return c.size()==2 && h.s.compareTo("cons")==0; };
  boolean isnil() { return c.size()==0 && h.s.compareTo("nil")==0; };

  public String toString() {
    String args = "";
    for(int a=0;a<c.size();a++) {
      args += ((Branch)c.elementAt(a)).e;
      if(a!=c.size()-1) args += ",";
    };
    return h.s+"("+args+")";
  }

  Code copy() {
    Tree t = new Tree((Atom)h.copy());
    for(int a=0;a<c.size();a++) t.add(((Branch)c.elementAt(a)).e.copy());
    return t;
  };

  void calcsize(Graphics g, Treeview tv) {
    precalcsize();
    drawmode = mflatbr;
    if(c.size()==0 || bborder!=0) drawmode = mflat;
    h.calcsize(g,tv);
    int totalx = 0;
    int maxy = 0;
    if(iscons()) drawmode = mcons;
    for(int a=0;a<c.size();a++) {
      Branch b = (Branch)c.elementAt(a);
      b.e.calcsize(g,tv);
      if(b.e.drawmode==mtree) drawmode = mtree;
      if(nobracketrender && drawmode!=mcons && b.e.drawmode==mflatbr) drawmode = mtree;
      totalx += b.e.xs;
      if(b.e.ys>maxy) maxy = b.e.ys;
    };
    if(totalx>500) drawmode = mtree; // config
    xs = 0;
    ys = 0;
    if(drawmode==mtree) {
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        ys += b.e.ys+vert_inter_elem_space;
        xs = Math.max(xs,b.e.xs);
      };
      ys = Math.max(ys-vert_inter_elem_space,h.ys);
      xs += h.xs+horiz_atom_tree_space+dotted_line_space+horiz_tree_child_space;
    } else if(drawmode==mflat || drawmode==mflatbr) {
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        xs += b.e.xs+flat_inter_elem_space;
        if(b.e.drawmode==mflatbr) xs += (flat_bracketsize+flat_nexttobracket)*2;
      };
      ys = Math.max(maxy,h.ys);
      xs += h.xs;
      xdot = maxy;
    } else if(drawmode==mcons) {
      Branch b = (Branch)c.elementAt(0);
      xs = cons_bracketsize+cons_nexttobracket+b.e.xs+cons_nexttobracket;
      ys = b.e.ys;
      b = (Branch)c.elementAt(1);
      if(b.e.isnil()) {
        xs += cons_bracketsize;
      } else {
        xs += b.e.xs;
        if(!b.e.iscons()) xs += cons_barsize+cons_bracketsize+cons_nexttobracket*2;
        ys = Math.max(ys,b.e.ys);
      };
    };
    postcalcsize();
  };

  boolean partof(Code x) {
    if(x == this || h.partof(x)) return true;
    for(int a=0;a<c.size();a++) if(((Branch)c.elementAt(a)).e.partof(x)) return true;
    return false;
  };

  int calcpos() {
    hx = bborder;
    if(drawmode==mtree) {
      xdot = h.xs+horiz_atom_tree_space;
      int xpos = xdot+dotted_line_space+horiz_tree_child_space;
      int cy = 0+bborder;
      int cx = xpos+bborder;
      first = -1;
      last = -1;
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        b.x = cx;
        b.y = cy;
        b.my = b.e.calcpos()+cy;
        if(first<0) first = b.my;
        if(last<b.my) last = b.my;
        cy += b.e.ys+vert_inter_elem_space;
      };
      hy = (c.size()>0)?Math.max(((first+last)/2)-(h.ys/2),0):0;
      return h.calcpos()+hy;
    } else if(drawmode==mflat || drawmode==mflatbr) {
      hy = (ys-h.ys)/2;
      int cx = h.xs+flat_inter_elem_space+bborder;
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        b.e.calcpos();
        b.x = cx;
        b.y = (ys-b.e.ys)/2;
        cx += b.e.xs+flat_inter_elem_space;
        if(b.e.drawmode==mflatbr) {
          b.x += flat_bracketsize+flat_nexttobracket;
          cx += (flat_bracketsize+flat_nexttobracket)*2;
        };
      };
      h.calcpos();
    } else if(drawmode==mcons) {
      Branch b = (Branch)c.elementAt(0);
      b.e.calcpos();
      b.x = cons_bracketsize+cons_nexttobracket;
      b.y = (ys-b.e.ys)/2;
      int cx = b.x+b.e.xs+cons_nexttobracket;
      b.x += bborder;
      cx += bborder;
      xdot = cx;
      b = (Branch)c.elementAt(1);
      if(!b.e.iscons()) {
        cx += cons_barsize+cons_nexttobracket;
      } else {
        b.e.ys = ys-bborder*2;
      };
      b.x = cx;
      b.y = (ys-b.e.ys)/2;
      b.e.calcpos();
    };
    return ys/2;
  }

  void draw(Drawinfo d, int x, int y, boolean parcons) {
    d.shared(this,x,y);
    if(drawmode!=mcons) h.draw(d,hx+x,hy+y,false);
    if(drawmode == mtree) {
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        b.e.draw(d,b.x+x,b.y+y,false);
        d.dotted_line(xdot+x,b.my+y,xdot+x+dotted_line_space,b.my+y,Color.gray,2);
      };
      if(c.size()>1) d.dotted_line(xdot+x,first+y,xdot+x,last+y,Color.gray,2);
    } else if(drawmode==mflat || drawmode==mflatbr) {
      int yp = (ys-xdot)/2+y;
      for(int a=0;a<c.size();a++) {
        Branch b = (Branch)c.elementAt(a);
        if(b.e.drawmode==mflatbr) d.bracket(b.x+x-flat_bracketsize-flat_nexttobracket,yp,flat_bracketsize-1,xdot);
        b.e.draw(d,b.x+x,b.y+y,false);
        if(b.e.drawmode==mflatbr) d.bracket(b.x+x+b.e.xs+flat_bracketsize+flat_nexttobracket,yp,-flat_bracketsize+1,xdot);
      };
    } else if(drawmode==mcons) {
      int lx = x+bborder;
      int ly = y+bborder;
      int lys = ys-bborder*2;
      int lxs = xs-bborder*2;
      if(parcons) {
        d.bigdot(x,y,cons_bracketsize,ys);
      } else {
        d.bigbracket(lx,ly,cons_bracketsize,lys);
      };
      Branch b = (Branch)c.elementAt(0);
      b.e.draw(d,b.x+x,b.y+y,false);
      b = (Branch)c.elementAt(1);
      if(b.e.iscons()) {
        b.e.draw(d,b.x+x,b.y+y,true);
      } else {
        if(!b.e.isnil()) {
          b.e.draw(d,b.x+x,b.y+y,false);
          d.bigbar(xdot+lx,ly,cons_barsize,lys);
        };
        d.bigbracket(lx+lxs,ly,-cons_bracketsize,lys);
      }
    };
    d.unshared(this);
  }

  Code find(Findinfo f, int x, int y) {
    if(!hit(f.fx,f.fy,x,y)) return null;
    if(drawmode==mcons) {
      if(f.fx>=x && f.fy>=y && f.fx<x+cons_bracketsize+1 && f.fy<y+ys) return this;
    } else {
      if(h.hit(f.fx,f.fy,x,hy+y)) return this;
    };
    for(int a=0;a<c.size();a++) {
      Branch b = (Branch)c.elementAt(a);
      Code xe = b.e.find(f,b.x+x,b.y+y);
      b.e = f.domid(b.e,this,xe,c,a);
      if(xe!=null) return xe;
    };
    return null;
  };

  void select(Selectinfo s, int x, int y) {
    s.box(this,x,y);
    for(int a=0;a<c.size();a++) {
      Branch b = (Branch)c.elementAt(a);
      b.e.select(s,b.x+x,b.y+y);
    };
    h.select(s,x,hy+y);
  }

  int structural_complexity() {
    if(isexample) return 1;
    int n = 1;
    for(int a=0;a<c.size();a++) n+=((Branch)c.elementAt(a)).e.structural_complexity();
    return n;
  };

  int speclevel() { return isexample?0:1; }

  int speccomp(Code c) {
    int sp = speclevel();
    int diff = sp-c.speclevel();
    if(diff == 0 && sp == 1) { // both trees
      Tree t = (Tree)c;
      int n = nchildren(), n2 = t.nchildren();
      if(n2!=n) return n2-n; // different nargs, less args is more spec
      if(h.name().compareTo(t.head().name())!=0) return 0; // different atoms, don't bother
      for(int i = 0;i<n;i++) {
        int d = child(i).speccomp(t.child(i));
        if(d!=0) return d;
      };
    };
    return diff;
  } // pos = morespec than c, 0 = equal, neg = lessspec

}

class Branch implements Serializable {
  static final long serialVersionUID = 2134899348482548316L;
  String label = null;
  Code e;
  transient int x, y, my;
  Branch(Code x) { e = x; };
};


