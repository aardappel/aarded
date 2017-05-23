import java.util.*;
import java.io.*;
import java.awt.*;
import jas.*;

public class Rules extends Code {
  static final long serialVersionUID = -443385629246835664L;
  Vector c = new Vector();
  Code e = null; // if non-null then the exp to a local rule.
  transient int locn;
  transient int ex, ey;
  final int patexp_space = 10;
  final int rule_space = 15;
  final static Color rulecolor = new Color(255,200,150);

  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
  Code add(Code x) { return add(x,new Atom("nil")); };

  Rules makelocal() {
    e = new Tree(new Atom("lambda"));
    add(new Tree(new Atom("lambda"),new Atom("nil")));
    return this;
  }

  Rules add(Code p, Code e) {
    c.addElement(new Rule(p,e));
    return this;
  };

  Code copy() {
    Rules r = new Rules();
    r.e = e==null?null:e.copy();
    for(int a = 0;a<c.size();a++) {
      Rule x = (Rule)c.elementAt(a);
      r.add(x.p.copy(),x.e.copy());
    };
    return r;
  }

  Code bestmatch(String s, Code sofar) {
    int d, e = sofar.structural_complexity();
    for(int i=0;i<c.size();i++) {
      Code p = ((Rule)c.elementAt(i)).p;
      Tree t = p.headtree();
      if(t!=null) {
        Atom a = t.head();
        if(a!=null && s.equals(a.name())) {
          if((d = p.structural_complexity())>=e) { e = d; sofar = p; };
        };
      };
    };
    return sofar;
  };

  Vector allexps() {
    Vector v = new Vector();
    for(int i=0;i<c.size();i++) {
      Rule r = (Rule)c.elementAt(i);
      v.addElement(r.p);
      v.addElement(r.e);
    };
    return v;
  };

  void calcsize(Graphics g, Treeview tv) {
    precalcsize();
    xs = 0;
    ys = 0;
    for(int a=0;a<c.size();a++) {
      Rule r = (Rule)c.elementAt(a);
      r.p.calcsize(g,tv);
      ys+=r.p.ys+patexp_space;
      xs=Math.max(xs,r.p.xs);
      r.e.calcsize(g,tv);
      ys+=r.e.ys+rule_space;
      xs=Math.max(xs,r.e.xs);
    };
    if(e!=null) {
      e.calcsize(g,tv);
      xs = Math.max(xs,e.xs);
      ys += e.ys+rule_space;
    };
    xs+=rule_space*2;
    ys+=rule_space*2-patexp_space;
    postcalcsize();
  };

  boolean partof(Code x) {
    if(x == this || (e!=null && e.partof(x))) return true;
    for(int a=0;a<c.size();a++) {
     Rule r = (Rule)c.elementAt(a);
     if(r.p.partof(x) || r.e.partof(x)) return true;
    };
    return false;
  };

  int calcpos() {
    int cy = rule_space+bborder;
    int cx = rule_space+bborder;
    if(e!=null) cy += e.ys+rule_space;
    for(int a=0;a<c.size();a++) {
      Rule r = (Rule)c.elementAt(a);
      r.px = cx;
      r.py = cy;
      r.p.calcpos();
      cy += r.p.ys+patexp_space;
      r.ex = cx;
      r.ey = cy;
      r.e.calcpos();
      cy += r.e.ys+rule_space;
    };
    if(e!=null) {
      e.calcpos();
      ex = cx;
      ey = rule_space+bborder;
    };
    return ys/2;
  }

  void draw(Drawinfo d, int x, int y, boolean parcons) {
    d.shared(this,x,y);
    if(e!=null) d.fatborder(rulecolor,x,y,this,0);
    for(int a=0;a<c.size();a++) {
      Rule r = (Rule)c.elementAt(a);
      if(r.py+y+r.p.ys>0 && r.py+y<d.gys) {
        r.p.draw(d,r.px+x,r.py+y,false);
        d.fatline(xs-rule_space*2,r.px+x,r.py+y-rule_space/2-2,3,Color.black);
      };
      if(r.ey+y+r.e.ys>0 && r.ey+y<d.gys) {
        r.e.draw(d,r.ex+x,r.ey+y,false);
        int ty = r.ey+y-(patexp_space/2+1);
        d.dotted_line(r.ex+x,ty,x+xs-rule_space,ty,Color.black,4);
      };
    };
    if(e!=null) e.draw(d,ex+x,ey+y,false);
    d.fatline(xs-rule_space*2,rule_space+x,y-rule_space+ys,3,Color.black);
    d.unshared(this);
  }

  Code find(Findinfo f, int x, int y) {
    if(!hit(f.fx,f.fy,x,y)) return null;
    if(boxhit(f.fx,f.fy,x,y,6)) return this;
    for(int a=0;a<c.size();a++) {
      Rule r = (Rule)c.elementAt(a);
      f.rules.push(r);
      Code xp = r.p.find(f,r.px+x,r.py+y);
      r.p = f.domid(r.p,this,xp,c,a);
      if(xp!=null) return xp;
      Code xe = r.e.find(f,r.ex+x,r.ey+y);
      r.e = f.domid(r.e,this,xe,c,a);
      if(xe!=null) return xe;
      f.rules.pop();
    };
    if(e!=null) {
      Code xe = e.find(f,ex+x,ey+y);
      e = f.domid(e,this,xe,null,0);
      if(xe!=null) return xe;
    };
    return null;
  }

  static boolean checkscope(Stack rules, Code from, Code to) {
    while(!rules.empty()) {
      Rule r = (Rule)rules.pop();
      if(r.e.partof(to)) {
        if(r.p.partof(from)) return from.isexample = true;
        //if(r.e.partof(from)) return true;  // creates normal sharing
        //return false;
      };
      if(r.p.partof(to)) {
        if(r.p.partof(from)) return from.isexample = true;
        //return false;
      };
    };
    return false;
  }

  void select(Selectinfo s, int x, int y) {
    s.box(this,x,y);
    for(int a=0;a<c.size();a++) {
      Rule r = (Rule)c.elementAt(a);
      r.p.select(s,r.px+x,r.py+y);
      r.e.select(s,r.ex+x,r.ey+y);
    };
    if(e!=null) e.select(s,ex+x,ey+y);
  }

  void specsort() {
    boolean swapped;
    do {
      swapped = false;
      for(int i = c.size()-1;i>0;i--) {
        Rule l = (Rule)c.elementAt(i);
        Rule b = (Rule)c.elementAt(i-1);
        if(l.p.speccomp(b.p)>0) {
          swapped = true;
          c.setElementAt(b,i);
          c.setElementAt(l,i-1);
        };
      };
    } while(swapped);
  };

}

class Rule implements Serializable {
  static final long serialVersionUID = 6146545985025118120L;
  Code p, e;
  transient int px, py, ex, ey;
  Rule(Code pat, Code exp) { p = pat; e = exp; };
}

