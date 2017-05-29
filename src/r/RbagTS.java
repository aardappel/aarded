package r;

import java.net.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class RbagTS extends UnicastRemoteObject implements RbagIn {
  TupleSpaceDiv normals[];
  int stillactive = 0, blocked = 0, numnormals = 0;
  boolean busy = false;
  boolean nf = false;
  static final boolean dbg = false;

  public RbagTS() throws RemoteException {
    normals = new TupleSpaceDiv[AardappelRuntime.self.maxtreeid];
  }

  public synchronized Rval[] nfarray() throws RemoteException {
    Rval a[] = new Rval[numnormals];
    int cur = 0;
    for (int i = 0; i < normals.length; i++) {
      TupleSpaceDiv t = normals[i];
      if (t != null) {
        for (int j = 0; j < t.normals.size(); j++) a[cur++] = (Rval) t.normals.elementAt(j);
        if (t.rejects != null)
          for (int j = 0; j < t.rejects.size(); j++) a[cur++] = (Rval) t.rejects.elementAt(j);
      }
    }
    if (cur != a.length) throw new Error("nfa");
    return a;
  }

  public synchronized void addactive() throws RemoteException {
    stillactive++;
    if (dbg) System.out.println("addactive: " + stillactive);
  }

  public synchronized void addnormal(Rval x) throws RemoteException {
    if (dbg) System.out.println("addnormal start: " + x);
    stillactive--;
    if (x.id != AardappelRuntime.idvoid) {
      TupleSpaceDiv t;
      if ((t = normals[x.id]) == null) t = normals[x.id] = new TupleSpaceDiv();
      t.normals.addElement(x);
      numnormals++;
    }
    notifyAll(); // for both grab & waitnf
    if (dbg) System.out.println("addnormal end: " + stillactive);
  }

  public synchronized void waitnormalform() throws RemoteException { // used by bag exp
    if (dbg) System.out.println("waitnormal");
    while (stillactive != 0)
      try {
        wait();
      } catch (InterruptedException e) {
      }
  }

  public synchronized Rval[] grab(int id[]) throws RemoteException {
    if (dbg) System.out.println("grab start: " + id);
    start:
    for (; ; ) {
      if (nf) return null;
      if (busy)
        try {
          if (dbg) System.out.println("grab busy");
          wait();
        } catch (InterruptedException e) {
          continue start;
        }
      for (int i = 0; i < id.length; i++) {
        TupleSpaceDiv t;
        if ((t = normals[id[i]]) == null) t = normals[id[i]] = new TupleSpaceDiv();
        if (t.normals.size() == 0) {
          if (dbg) System.out.println("grab block");
          if (blocked == stillactive - 1) { // last tree to go blocked -> bag nf
            if (dbg) System.out.println("grab last");
            nf = true;
            notifyAll();
            return null;
          }
          blocked++;
          try {
            wait();
          } catch (InterruptedException e) {
          }
          blocked--;
          if (dbg) System.out.println("grab wait done");
          continue start;
        }
      }
      break;
    }
    Rval rvs[] = new Rval[id.length];
    for (int i = 0; i < id.length; i++) {
      TupleSpaceDiv t = normals[id[i]];
      rvs[i] = (Rval) t.normals.lastElement();
      t.normals.setSize(t.normals.size() - 1);
    }
    busy = true;
    return rvs;
  }

  public synchronized void reject(Rval v[], int rejected) throws RemoteException {
    if (dbg) System.out.println("reject: " + v);
    for (int i = 0; i < v.length; i++) {
      TupleSpaceDiv t = normals[v[i].id];
      if (i == rejected) {
        if (t.rejects == null) t.rejects = new Vector();
        t.rejects.addElement(v[i]);
      } else {
        t.normals.addElement(v[i]);
      }
    }
    busy = false;
  }

  public synchronized void unreject(Rval v[]) throws RemoteException {
    if (dbg) System.out.println("unreject: " + v);
    for (int i = 0; i < v.length; i++) {
      TupleSpaceDiv t = normals[v[i].id];
      if (t.rejects != null) {
        for (int j = 0; j < t.rejects.size(); j++) t.normals.addElement(t.rejects.elementAt(j));
        t.rejects.removeAllElements();
      }
    }
    busy = false;
    numnormals -= v.length;
  }

  public Rval statebuiltin(String op, Rval a, Rval b, Rval c, Rval d, Rval e)
      throws RemoteException {
    if (op.compareTo("gfxwindow") == 0) {
      return AardappelRuntime.builtin_rule_gfxwindow_2(a, b);
    } else if (op.compareTo("plot") == 0) {
      return AardappelRuntime.builtin_rule_plot_5(a, b, c, d, e);
    } else {
      AardappelRuntime.msg("remote state builtin problem: " + op);
    }
    return null;
  }

  public void distributethread(Rbag p, int c, Rval f[]) throws RemoteException {
    AardappelRuntime.newthread(p, c, f);
  }
}

class TupleSpaceDiv {
  public Vector normals = new Vector();
  public Vector rejects = null;
}
