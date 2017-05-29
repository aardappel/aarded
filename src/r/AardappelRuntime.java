package r;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.security.*;
import java.util.*;

public class AardappelRuntime {
  public static final int maxchildren = 12;
  public static final int idint = Rint.idc; // 0
  public static final int idbag = Rbag.idc; // 1
  public static final int idreal = Rreal.idc; // 2
  public static final int idtreestart = 10;
  public static final int idtrue = 10; // in order, see Compiler()
  public static final int idfalse = 11;
  public static final int idnil = 12;
  public static final int idvoid = 13;

  public static final Rval rtrue = new Rval(idtrue);
  public static final Rval rfalse = new Rval(idfalse);
  public static final Rval rnil = new Rval(idnil);
  public static final Rval rvoid = new Rval(idvoid);

  public Rval finalbag = null;
  public int maxtreeid;
  public Canvas gfxc = null;
  public Debug dbg = null;
  public static AardappelRuntime self; // we only create 1 object ever
  public Vector tupleservers = new Vector();
  public Vector servernames = new Vector();
  public int totalservers = 1, nextserver = 0;
  public AardappelServer server = null;
  long start;

  public AardappelRuntime(int maxid) {
    self = this;
    maxtreeid = maxid;
    dbg = Debug.getdbg();
    start = System.currentTimeMillis();
  }

  public static void rterror(String s) {
    msg("fatal: " + s);
    System.exit(1);
  }

  public static void msg(String s) {
    System.out.println("aardappelruntime: " + s);
  }

  public static void debug(Rval a) {
    /*System.out.println("value: "+a);*/
    System.out.println((System.currentTimeMillis() - self.start) / 1000.0 + " seconds.");
  }

  public static void interntab(int i) {
    rterror("table: " + i);
  }

  public static void debugarg(Rval a) {
    if (self.dbg != null) self.dbg.arg(a);
  }

  public static void debugtree(int id) {
    if (self.dbg != null) self.dbg.tree(id);
  }

  public static Rint rint(int i) {
    return new Rint(i);
  }

  public static Rreal rreal(double r) {
    return new Rreal(r);
  }

  public Rval runcode(int codeid, Rval freevars[]) {
    rterror("runcode");
    return null;
  }

  public void run(String[] servers, byte[] classdata) {
    rterror("run");
  }

  public Rval runrule() {
    rterror("runrule");
    return null;
  }

  public byte[] loadclass(String[] servers) {
    if (servers.length == 0) return null;
    String name = this.getClass().getName() + ".class"; //"CompiledAardappel.class";
    try {
      FileInputStream in = new FileInputStream(name);
      byte[] buf = new byte[(int) (new File(name)).length()];
      in.read(buf);
      in.close();
      return buf;
    } catch (IOException i) {
      System.out.println("couldn't load class file " + name + " for distribution");
    }
    return null;
  }

  public Rval start(String[] servers, byte[] classdata) {
    if (classdata != null && servers.length > 0) {
      for (int i = 0; i < servers.length; i++) connectserver(servers[i], classdata);
    }
    totalservers = tupleservers.size() + 1;
    Rval r = null;
    try {
      r = runrule();
    } catch (Throwable e) {
      msg("main thread aborted abnormally: " + e.getMessage());
      e.printStackTrace();
    }
    return r;
  }

  public static void cleanup() {
    try {
      for (int i = 0; i < self.totalservers - 1; i++) {
        AardappelServerIn as = (AardappelServerIn) self.tupleservers.elementAt(i);
        as.terminate();
      }
    } catch (RemoteException e) {
      msg("could not terminate threads on remote host");
    }
  }

  public void connectserver(String servername, byte[] classdata) {
    String name = "rmi://" + servername + "/TupleServer";
    try {
      if (tupleservers.size() == 0) System.setSecurityManager(new RMISecurityManager());
      AardappelServerIn as = (AardappelServerIn) Naming.lookup(name);
      as.startup(classdata);
      tupleservers.addElement(as);
      servernames.addElement(servername);
      msg("host " + servername + " ready to participate");
    } catch (AccessControlException e) {
      msg("access denied for " + servername + " (check java.policy?):\n" + e);
    } catch (MalformedURLException e) {
      msg("URL error: " + name);
    } catch (NotBoundException e) {
      msg("TupleServer not bound on host " + servername);
    } catch (RemoteException e) {
      msg(
          "host "
              + servername
              + " won't participate: error connecting or rmi registry could not be contacted:\n"
              + e);
    }
  }

  public static void newthread(Rbag parent, int codeid, Rval freevars[]) {
    if (self.server != null) {
      self.server.marshallthread(parent, codeid, freevars);
    } else if (self.nextserver == self.totalservers - 1) {
      self.nextserver = 0;
      runthread(parent, codeid, freevars);
    } else {
      AardappelServerIn as = (AardappelServerIn) self.tupleservers.elementAt(self.nextserver++);
      // self.nextserver = 0;  // to force all threads to be on the first remote machine
      try {
        as.exec(parent, codeid, freevars);
      } catch (RemoteException e) {
        msg(
            "could not launch thread on remote host "
                + (String) self.servernames.elementAt(self.nextserver - 1)
                + "(not fault tolerant yet, sorry)");
        msg("problem was: " + e);
      }
    }
  }

  public static void runthread(Rbag parent, int codeid, Rval freevars[]) {
    try {
      Rthread t = new Rthread(codeid, freevars, parent, self);
      t.start();
    } catch (RemoteException e) {
      msg("remote problem on local host?\n" + e);
    } catch (Throwable e) {
      msg("thread aborted abnormally: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static Rbag newbag() throws RemoteException {
    Rbag b = new Rbag();
    b.createTS();
    return b;
  }

  public static Rbag getparent() { // from current thread, for rule in/out op
    return ((Rthread) Thread.currentThread()).p;
  }

  public static Rval builtin_rule_odd_1(Rval a) { // odd
    if (a.id == idint) return (((Rint) a).v & 1) == 1 ? rtrue : rfalse;
    return null;
  }

  public static Rval builtin_rule_43_2(Rval a, Rval b) { // +
    if (a.id == idint && b.id == idint) return rint(((Rint) a).v + ((Rint) b).v);
    if (a.id == idreal && b.id == idreal) return rreal(((Rreal) a).v + ((Rreal) b).v);
    if (a.id == idint && b.id == idreal) return rreal(((Rint) a).v + ((Rreal) b).v);
    if (a.id == idreal && b.id == idint) return rreal(((Rreal) a).v + ((Rint) b).v);
    return null;
  }

  public static Rval builtin_rule_45_2(Rval a, Rval b) { // -
    if (a.id == idint && b.id == idint) return rint(((Rint) a).v - ((Rint) b).v);
    if (a.id == idreal && b.id == idreal) return rreal(((Rreal) a).v - ((Rreal) b).v);
    if (a.id == idint && b.id == idreal) return rreal(((Rint) a).v - ((Rreal) b).v);
    if (a.id == idreal && b.id == idint) return rreal(((Rreal) a).v - ((Rint) b).v);
    return null;
  }

  public static Rval builtin_rule_42_2(Rval a, Rval b) { // *
    if (a.id == idint && b.id == idint) return rint(((Rint) a).v * ((Rint) b).v);
    if (a.id == idreal && b.id == idreal) return rreal(((Rreal) a).v * ((Rreal) b).v);
    if (a.id == idint && b.id == idreal) return rreal(((Rint) a).v * ((Rreal) b).v);
    if (a.id == idreal && b.id == idint) return rreal(((Rreal) a).v * ((Rint) b).v);
    return null;
  }

  public static Rval builtin_rule_47_2(Rval a, Rval b) { // /
    if (a.id == idint && b.id == idint) return rint(((Rint) a).v / ((Rint) b).v);
    if (a.id == idreal && b.id == idreal) return rreal(((Rreal) a).v / ((Rreal) b).v);
    if (a.id == idint && b.id == idreal) return rreal(((Rint) a).v / ((Rreal) b).v);
    if (a.id == idreal && b.id == idint) return rreal(((Rreal) a).v / ((Rint) b).v);
    return null;
  }

  public static Rval builtin_rule_mod_2(Rval a, Rval b) { // %
    if (a.id == idint && b.id == idint) return rint(((Rint) a).v % ((Rint) b).v);
    return null;
  }

  public static Rval builtin_rule_60_2(Rval a, Rval b) { // <
    if (a.id == idint && b.id == idint) return (((Rint) a).v < ((Rint) b).v) ? rtrue : rfalse;
    if (a.id == idreal && b.id == idreal) return (((Rreal) a).v < ((Rreal) b).v) ? rtrue : rfalse;
    if (a.id == idint && b.id == idreal) return (((Rint) a).v < ((Rreal) b).v) ? rtrue : rfalse;
    if (a.id == idreal && b.id == idint) return (((Rreal) a).v < ((Rint) b).v) ? rtrue : rfalse;
    return null;
  }

  public static Rval builtin_rule_62_2(Rval a, Rval b) { // >
    if (a.id == idint && b.id == idint) return (((Rint) a).v > ((Rint) b).v) ? rtrue : rfalse;
    if (a.id == idreal && b.id == idreal) return (((Rreal) a).v > ((Rreal) b).v) ? rtrue : rfalse;
    if (a.id == idint && b.id == idreal) return (((Rint) a).v > ((Rreal) b).v) ? rtrue : rfalse;
    if (a.id == idreal && b.id == idint) return (((Rreal) a).v > ((Rint) b).v) ? rtrue : rfalse;
    return null;
  }

  public static Rval builtin_rule_real_1(Rval a) {
    if (a.id == idint) return rreal(((Rint) a).v);
    return null;
  }

  public static Rval builtin_rule_int_1(Rval a) {
    if (a.id == idreal) return rint((int) ((Rreal) a).v);
    return null;
  }

  public static Rval builtin_rule_61_2(Rval a, Rval b) { // = (structural equivalence)
    if (a == b) return rtrue; // cheap shortcut :)
    if (a.id != b.id) return rfalse;
    if (a.id == idint) return (((Rint) a).v == ((Rint) b).v) ? rtrue : rfalse;
    if (a.id >= idtreestart) return a.equal(b);
    if (a.id == idreal) return (((Rreal) a).v == ((Rreal) b).v) ? rtrue : rfalse;
    // future datatypes go here
    if (a.id != idbag) return rfalse;
    // comparing two bags. heinously expensive but supported anyway
    try {
      Rval[] x = ((Rbag) a).nfarray();
      Rval[] y = ((Rbag) b).nfarray();
      if (x.length != y.length) return rfalse;
      int ylength = y.length;
      outerloop:
      for (int i = 0; i < x.length; i++) {
        for (int j = 0; j < ylength; j++) {
          if (builtin_rule_61_2(x[i], y[j]) == rtrue) {
            ylength--;
            for (int k = j; k < ylength; k++) y[k] = y[k + 1];
            continue outerloop;
          }
        }
        return rfalse; // one value could not be found in the other
      }
    } catch (RemoteException e) {
      msg("bag equality: remote exception: " + e);
      return rfalse;
    }
    return rtrue; // all elements matched
    // no null, because not used by equal(), has to be complete
  }

  public static Rval builtin_rule_gfxwindow_2(Rval x, Rval y) {
    if (self.server != null) return self.server.marshall("gfxwindow", x, y, null, null, null);
    if (self.gfxc != null) return null;
    if (x.id != idint || y.id != idint) return null;
    Frame f;
    Canvas c;
    try {
      int xs = ((Rint) x).v;
      int ys = ((Rint) y).v;
      f = new Frame();
      f.setLayout(new BorderLayout());
      f.setBackground(Color.white);
      f.setTitle("Aardappel Graphics Output");
      c =
          new Canvas() {
            public void paint(Graphics g) {}
          };
      c.setBackground(Color.white);
      c.setSize(xs, ys);
      f.add(c, BorderLayout.CENTER);
      f.pack();
      f.setLocation(150, 150);
      f.addWindowListener(
          new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              Window w = e.getWindow();
              w.dispose();
              self.gfxc = null;
            }
          });
      f.setVisible(true);
    } catch (Exception e) {
      return rfalse;
    }
    self.gfxc = c;
    return rtrue;
  }

  public static Rval builtin_rule_plot_5(Rval x, Rval y, Rval r, Rval g, Rval b) {
    if (self.server != null) return self.server.marshall("plot", x, y, r, g, b);
    return plot(x, y, r, g, b);
  }

  public static synchronized Rval plot(Rval x, Rval y, Rval r, Rval g, Rval b) {
    if (self.gfxc == null) return null;
    if (x.id != idint || y.id != idint || r.id != idint || g.id != idint || b.id != idint)
      return null;
    try {
      Graphics gr = self.gfxc.getGraphics();
      if (gr == null) return null;
      gr.setColor(new Color(((Rint) r).v, ((Rint) g).v, ((Rint) b).v));
      int xc = ((Rint) x).v;
      int yc = ((Rint) y).v;
      gr.drawLine(xc, yc, xc, yc);
    } catch (Exception e) {
      return null;
    }
    return rvoid;
  }

  public static synchronized Rval builtin_rule_flatten_1(Rval x) throws RemoteException {
    if (x.id != idbag) return x;
    Rval[] a = ((Rbag) x).nfarray();
    Rbag b = getparent();
    for (int i = 0; i < a.length; i++) {
      b.addactive();
      b.addnormal(a[i]);
    }
    return rvoid;
  }
}
