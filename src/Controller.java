import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Controller {
    /* Controller Explained
    *
    * The Controller is the aspect that controls the whole simulation
    * It is the one that takes the assembled code, and tries to run it
    * It sends it to the ALU and then functions depending on that response
    * It contains all the registers and the access of the memory
    *
     */

    private ALU alu;
    private Memory memory;
    private Register register;

    // Program Counter of which line the Program points to in memory
    private int programCounter;

    // Instruction Register stores the instruction at line in programCounter
    private String instructionRegister;

    // Constructor of Controller
    // -----------------------------------------------------------------------------------------------------------------
    public Controller () {
        alu      = ALU.getInstance();
        memory   = Memory.getInstance();
        register = Register.getInstance();

        programCounter = 0;
        instructionRegister = "";
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Operate Instruction from Instruction Register
    // -----------------------------------------------------------------------------------------------------------------
    public void operateInstructionRegister () {
        // Point out which format this is
        // To tell the ALU what to do
        int opCode = Integer.parseInt(instructionRegister.substring(0, 6), 2);
        if (opCode == 0) { // Check if it is R format
            int rs = Integer.parseInt(instructionRegister.substring(6, 11), 2);
            int rt = Integer.parseInt(instructionRegister.substring(11, 16), 2);
            int rd = Integer.parseInt(instructionRegister.substring(16, 21), 2);

            // Ignoring Shifting since we have no SLL or SRL

            // Get Operation's Function and Switch it Between Cases
            // Taken from the 'resources/instructions.csv' to know
            // Which instruction the ALU must perform
            int func = Integer.parseInt(instructionRegister.substring(26, 32), 2);
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
        } else if (opCode == 2) { // Check if it is J format
            int address = Integer.parseInt(instructionRegister.substring(6, 26), 2);

            alu.jump(address);
        } else { // Then this is definitely I format
            int rs = Integer.parseInt(instructionRegister.substring(6, 11), 2);
            int rd = Integer.parseInt(instructionRegister.substring(11, 16), 2);

            int address = Integer.parseInt(instructionRegister.substring(16, 32), 2);

            switch (opCode) {
                case 4:
                    alu.beq(rd, rs, address);
                    break;
                case 5:
                    alu.bne(rd, rs, address);
                    break;
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
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Controller MIPS Simulator
    // -----------------------------------------------------------------------------------------------------------------
    public void simulate (File inputFile, File outputFile) {
        Assembler assembler = new Assembler(inputFile);

        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(outputFile));

            String[] instruction;
            while ((instruction = assembler.nextInstruction()) != null) {
                instructionRegister = memory.loadInstruction(instruction[0]);
                operateInstructionRegister();

                printWriter.println(instruction[1]);
                printWriter.println(instruction[0]);
                printWriter.println();
                printWriter.println(register);
                printWriter.println();
                printWriter.println("---------------------------------------------------------------------------------");
                printWriter.println();

                System.out.println(memory);
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println();

                printWriter.flush();
            }

            printWriter.close();
        } catch (Exception e) {
//            Controller.exit("Controller : " + e.getMessage());
            e.printStackTrace();
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Program Stopper
    // -----------------------------------------------------------------------------------------------------------------
    public static void exit(String message) {
        System.out.println(message);
        System.exit(-1);
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Controller Executor
    // -----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        String fileName = "loadnstore";

        File inputFile  = new File("in/" + fileName + ".in");
        File outputFile = new File("out/" + fileName + ".out");

        if (!inputFile.exists())
            Controller.exit("Input file does not exist, aborting process");

        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (Exception e) {
                Controller.exit("Not enough permissions to create output file");
            }
        }

        Controller controller = new Controller();
        controller.simulate(inputFile, outputFile);
    }
    // -----------------------------------------------------------------------------------------------------------------
}
