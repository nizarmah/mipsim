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

    // Executing Instruction Register
    // -----------------------------------------------------------------------------------------------------------------
    public int executeInstruction(int[] instruction) {
        // Executes the Instruction provided
        // This function understands the instruction
        // And then calls the correct part of the ALU to deal with it
        // It knows which part is the correct part using
        // The Operation Code and the Function Code

        int opCode = instruction[0];
        if (opCode == 0) {
            int rs = instruction[1];
            int rt = instruction[2];
            int rd = instruction[3];

            // ignoring shift because we have no SLL or SRL

            int func = instruction[5];

            switch (func) {
                case 32:
                    alu.add(rd, rs, rt);
                    break;
                case 34:
                    alu.sub(rd, rs, rt);
                    break;
                case 36:
                    alu.and(rd, rs, rt);
                    break;
                case 37:
                    alu.or(rd, rs, rt);
                    break;
                case 42:
                    alu.slt(rd, rs, rt);
                    break;
            }

            return -1;
        } else if (opCode == 2) {
            return instruction[1];
        } else {
            int rs = instruction[1];
            int rd = instruction[2];

            int address = instruction[3];

            switch (opCode) {
                case 4:
                    return alu.beq(rd, rs, address);
                case 5:
                    return alu.bne(rd, rs, address);
                case 8:
                    alu.addi(rd, rs, address);
                    break;
                case 35:
                    alu.lw(rd, rs, address);
                    break;
                case 43:
                    alu.sw(rd, rs, address);
                    break;
            }

            return -1;
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // ALU R Format Operations
    // -----------------------------------------------------------------------------------------------------------------
    private void add(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) + register.read(rt));
    }

    private void sub(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) - register.read(rt));
    }

    private void and(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) & register.read(rt));
    }

    private void or(int rd, int rs, int rt) {
        register.write(rd, register.read(rs) | register.read(rt));
    }

    private void slt(int rd, int rs, int rt) {
        boolean isSlt = register.read(rs) < register.read(rt);

        register.write(rd, (isSlt) ? 1 : 0);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // ALU I Format Operations
    // -----------------------------------------------------------------------------------------------------------------
    private int beq(int rs, int rt, int address) {
        if (register.read(rs) == register.read(rt))
            return address;
        else return -1;
    }

    private int bne(int rs, int rt, int address) {
        if (register.read(rs) != register.read(rt))
            return address;
        else return -1;
    }

    private void addi(int rd, int rs, int constant) {
        register.write(rd, register.read(rs) + constant);
    }

    private void lw(int rd, int rs, int address) {
        register.write(rd, Integer.parseInt(memory.getInstruction(register.read(rs) + address), 2));
    }

    private void sw(int rd, int rs, int address) {
        memory.write(register.read(rd) + address, Assembler.toBitString(register.read(rs), 32));
    }
    // -----------------------------------------------------------------------------------------------------------------
}
