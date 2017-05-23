package r;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;


public class AardappelServer extends UnicastRemoteObject implements AardappelServerIn {
  ThreadGroup tg = null;
  AardappelRuntime ar = null;
  static AardappelServer self;
  Rbag remotebag;

  public AardappelServer() throws RemoteException { self = this; };

  public void msg(String s) { System.out.println("aardappelserver: "+s); }

  public void startup(byte[] cldata) throws RemoteException {
    try {
      tg = new ThreadGroup("remote_threads");
      RuntimeLoader loader = new RuntimeLoader(cldata);
      Class cls = loader.loadClass();
      msg("received class data successfully");
      ar = (AardappelRuntime)cls.newInstance();
      ar.server = this;
    } catch(IllegalAccessException e) {
      msg("class not valid");
    } catch(InstantiationException e) {
      msg("couldn't instantiate");
    } catch(VerifyError e) {
      msg("internal error: JVM VerifyError");
    } catch(Exception e) {
      msg("exception: "+e);
      e.printStackTrace();
    }
  }
  public Rval marshall(String op, Rval a, Rval b, Rval c, Rval d, Rval e) {
    try {
      return remotebag.statebuiltin(op,a,b,c,d,e);
    } catch(RemoteException r) {
      msg("trouble marshalling state builtin: "+r);
      return null;
    }
  }

  public void marshallthread(Rbag p, int c, Rval f[]) {
    try {
      remotebag.distributethread(p,c,f);
    } catch(RemoteException r) {
      msg("trouble marshalling thread: "+r);
    };
  };

  public void exec(Rbag parent, int codeid, Rval freevars[]) throws RemoteException {
    if(tg==null || ar==null) {
      msg("internal problem: not initialised");
      System.exit(1);
    };
    remotebag = parent;
    ar.runthread(parent,codeid,freevars);
  }

  public void terminate() throws RemoteException {
    tg.stop();
    tg = null;
    ar.cleanup();
    ar = null;
  };

  public static void main(String args[]) {
    if(args.length!=1) { System.out.println("usage: java AardappelServer hostname"); System.exit(1); };
    System.setSecurityManager(new RMISecurityManager());
    Registry r;
    AardappelServer as;
    try {
      as = new AardappelServer();
      r = LocateRegistry.getRegistry(); //createRegistry(Registry.REGISTRY_PORT);
      as.msg("rmi registry located/created "+r);
      String name = "rmi://"+args[0]+"/TupleServer";
      System.out.println("registering "+name);
      try {
        Naming.rebind(name,as);
        System.out.println(name+" now set in registry");
      } catch(RemoteException e) {
        self.msg("Couldn't register object: "+e);
        e.printStackTrace();
      }
    } catch(RemoteException e) {
      self.msg("Couldn't locate nor create a rmi registry at port "+Registry.REGISTRY_PORT);
    } catch (Exception e) {
      self.msg("error: "+e.getMessage());
      e.printStackTrace();
    }
  }
}

