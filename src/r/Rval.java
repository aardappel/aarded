package r;

import java.io.*;

public class Rval implements Serializable {
  public int id;
  public Rval(int i) { id = i; }
  public String toString() { return id+"()"; }
  public Object code(Access a) { return a.rv(this); }
  public Rval equal(Rval a) { return AardappelRuntime.rtrue; }; // assumes a has equal id
};

