public class ALU {
    private static ALU alu;

    private Memory memory;
    private Register register;

    // ALU Constructor
    // -----------------------------------------------------------------------------------------------------------------
    public ALU () {
        memory   = Memory.getInstance();
        register = Register.getInstance();
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Default Memory Instance
    // -----------------------------------------------------------------------------------------------------------------
    public static synchronized ALU getInstance() {
        if (alu == null)
            alu = new ALU();
        return alu;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // ALU R Format Operations
    // -----------------------------------------------------------------------------------------------------------------
    public void add(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) + register.read(rt));
    }

    public void sub(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) - register.read(rt));
    }

    public void and(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) & register.read(rt));
    }

    public void or(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) | register.read(rt));
    }

    public void slt(int rd, int rs, int rt) {
        boolean isSlt = register.read(rs) < register.read(rt);

        register.write(rd, (isSlt) ? 1 : 0);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // ALU I Format Operations
    // -----------------------------------------------------------------------------------------------------------------
    public int beq(int rs, int rt, int address) {
        if (register.read(rs) == register.read(rt))
            return address;
        else return -1;
    }

    public int bne(int rs, int rt, int address) {
        if (register.read(rs) != register.read(rt))
            return address;
        else return -1;
    }

    public void addi(int rd, int rs, int constant) {
        register.write(rd, register.read(rs) + constant);
    }

    public void lw(int rd, int rs, int address) {
        System.out.println(register.read(rs) + address);
        register.write(rd, Integer.parseInt(memory.getInstruction(register.read(rs) + address), 2));
    }

    public void sw(int rd, int rs, int address) {
        memory.write(register.read(rd) + address, Assembler.toBitString(register.read(rs), 32));
    }
    // -----------------------------------------------------------------------------------------------------------------

    // ALU J Format Operations
    // -----------------------------------------------------------------------------------------------------------------
    public void jump(int address) {

    }
    // -----------------------------------------------------------------------------------------------------------------
}
