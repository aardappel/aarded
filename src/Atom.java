import jas.*;
import java.awt.*;

public class Atom extends Code {
  static final long serialVersionUID = 868754979269304022L;
  String s;
  transient Image cached = null;

  Atom(String i) { s = i; };
  String name() { return s; };
  public String toString() { return s; }
  Code copy() { return new Atom(s); };
  void genpat(Sym s) throws jasError { s.genpat(this); }
  void genexp(Sym s) throws jasError { s.genexp(this); }
  void genpatpre(Sym s) throws jasError { s.genpatpre(this); }
  void genexppre(Sym s) throws jasError { s.genexppre(this); }
  boolean isnil() { return s.compareTo("nil")==0; };
  int structural_complexity() { return isexample?1:3; }

  void calcsize(Graphics g, Treeview tv) {
    precalcsize();
    drawmode = mflat;
    Image i;
    if((i = tv.mwin.checkatom(s,null))!=null) {
      cached = i;
      xs = i.getWidth(tv.darea);
      ys = i.getHeight(tv.darea);
    } else {
      FontMetrics fm = g.getFontMetrics();
      ys = fm.getHeight();
      xs = fm.stringWidth(s)+2;
    };
    postcalcsize();
  };

  void draw(Drawinfo d, int x, int y, boolean parcons) {
    d.shared(this,x,y);
    x += bborder; y += bborder;
    if(y+ys>0 && y<d.gys) {
      if(cached!=null) {
        d.g.drawImage(cached,x,y,null,d.tv.darea);
      } else {
        d.g.setColor(Color.black);
        d.g.drawString(s,x+1,y+d.g.getFontMetrics().getMaxAscent());
      };
    };
    d.unshared(this);
  }

}


