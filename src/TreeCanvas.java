import java.awt.*;
import java.awt.event.*;

class TreeCanvas extends Canvas {
  Treeview tv;
  TreeCanvas c = this;
  int oldoffy = 0;
  Graphics lastg = null;
  Rectangle lastr = null;

  TreeCanvas(Treeview x, PopupMenu p) {
    tv = x;
    add(p);
    setBackground(Color.white);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    setSize(400, 400);
    addMouseListener(
        new MouseAdapter() {
          public void mousePressed(MouseEvent e) {
            Graphics g = c.getGraphics();
            tv.doselect(g, tv.firstx = tv.lastx = e.getX(), tv.firsty = tv.lasty = e.getY());
            if (e.isPopupTrigger()) {
              tv.popup.show(c, tv.lastx, tv.lasty);
            }
          }

          public void mouseReleased(MouseEvent e) {
            Graphics g = c.getGraphics();
            tv.lastx = e.getX();
            tv.lasty = e.getY();
            if (tv.diddrag) tv.dragndrop(g, (e.getModifiers() & MouseEvent.ALT_MASK) != 0);
            tv.diddrag = false;
          }

          public void mouseClicked(MouseEvent e) {
            Graphics g = c.getGraphics();
          }
        });
    addMouseMotionListener(
        new MouseMotionAdapter() {
          public void mouseDragged(MouseEvent e) {
            Graphics g = c.getGraphics();
            tv.dodrag(g, tv.firstx, tv.firsty, e.getX(), e.getY());
          }
        });
  }

  void dodraw(Graphics g, Rectangle r) {
    tv.paintlock.obtain();
    if (tv.nocoords) {
      tv.recalctree(g);
      tv.nocoords = false;
    }
    tv.tvc.draw(
        new Drawinfo(g, r.width, r.height, tv), tv.border - tv.offx, tv.border - tv.offy, false);
    tv.paintsel(g);
    tv.paintlock.release();
    Dimension d = getSize();
    tv.scrollhor.setVisibleAmount(d.width);
    tv.scrollver.setVisibleAmount(d.height);
    tv.scrollhor.setBlockIncrement(d.width);
    tv.scrollver.setBlockIncrement(d.height);
  }

  public void paint(Graphics g) {
    lastg = g;
    lastr = g.getClip().getBounds();
    dodraw(g, lastr);
  }

  public void repaint_scrolly() { // called instead of repaint() to avoid clear canvas
    /*
    Graphics g = lastg;
    Rectangle r = lastr;
    //if(g==null || r==null) return;
    if(oldoffy!=tv.offy && Math.abs(oldoffy-tv.offy)<r.height) {
      if(oldoffy<tv.offy) { // scroll down
      g.copyArea(0,tv.offy-oldoffy,r.width,r.height-tv.offy+oldoffy,0,oldoffy-tv.offy);
      } else { // scroll up
      }
    }
    dodraw(g,r);
    oldoffy = tv.offy;
    */
    repaint();
  }

  public void processMouseEvent(MouseEvent e) {
    if (e.isPopupTrigger()) tv.popup.show(c, e.getX(), e.getY());
    super.processMouseEvent(e);
  }
}
