package r;

import java.rmi.*;

public interface RbagIn extends Remote {
  public Rval[] nfarray() throws RemoteException;

  public void addactive() throws RemoteException;

  public void addnormal(Rval x) throws RemoteException;

  public void waitnormalform() throws RemoteException;

  public Rval[] grab(int id[]) throws RemoteException;

  public void reject(Rval v[], int rejected) throws RemoteException;

  public void unreject(Rval v[]) throws RemoteException;

  public Rval statebuiltin(String op, Rval a, Rval b, Rval c, Rval d, Rval e)
      throws RemoteException;

  public void distributethread(Rbag p, int c, Rval f[]) throws RemoteException;
}
