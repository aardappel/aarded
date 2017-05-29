import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Treeview {
  TreeCanvas darea;
  Frame win;
  MainWin mwin;
  String fname;
  Code tvc;
  Code sel = null, dragging = null;
  int lastx = 0, lasty = 0, firstx = 0, firsty = 0, prevx = 0, prevy = 0, dragx = 0, dragy = 0;
  boolean diddrag = false;
  final int border = 8;
  Scrollbar scrollver, scrollhor;
  int offx = 0, offy = 0;
  PopupMenu popup;
  TextField tf;
  Panel np;
  Panel bp;
  Button be;
  boolean nocoords = true;
  static Lock paintlock = new Lock();
  //final static Color pb = new Color(0,150,150);
  //final static Color sb = new Color(150,200,200);
  static final Color pb = Color.black;
  static final Color sb = new Color(60, 60, 60);
  final Treeview self = this; // to get around innerclass final prob

  Treeview(final MainWin mw, final String filename, Code tc) {
    tvc = tc;
    mwin = mw;
    fname = filename;
    try {
      win = new Frame("Tree View");
      MenuBar mb = new MenuBar();
      Menu mtree = new Menu("Tree");
      popup = new PopupMenu();
      addtotreemenu(mtree);
      addtotreemenu(popup);
      mb.add(mtree);
      win.setMenuBar(mb);
      darea = new TreeCanvas(this, popup);
      scrollver = new Scrollbar();
      scrollhor = new Scrollbar();
      Panel panel1 = new Panel();
      Canvas dummyc = new Canvas();
      win.setTitle("Tree Editor: " + filename);
      win.setLayout(new BorderLayout());
      scrollver.setSize(new Dimension(18, 258));
      win.addWindowListener(
          new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              mwin.viewclosing(self);
              win.dispose();
            }
          });
      scrollhor.setSize(new Dimension(477, 15));
      scrollhor.setOrientation(0);
      scrollhor.setUnitIncrement(50);
      scrollver.setUnitIncrement(50);
      scrollhor.addAdjustmentListener(
          new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
              offx = e.getValue();
              darea.repaint();
            }
          });
      scrollver.addAdjustmentListener(
          new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
              offy = e.getValue();
              darea.repaint_scrolly();
            }
          });
      win.add(scrollver, BorderLayout.EAST);
      panel1.setLayout(new BorderLayout());
      panel1.add(scrollhor, BorderLayout.CENTER);
      panel1.add(dummyc, BorderLayout.EAST);
      dummyc.setSize(15, 15);
      dummyc.setBackground(SystemColor.control);
      np = new Panel();
      np.setLayout(new BorderLayout());
      tf = new TextField();
      tf.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              String s = e.getActionCommand();
              try {
                try {
                  newint(Integer.parseInt(s), (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
                } catch (NumberFormatException x) {
                  newreal(
                      Double.valueOf(s).doubleValue(),
                      (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
                }
              } catch (NumberFormatException x) {
                newtree(s, (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
              }
              tf.setText("");
            }
          });
      Canvas dc = new Canvas();
      dc.setSize(200, 3);
      dc.setBackground(SystemColor.control);
      be = new Button("Eval");
      be.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              compile(false, null);
            }
          });
      bp = new Panel();
      bp.setLayout(new BorderLayout());
      bp.add(be, BorderLayout.EAST);
      np.add(bp, BorderLayout.WEST);
      np.add(dc, BorderLayout.SOUTH);
      np.add(tf, BorderLayout.CENTER);
      win.add(np, BorderLayout.NORTH);
      win.add(panel1, BorderLayout.SOUTH);
      win.add(darea, BorderLayout.CENTER);
      win.setLocation(250, 200);
      win.pack();
      tf.requestFocus();
      win.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  Code find(int x, int y, Code putin, boolean delete, boolean headonly) {
    return tvc.find(new Findinfo(x + offx, y + offy, putin, win, delete, headonly), border, border);
  }

  void select(Code s, Color a, Color b, Graphics g, int x, int y, boolean all) {
    tvc.select(new Selectinfo(s, a, b, g, x, y, all, offx, offy), border, border);
  }

  void doselect(Graphics g, int x, int y) {
    if (sel != null) {
      //select(sel,Color.white,Color.white,g,x,y,true);
      select(sel, pb, sb, g, prevx, prevy, true);
      sel = null;
    }
    sel = find(x, y, null, false, false);
    //darea.repaint();
    paintsel(g);
  }

  void paintsel(Graphics g) {
    if (sel != null) select(sel, pb, sb, g, lastx, lasty, true);
    prevx = lastx;
    prevy = lasty;
  }

  void dodrag(Graphics g, int xa, int ya, int xb, int yb) {
    if (dragging != null) { // for previous (xb,yb) actually
      //if(dragging!=sel) select(dragging,Color.white,Color.white,g,xb,yb,false);
      if (dragging != sel) select(dragging, Color.black, Color.black, g, dragx, dragy, false);
      dragging = null;
    }
    if ((dragging = find(xb, yb, null, false, false)) != null) {
      //if(dragging!=sel) select(dragging,Color.red,Color.red,g,xb,yb,false);
      if (dragging != sel) select(dragging, Color.black, Color.black, g, xb, yb, false);
      dragx = xb;
      dragy = yb;
    }
    diddrag = true;
  }

  void dragndrop(Graphics g, boolean headonly) {
    dragging = null;
    Code from = find(firstx, firsty, null, false, false);
    Code to = find(lastx, lasty, null, false, false);
    if (from != null) {
      if (from == to) {
          /*doselect(g,lastx,lasty);*/
        return;
      }
      find(lastx, lasty, from, false, headonly);
    }
    sel = null;
    refreshtree(true);
  }

  void refreshtree(boolean refreshothers) {
    recalctree(darea.getGraphics());
    repainttree(refreshothers);
  }

  void repainttree(boolean refreshothers) {
    darea.repaint();
    if (refreshothers) mwin.haschanged(tvc, this);
  }

  void recalctree(Graphics g) {
    tvc.calcsize(g, this);
    tvc.calcpos();
    scrollhor.setMaximum(tvc.xs);
    scrollver.setMaximum(tvc.ys);
  }

  void newtree(String t, boolean headonly) {
    // lookup t in known atoms
    Code nt = mwin.proj.newexample(t);
    putinplace(nt, headonly);
  }

  void newint(int i, boolean headonly) {
    putinplace(new Int(i), headonly);
  }

  void newreal(double d, boolean headonly) {
    putinplace(new Real(d), headonly);
  }

  void delete() {
    if (sel != null) {
      find(lastx, lasty, null, true, false);
      sel = null;
      refreshtree(true);
    }
  }

  void putinplace(Code nt, boolean headonly) {
    if (sel != null) {
      nt.dec();
      find(lastx, lasty, nt, false, headonly);
      sel = null;
    } else {
      tvc.add(nt);
    }
    refreshtree(true);
  }

  void compile(boolean standalone, Treeview tv) {
    Code target = tvc;
    if (sel != null) target = sel;
    target = target.copy().bag();
    if (target == null) {
      mwin.msg("can only evaluate bags (and trees)");
    } else {
      mwin.proj.compile(target, standalone, tv);
    }
  }

  void addtotreemenu(Menu m) {
    MenuItem midel;

    midel = new MenuItem("Evaluate", new MenuShortcut(KeyEvent.VK_E));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            compile(false, null);
          }
        });
    m.add(midel);

    midel = new MenuItem("Evaluate Step by Step" /*,new MenuShortcut(KeyEvent.VK_E)*/);
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            compile(false, new Treeview(mwin, "debugger view", new Atom("nil")));
          }
        });
    m.add(midel);

    midel = new MenuItem("Compile Standalone");
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            compile(true, null);
          }
        });
    m.add(midel);

    midel = new MenuItem("Inspect", new MenuShortcut(KeyEvent.VK_I));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) new Treeview(mwin, "part of " + fname, sel);
          }
        });
    m.add(midel);

    midel = new MenuItem("Copy", new MenuShortcut(KeyEvent.VK_C));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) mwin.buffer = sel.copy();
          }
        });
    m.add(midel);

    midel = new MenuItem("Paste", new MenuShortcut(KeyEvent.VK_V));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null && mwin.buffer != null)
              putinplace(mwin.buffer, (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
          }
        });
    m.add(midel);

    midel = new MenuItem("Delete", new MenuShortcut(KeyEvent.VK_D));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) delete();
          }
        });
    m.add(midel);

    midel = new MenuItem("New Bag", new MenuShortcut(KeyEvent.VK_B));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) putinplace(new Bag(), (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
          }
        });
    m.add(midel);

    midel = new MenuItem("New Local Rules", new MenuShortcut(KeyEvent.VK_R));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null)
              putinplace((new Rules()).makelocal(), (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
          }
        });
    m.add(midel);

    midel = new MenuItem("Add Child", new MenuShortcut(KeyEvent.VK_A));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) {
              sel.add(new Atom("nil"));
              refreshtree(true);
            }
          }
        });
    m.add(midel);

    midel = new MenuItem("Create In/Out", new MenuShortcut(KeyEvent.VK_L));
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) {
              Bag b = new Bag();
              b.setio(new Atom("nil"));
              putinplace(b, (e.getModifiers() & ActionEvent.ALT_MASK) != 0);
            }
          }
        });
    m.add(midel);

    midel = new MenuItem("Toggle Example" /*,new MenuShortcut(KeyEvent.VK_A)*/);
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) {
              sel.isexample = (!sel.isexample);
              refreshtree(true);
            }
          }
        });
    m.add(midel);

    midel = new MenuItem("Sort on Specificity" /*,new MenuShortcut(KeyEvent.VK_A)*/);
    midel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (sel != null) {
              sel.specsort();
              refreshtree(true);
            }
          }
        });
    m.add(midel);
  }
}

class Lock {
  boolean lock = false;

  synchronized void obtain() {
    while (lock)
      try {
        wait();
      } catch (InterruptedException e) {
      }
    lock = true;
  }

  synchronized void release() { // only to be called after an obtain()
    lock = false;
    notifyAll();
  }
}
