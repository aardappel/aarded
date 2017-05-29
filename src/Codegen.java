import jas.*;

class Codegen implements RuntimeConstants {
  private CodeAttr cc;

  void startmethod() {
    cc = new CodeAttr();
  }

  void endmethod(int ss, int vs, int acc, String id, String sig, ClassEnv cl) {
    cc.setStackSize((short) ss);
    cc.setVarSize((short) vs);
    cl.addMethod((short) acc, id, sig, cc, null);
  }

  void gendrop() throws jasError {
    cc.addInsn(new Insn(opc_pop));
  }

  void gendrop2() throws jasError {
    cc.addInsn(new Insn(opc_pop2));
  }

  void gendup() throws jasError {
    cc.addInsn(new Insn(opc_dup));
  }

  void gendup2() throws jasError {
    cc.addInsn(new Insn(opc_dup2));
  }

  void gendupx1() throws jasError {
    cc.addInsn(new Insn(opc_dup_x1));
  }

  void gendupx2() throws jasError {
    cc.addInsn(new Insn(opc_dup_x2));
  }

  void genswap() throws jasError {
    cc.addInsn(new Insn(opc_swap));
  }

  void gengetfield(FieldCP f) throws jasError {
    cc.addInsn(new Insn(opc_getfield, f));
  }

  void genputfield(FieldCP f) throws jasError {
    cc.addInsn(new Insn(opc_putfield, f));
  }

  void gengetstat(FieldCP f) throws jasError {
    cc.addInsn(new Insn(opc_getstatic, f));
  }

  void genldc(CP f) throws jasError {
    cc.addInsn(new Insn(opc_ldc, f));
  }

  void gencast(ClassCP c) throws jasError {
    cc.addInsn(new Insn(opc_checkcast, c));
  }

  void gennew(ClassCP c) throws jasError {
    cc.addInsn(new Insn(opc_new, c));
  }

  void genaarray(ClassCP c) throws jasError {
    cc.addInsn(new Insn(opc_anewarray, c));
  }

  void geniarray() throws jasError {
    cc.addInsn(new Insn(opc_newarray, T_INT));
  }

  void genalen() throws jasError {
    cc.addInsn(new Insn(opc_arraylength));
  }

  void genaastore() throws jasError {
    cc.addInsn(new Insn(opc_aastore));
  }

  void genaaload() throws jasError {
    cc.addInsn(new Insn(opc_aaload));
  }

  void geniastore() throws jasError {
    cc.addInsn(new Insn(opc_iastore));
  }

  void geniaload() throws jasError {
    cc.addInsn(new Insn(opc_iaload));
  }

  void genjsrstat(MethodCP mcp) throws jasError {
    cc.addInsn(new Insn(opc_invokestatic, mcp));
  }

  void genjsrspec(MethodCP mcp) throws jasError {
    cc.addInsn(new Insn(opc_invokespecial, mcp));
  }

  void genjsrvirt(MethodCP mcp) throws jasError {
    cc.addInsn(new Insn(opc_invokevirtual, mcp));
  }

  void genjsrnonv(MethodCP mcp) throws jasError {
    cc.addInsn(new Insn(opc_invokenonvirtual, mcp));
  }

  void genareturn() throws jasError {
    cc.addInsn(new Insn(opc_areturn));
  }

  void genreturn() throws jasError {
    cc.addInsn(new Insn(opc_return));
  }

  void genlab(Label l) {
    cc.addInsn(l);
  }

  void gentable(int s, int e, Label def, Label tab[]) throws jasError {
    cc.addInsn(new TableswitchInsn(s, e, def, tab));
  }

  void gengoto(Label l) throws jasError {
    cc.addInsn(new Insn(opc_goto, l));
  }

  void gennull() throws jasError {
    cc.addInsn(new Insn(opc_aconst_null));
  }

  void genisub() throws jasError {
    cc.addInsn(new Insn(opc_isub));
  }

  void genis0(Label l) throws jasError {
    cc.addInsn(new Insn(opc_ifeq, l));
  }

  void genisnull(Label l) throws jasError {
    cc.addInsn(new Insn(opc_aconst_null));
    cc.addInsn(new Insn(opc_if_acmpeq, l));
  }

  void genisnnull(Label l) throws jasError {
    cc.addInsn(new Insn(opc_aconst_null));
    cc.addInsn(new Insn(opc_if_acmpne, l));
  }

  void genaload(int a) throws jasError {
    if (a >= 0 && a <= 3) {
      cc.addInsn(new Insn(a + opc_aload_0));
    } else {
      cc.addInsn(new Insn(opc_aload, a));
    }
  }

  void geniload(int a) throws jasError {
    if (a >= 0 && a <= 3) {
      cc.addInsn(new Insn(a + opc_iload_0));
    } else {
      cc.addInsn(new Insn(opc_iload, a));
    }
  }

  void genastore(int n) throws jasError {
    if (n >= 0 && n <= 3) {
      cc.addInsn(new Insn(n + opc_astore_0));
    } else {
      cc.addInsn(new Insn(opc_astore, n));
    }
  }

  void genval(int i) throws jasError {
    if (i >= -1 && i <= 5) {
      cc.addInsn(new Insn(i + 3));
    } else if (i >= -128 && i <= 127) {
      cc.addInsn(new Insn(opc_bipush, i));
    } else if (i >= -0x8000 && i < 0x7FFF) {
      cc.addInsn(new Insn(opc_sipush, i));
    } else {
      cc.addInsn(new Insn(opc_ldc, new IntegerCP(i)));
    }
  }

  void gendouble(double r) throws jasError {
    cc.addInsn(new Insn(opc_ldc2_w, new DoubleCP(r)));
  }

  void gencmpne(int val, Label neq) throws jasError {
    if (val == 0) {
      cc.addInsn(new Insn(opc_ifne, neq));
    } else {
      genval(val);
      cc.addInsn(new Insn(opc_if_icmpne, neq));
    }
  }
}
