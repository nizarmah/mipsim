public class Register {
    /* Register Explained
    *
    * This deals with all registers
    * It changes the values of the registers and reads them
    * It also returns the number of the register
    * depending on the name used, such as $s7
    *
     */
    private static Register register;

    private String[] registers;

    // Register Constructor
    // -----------------------------------------------------------------------------------------------------------------
    public Register () {
        registers = new String[32];
        for (int i = 0; i < registers.length; i++)
            registers[i] = Assembler.toBitString(0, 32);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Default Register Instance
    // -----------------------------------------------------------------------------------------------------------------
    public static synchronized Register getInstance() {
        if (register == null)
            register = new Register();
        return register;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Read from Register
    // -----------------------------------------------------------------------------------------------------------------
    public int read(int registerNum) {
        return Integer.parseInt(registers[registerNum], 2);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Write to Register
    // -----------------------------------------------------------------------------------------------------------------
    public void write(int registerNum, int value) {
        registers[registerNum] = Assembler.toBitString(value, 32);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Get Register Number
    // -----------------------------------------------------------------------------------------------------------------
    public static int getRegister(String register) {
        // Returns Register's index in the Array of 32 registers
        // According to how the register is named

        if (register.equals("$0"))
            return 0;

        String[] info = register.split("");

        if (info.length < 3)
            Controller.exit("Register Error : getRegister : Invalid Register Name");

        int value;
        switch (info[1]) {
            case "s":
                // Check if Register is $sp || $s0 -> $s7
                try {
                    // If Integer Parses, then there is no P in info[2]
                    value = Integer.parseInt(info[2]);

                    if (value > 7)
                        Controller.exit("Register Error : getRegister : Register $s only accepts nums 0 -> 7");
                    else return 16 + value;
                } catch (Exception e) { return 28; }
                break;
            case "t":
                value = Integer.parseInt(info[2]);

                if (value > 9)
                    Controller.exit("Register Error : getRegister : Register $t only accepts nums 0 -> 9");
                else if (value < 8)
                    return 8 + value;
                else return 24 + (value - 8);
                break;
            case "a":
                value = Integer.parseInt(info[2]);

                if (value > 3)
                    Controller.exit("Register Error : getRegister : Register $a only accepts nums 0 -> 3");
                else return 4 + value;
                break;
            case "v":
                value = Integer.parseInt(info[2]);

                if (value > 1)
                    Controller.exit("Register Error : getRegister : Register $v only accepts nums 0 - 1");
                else return 2 + value;
                break;
            case "g":
                return 28;
            case "f":
                return 30;
            case "r":
                return 31;
            default:
                return 0;
        }

        return 0;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Register to String Dump
    // -----------------------------------------------------------------------------------------------------------------
    public String toString () {
        String str = "";

        for (int i = 0; i < registers.length; i++) {
            if (i > 0)
                str += "\n";

            str += ((i < 10) ? "0" : "") + i + " : " + registers[i];
        }

        return str;
    }
    // -----------------------------------------------------------------------------------------------------------------
}
