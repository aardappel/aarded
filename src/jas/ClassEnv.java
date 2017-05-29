
package jas;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;


/**
 * This is the place where all information about the class to
 * be created resides.
 *
 * @author $Author: kbs $
 * @version $Revision: 1.13 $
 */

public class ClassEnv implements RuntimeConstants
{
  int magic;
  short version_lo, version_hi;
  CP this_class, super_class;
  short class_access;
  Hashtable cpe, cpe_index;
  Vector interfaces;
  Vector vars;
  Vector methods;
  SourceAttr source;
  Vector generic;

  public ClassEnv()
  {
                                // Fill in reasonable defaults
    magic = JAVA_MAGIC;
    version_lo = (short) JAVA_MINOR_VERSION;
    version_hi = (short) JAVA_VERSION;
                                // Initialize bags
    cpe = new Hashtable();
    cpe_index = null;
    interfaces = new Vector();
    vars = new Vector();
    methods = new Vector();
    generic = new Vector();
  }

  /**
   * Define this class to have this name.
   * @param name CPE representing name for class. (This is usually
   * a ClassCP)
   */
  public void setClass(CP name)
  {  this_class = name; addCPItem(name); }
  /**
   * Define this class to have this superclass
   * @param name CPE representing name for class. (This is usually
   * a ClassCP)
   */
  public void setSuperClass(CP name)
  {  super_class = name; addCPItem(name); }

  /**
   * Set the class access for this class. Constants understood
   * by this are present along with the java Beta distribution.
   * @param access number representing access permissions for
   *        the entire class.
   * @see RuntimeConstants
   */
  public void setClassAccess(short access)
  { class_access  = access; }

  /**
   * Add this CP to the list of interfaces supposedly implemented by
   * this class. Note that the CP ought to be a ClassCP to make
   * sense to the VM.
   */

  public void addInterface(CP ifc)
  {
    addCPItem(ifc);
    interfaces.addElement(ifc);
  }

  /**
   * Add this to the list of interfaces supposedly implemented
   * by this class. Note that each CP is usually a ClassCP.
   * @param ilist An array of CP items representing the 
   *          interfaces implemented by this class.
   */
  public void addInterface(CP ilist[])
  {
    for (int i=0; i<ilist.length; i++)
      {
        interfaces.addElement(ilist[i]);
        addCPItem(ilist[i]);
      }
  }

  public void addField(Var v)
  {
    vars.addElement(v);
    v.resolve(this);
  }
  /**
   * Write the contents of the class.
   *
   * @param out DataOutputStream on which the contents are written.
   */
  public void write(DataOutputStream out)
    throws IOException, jasError
  {
				// Headers
    out.writeInt(magic);
    out.writeShort(version_lo);
    out.writeShort(version_hi);

				// cpe items
    int curidx = 1;
				// make up indices for entries
    cpe_index = new Hashtable();
    for (Enumeration e = cpe.elements(); e.hasMoreElements();)
      {
        CP tmp = (CP)(e.nextElement());
        cpe_index.put(tmp.getUniq(), new Integer(curidx));
        curidx++;
        if ((tmp instanceof LongCP) ||
            (tmp instanceof DoubleCP))
          curidx++;
      }
    out.writeShort((short)curidx);

				// Now write out all the entries
    for (Enumeration e = cpe.elements(); e.hasMoreElements();)
      {
        CP now = (CP) (e.nextElement());
        now.write(this, out);
      }

				// Class hierarchy/access
    out.writeShort(class_access);
    out.writeShort(getCPIndex(this_class));
    out.writeShort(getCPIndex(super_class));
                                // interfaces
    out.writeShort(interfaces.size());
    for (Enumeration e = interfaces.elements(); e.hasMoreElements();)
      {
        CP c = (CP)(e.nextElement());
        out.writeShort(getCPIndex(c));
      }
                                // variables
    out.writeShort(vars.size());
    for (Enumeration e = vars.elements(); e.hasMoreElements();)
      {
        Var v = (Var)(e.nextElement());
        v.write(this, out);
      }

                                // methods
    out.writeShort(methods.size());
    for (Enumeration e = methods.elements(); e.hasMoreElements();)
      {
        Method m = (Method)(e.nextElement());
        m.write(this, out);
      }
                                // additional attributes
    short numExtra = 0;
    if (source != null)
      { numExtra = 1; }
    numExtra += generic.size();

    out.writeShort(numExtra);
    if (source != null)
      { source.write(this, out); }
    for (Enumeration gen=generic.elements(); gen.hasMoreElements(); )
      {
	GenericAttr gattr = (GenericAttr)gen.nextElement();
	gattr.write(this, out);
      }
    out.flush();
  }

  /**
   * This is the method to add CPE items to a class. CPE items for
   * a class are "uniquefied". Ie, if you add a CPE items whose
   * contents already exist in the class, only one entry is finally
   * written out when the class is written.
   *
   * @param cp Item to be added to the class
   */

  public void addCPItem(CP cp)
  {
    String uniq = cp.getUniq();
    CP intern;

    if ((intern = (CP)(cpe.get(uniq))) == null)
      {
				// add it
	cpe.put(uniq, cp);
				// resolve it so it adds anything
				// which it depends on
	cp.resolve(this);
      }
  }

  /**
   * Here is where code gets added to a class.
   * @param acc method_access permissions, expressed with some combination
   *       of the values defined in RuntimeConstants
   * @param name Name of the method
   * @param sig Signature for the method
   * @param code Actual code for the method
   * @param ex Any exception attribute to be associated with method
   */
  public void
  addMethod(short acc, String name, String sig, CodeAttr code, ExceptAttr ex)
  {
    Method x = new Method(acc, new AsciiCP(name), new AsciiCP(sig),
                          code, ex);
    x.resolve(this);
    methods.addElement(x);
  }

  /**
   * Add an attribute specifying the name of the source file
   * for the class
   * @param source SourceAttribute specifying the source for the file
   */

  public void setSource(SourceAttr source)
  { this.source = source; source.resolve(this); }

  /**
   * Add an attribute specifying the name of the source file
   * for the clas.
   * @param source String with the name of the class
   */
  public void setSource(String source)
  { this.source = new SourceAttr(source); this.source.resolve(this); }

  /**
   * Add a generic attribute to the class file. A generic attribute
   * contains a stream of uninterpreted bytes which is ignored by
   * the VM (as long as its name doesn't conflict with other names
   * for attributes that are understood by the VM)
   */
  public void addGenericAttr(GenericAttr g)
  { generic.addElement(g); g.resolve(this); }

  /**
   * This allows more control over generating CP's for methods
   * if you feel so inclined.
   */
  public void addMethod(Method m)
  {
    m.resolve(this);
    methods.addElement(m);
  }

  short getCPIndex(CP cp)
    throws jasError
  {
    if (cpe_index == null)
      throw new jasError("Internal error: CPE index has not been generated");

    Integer idx = (Integer)(cpe_index.get(cp.getUniq()));
    if (idx == null)
      throw new jasError("Item " + cp + " not in the class");
    return ((short)(idx.intValue()));
  }
}
