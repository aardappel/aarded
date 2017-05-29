package r;

import java.rmi.*;

public interface AardappelServerIn extends Remote {
  public void startup(byte[] cldata) throws RemoteException;

  public void exec(Rbag parent, int codeid, Rval freevars[]) throws RemoteException;

  public void terminate() throws RemoteException;
}
