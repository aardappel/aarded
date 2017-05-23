import java.util.*;
import java.awt.*;
import java.io.*;
import jas.*;

public class Bag extends Code {
  static final long serialVersionUID = 6184631432177149899L;
  Vector c = new Vector();
  Code io = null;
  transient int iox, ioy;
  final int vert_inter_elem_space = 10;
  final int border_space = 10;
  final int ioadd = border_space*2-4;
  final static Color bagcolor = new Color(200,200,255);

  Code add(Code x) { c.addElement(new BagElem(x)); return this; };
  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
  Code bag() { return this; };
  int nchildren() { return c.size(); };
  Code child(int i) { return ((BagElem)c.elementAt(i)).e; }
  void setio(Code x) { io = x; } 
  Tree headtree() { return io==null?null:io.headtree(); };
  Bag headin() { return io==null?null:this; }

  public String toString() {
    String args = "";
    for(int a=0;a<c.size();a++) {
      args += ((BagElem)c.elementAt(a)).e+" ";
    };
    return "{ "+args+"}";
  }

  Code copy() {
    Bag b = new Bag();
    if(io!=null) b.io = io.copy();
    for(int a=0;a<c.size();a++) b.add((((BagElem)c.elementAt(a)).e).copy());
    return b;
  }

  void calcsize(Graphics g, Treeview tv) {
    precalcsize();
    xs = 0;
    ys = 0;
    for(int a=0;a<c.size();a++) {
      Code e = ((BagElem)c.elementAt(a)).e;
      e.calcsize(g,tv);
      ys+=e.ys+vert_inter_elem_space;
      xs=Math.max(xs,e.xs);
    };
    ys+=border_space*2-vert_inter_elem_space;
    if(io!=null) {
      io.calcsize(g,tv);
      ys+=io.ys+ioadd;
      xs=Math.max(xs,io.xs);
    };
    xs+=border_space*2;
    postcalcsize();
  }

  boolean partof(Code x) {
    if(x == this) return true;
    if(io!=null && io.partof(x)) return true;
    for(int a=0;a<c.size();a++) if((((BagElem)c.elementAt(a))).e.partof(x)) return true;
    return false;
  };

  int calcpos() {
    int cy = bborder+border_space;
    int cx = bborder+border_space;
    if(io!=null) {
      iox = cx;
      ioy = cy;
      io.calcpos();
      cy += io.ys+ioadd;
    };
    for(int a=0;a<c.size();a++) {
      BagElem be = (BagElem)c.elementAt(a);
      be.x = cx;
      be.y = cy;
      be.e.calcpos();
      cy += be.e.ys+vert_inter_elem_space;
    };
    return ys/2;
  }

  void draw(Drawinfo d, int x, int y, boolean parcons) {
    d.shared(this,x,y);
    int vskip = 0;
    if(io!=null) {
      vskip = io.ys+ioadd;
      d.fatdottedline(bagcolor,x,y,x+xs,y);
      d.fatdottedline(bagcolor,x,y,x,y+vskip);
      d.fatdottedline(bagcolor,x+xs-3,y,x+xs-3,y+vskip);
      io.draw(d,iox+x,ioy+y,false);
      y += vskip;
    };
    d.fatborder(bagcolor,x,y,this,vskip);
    for(int a=0;a<c.size();a++) {
      BagElem be = (BagElem)c.elementAt(a);
      if(be.y+y+be.e.ys>0 && be.y+y<d.gys) be.e.draw(d,be.x+x,be.y+y-vskip,false);
    };
    d.unshared(this);
  }

  Code find(Findinfo f, int x, int y) {
    if(!hit(f.fx,f.fy,x,y)) return null;
    if(boxhit(f.fx,f.fy,x,y,6)) return this;
    for(int a=0;a<c.size();a++) {
      BagElem be = (BagElem)c.elementAt(a);
      Code xe = be.e.find(f,be.x+x,be.y+y);
      be.e = f.domid(be.e,this,xe,c,a);
      if(xe!=null) return xe;
    };
    if(io!=null) {
      Code xe = io.find(f,iox+x,ioy+y);
      io = f.domid(io,this,xe,null,0);
      if(xe!=null) return xe;
    };
    return null;
  };

  void select(Selectinfo s, int x, int y) {
    s.box(this,x,y);
    if(io!=null) io.select(s,iox+x,ioy+y);
    for(int a=0;a<c.size();a++) {
      BagElem be = (BagElem)c.elementAt(a);
      be.e.select(s,be.x+x,be.y+y);
    };
  }

}

class BagElem implements Serializable {
  static final long serialVersionUID = 4998631640532122392L;
  Code e;
  transient int x, y;
  BagElem(Code x) { e = x; };
}

