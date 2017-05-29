/**
 * CodeAttr's are used as a bag to hold lists of instructions
 * until it is time to put them into a Class.
 * @see ClassEnv#addMethod
 * @author $Author: kbs $
 * @version $Revision: 1.6 $
 */

package jas;

import java.io.*;
import java.util.*;


public class CodeAttr
{
  static CP attr = new AsciiCP("Code");
  short stack_size, num_locals;
  int code_size;
  Vector insns;
  Hashtable insn_pc;
  Catchtable ctb;
  LineTableAttr ltab;
  LocalVarTableAttr lvar;
  Vector generic;

  /**
   * Create a new bag. Add instructions with the addInsn() method,
   * set the catch table with the setCatchTable() method.
   * @see Insn
   * @see Catchtable
   * @see ClassEnv#addMethod
   */

  public CodeAttr()
  {
    this.stack_size = 1;
    this.num_locals = 1;
    this.ctb = null;
    this.insns = new Vector();
    generic = new Vector();
  }
  /**
   * Set the catchtable for this code
   */
  public void setCatchtable(Catchtable ctb)
  { this.ctb = ctb; }

  /**
   * Set the line number table for this method
   */

  public void setLineTable(LineTableAttr ltab)
  { this.ltab = ltab; }

  /**
   * Set the local variable information for this method
   */

  public void setLocalVarTable(LocalVarTableAttr lvar)
  { this.lvar = lvar; }

  /**
   * Add a generic attribute to the method. A generic attribute
   * contains a stream of uninterpreted bytes which is ignored by
   * the VM (as long as its name doesn't conflict with other names
   * for attributes that are understood by the VM)
   */
  public void addGenericAttr(GenericAttr g)
  { generic.addElement(g); }


  /**
   * Append a new Insn to this code. Insn's are sequentially
   * stored, in the order in which this method is called. You
   * can't reorder code fragments after you've added it here.
   */
  public void addInsn(Insn insn)
  { insns.addElement(insn); }

  public void setStackSize(short stack_size)
  { this.stack_size = stack_size; }
  public void setVarSize(short num_vars)
  { num_locals = num_vars; }

  void resolve(ClassEnv e)
  {
                                // propagate this resolution to
                                // the insns and catch table, so
                                // that any CP's referenced by them
                                // also get added.
    e.addCPItem(attr);
    for (Enumeration en = insns.elements(); en.hasMoreElements();)
      {
        Insn i = (Insn)(en.nextElement());
        i.resolve(e);
      }
    if (ctb != null)  ctb.resolve(e);
    if (ltab != null) ltab.resolve(e);
    if (lvar != null) lvar.resolve(e);
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
      {
	GenericAttr gattr = (GenericAttr)gen.nextElement();
	gattr.resolve(e);
      }
  }

  int getPc(Insn i)
     throws jasError
  {
    if (insn_pc == null)
      throw new jasError("Internal error, insn_pc has not been initialized");
    Integer tmp;
    if (i instanceof Label)
      {
        tmp = (Integer)(insn_pc.get(((Label)i).id));
      }
    else
      {
        tmp = (Integer)(insn_pc.get(i));
      }
    if (tmp == null)
      throw new jasError(i + " has not been added to the code");
    return tmp.intValue();
  }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
                                // First, resolve all labels and
                                // compute total size
    int code_size = 0;
    insn_pc = new Hashtable();
    for (Enumeration en = insns.elements(); en.hasMoreElements();)
      {
        Insn now = (Insn)(en.nextElement());
        if (now instanceof Label)
          {
            insn_pc.put(((Label)now).id, new Integer(code_size));
          }
        else
          { insn_pc.put(now, new Integer(code_size)); }
        code_size += now.size(e, this);
      }
    int total_size = code_size;
    if (ctb != null) total_size += ctb.size();
    if (ltab != null) total_size += ltab.size();
    if (lvar != null) total_size += lvar.size();
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
      {
	GenericAttr gattr = (GenericAttr)(gen.nextElement());
	total_size += gattr.size();
      }

                                // extra headers
    total_size += 12;


    out.writeShort(e.getCPIndex(attr));
    out.writeInt(total_size);
    out.writeShort(stack_size);
    out.writeShort(num_locals);
    out.writeInt(code_size);
    for (Enumeration en = insns.elements(); en.hasMoreElements();)
      {
        Insn now = (Insn)(en.nextElement());
        now.write(e, this, out);
      }
    if (ctb != null)
      { ctb.write(e, this, out); }
    else
      { out.writeShort(0); }
    short extra = 0;
    if (ltab != null) extra++;
    if (lvar != null) extra++;
    extra += generic.size();
    out.writeShort(extra);
    if (ltab != null)
      { ltab.write(e, this, out); }
    if (lvar != null)
      { lvar.write(e, this, out); }
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
      {
	GenericAttr gattr = (GenericAttr)gen.nextElement();
	gattr.write(e, out);
      }
  }

  public String toString()
  { return ("<#code-attr>"); }
}
