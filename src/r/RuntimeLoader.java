package r;

import java.util.*;

public class RuntimeLoader extends ClassLoader {
  private Hashtable cache = new Hashtable();
  private byte[] classdata;
  private Class loadedClass;

  public RuntimeLoader(byte[] data) { this.classdata = data; }

  public Class loadClass() {
    Class cl = defineClass(null, classdata, 0, classdata.length);
    loadedClass = cl;
    resolveClass(cl);
    cache.put(cl.getName(), cl);
    return cl;
  }

  public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class cl = (Class)cache.get(name);
    if (cl == null) {
      try {
        cl = findSystemClass(name);
        cache.put(name, cl);
      } catch (ClassNotFoundException ex) {
        if (loadedClass != null && name.equals(loadedClass.getName())) {
          cl = loadedClass;
        } else {
          throw ex;
        }
      }
    }
    if (resolve) resolveClass(cl);
    return cl;
  }

}

