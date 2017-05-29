package r;

import java.rmi.*;
import java.util.*;

public class Rthread extends Thread {
  int id;
  Rval vars[];
  public Rbag p;
  AardappelRuntime rt;

  public Rthread(int codeid, Rval freevars[], Rbag parent, AardappelRuntime ar)
      throws RemoteException {
    id = codeid;
    vars = freevars;
    p = parent;
    rt = ar;
    p.addactive();
  }

  public void run() {
    Rval v = rt.runcode(id, vars);
    try {
      p.addnormal(v);
    } catch (RemoteException e) {
      System.out.println("rthread.run: remote exception: " + e.getMessage());
    }
  }
}
