package r;

import java.rmi.*;

public class Rbag extends Rval {
  public static final int idc = 1;
  public RbagIn ts;

  public Rbag() { super(idc); }
  public void createTS() throws RemoteException { ts = new RbagTS(); }

  public String toString() { return "{}"; }
  public Object code(Access a) { return a.rv(this); }

  public Rval[] nfarray() throws RemoteException { return ts.nfarray(); }
  public void addactive() throws RemoteException { ts.addactive(); }
  public void addnormal(Rval x) throws RemoteException { ts.addnormal(x); }
  public void waitnormalform() throws RemoteException { ts.waitnormalform(); }
  public Rval[] grab(int id[]) throws RemoteException { return ts.grab(id); }
  public void reject(Rval v[], int rejected) throws RemoteException { ts.reject(v,rejected); }
  public void unreject(Rval v[]) throws RemoteException { ts.unreject(v); }
  public Rval statebuiltin(String op, Rval a, Rval b, Rval c, Rval d, Rval e) throws RemoteException { return ts.statebuiltin(op,a,b,c,d,e); };
  public void distributethread(Rbag p, int c, Rval f[]) throws RemoteException { ts.distributethread(p,c,f); }
}


