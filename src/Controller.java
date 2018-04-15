import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private Assembler assembler;

    private PrintWriter printWriter;

    // Instruction Register stores the instruction we are on currently
    private String instructionRegister;

    volatile private boolean jumpPipeline;
    private final Lock pipelineLock = new ReentrantLock();

    // Constructor of Controller
    // -----------------------------------------------------------------------------------------------------------------
    public Controller (File inputFile, File outputFile) {
        alu       = ALU.getInstance();
        memory    = Memory.getInstance();
        register  = Register.getInstance();
        assembler = new Assembler(inputFile);

        try {
            printWriter = new PrintWriter(outputFile);
        } catch (Exception e) {
            this.exit(e.getMessage());
        }

        instructionRegister = "";
    }
    // -----------------------------------------------------------------------------------------------------------------

    public void print(String[] instruction) {
        printWriter.println();

        printWriter.println(instruction[1]);
        printWriter.println(instruction[0]);
        printWriter.println();

        printWriter.println(register);
        printWriter.println();

        printWriter.println("------------------------------------------------------------------------------------");

        printWriter.flush();
    }

    // Controller MIPS Simulation
    // -----------------------------------------------------------------------------------------------------------------
    public void startSimulation () {
        String[] instruction;

        // Keep track of Current Register and Of Old Register
        // In Pipelining, we need to make sure that Instructions are not dependent on each other
        // Because if they are, we need to delay the other and thus, delay all the other instructions
        int[] registerInstruction;
        int[] previousInstruction = null;

        // Also we need to make sure that the Pipeline won't be jumping or changing Sequence
        // Because if it is, we don't want to end up decoding the instruction after
        // Since we are not gonna be going to it, but jumping to a different one
        jumpPipeline = false;


        // Keep going through the program until we have no further instructions
        while ((instruction = assembler.nextInstruction()) != null) {
            // Load the instruction into Memory and save it into the InstructionRegister
            // Since Memory.loadInstruction returns the Instruction[0] in the parameter
            instructionRegister = memory.loadInstruction(instruction[0]);
            // Decode the Instruction using the Assembler, and store it
            registerInstruction = assembler.decodeInstruction(instructionRegister);

            // Setting some Temporary Variables in order to use them in the Thread
            final String[] instructionTemp = instruction;
            final int[] registerInstructionTemp = registerInstruction;

            // If the previous Instruction was a Jump
            // Then do not proceed by executing or Decoding the Statement
            if (registerInstruction[0] == 2) {
                // Therefore, we make the ALU reset to the specified Address
                // That address will be returned from the ALU Executed Instruction
                assembler.jumpTo(alu.executeInstruction(registerInstruction));

                // Removing Previous Instruction for this Operation
                // Because we are sure that Jump isn't dependent with anything
                previousInstruction = null;

                continue;
            }

            // Pipeline Locking Will Pause While Loop until It is Notified by Thread that the instruction was executed

            // Check if Current Instruction is a BEQ or BNE
            // Then wait BEQ or BNE Output before Execution
            // Because we are either gonna proceed correctly
            // Or we are gonna make a Jump in our Sequence
            if (registerInstruction[0] == 4 || registerInstruction[0] == 5)
                pipelineLock.lock();

            // Check if Previous and Current Instructions
            // Are Independent, If not, Stall Pipeline
            if (previousInstruction != null && assembler.areDependent(registerInstruction, previousInstruction))
                pipelineLock.lock();

            previousInstruction = registerInstruction;

            // Check if Pipeline was Jumped through BEQ or BNE
            // If true, then Ignore current Instruction because
            // We are gonna be changing our sequence in the code
            if (jumpPipeline) {
                previousInstruction = null;

                jumpPipeline = false;
                continue;
            }

            // Thread that will Execute the Instruction
            new Thread(() -> {
                // Give a bit of delay cause of errors
                // TODO: Understand why the hell this bullshit occurs
                try {
                    Thread.sleep(50);
                } catch (Exception e) {  }

                // Get the Response from the Execution Instruction
                int address = alu.executeInstruction(registerInstructionTemp);
                // Make sure the response is not -1
                if (address != -1) {
                    // If it isn't then we have a sequence jump
                    // So JUMP the pipeline, JUMP JUMP JUMP
                    assembler.jumpTo(assembler.getProgramCounter() + address);
                    jumpPipeline = true;
                }

                // Notify the Pipeline that the Thread Executed
                try {
                    pipelineLock.notify();
                } catch (Exception e) { }

                // Print the Registers with the Instruction that Ran
                print(instructionTemp);
            }).start();

            // Only added this for printing purposes, bad printing!
            // TODO: Understand why the hell this bullshit occurs
            try {
                Thread.sleep(50);
            } catch (Exception e) {  }
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
        String[] examples = { "addition", "loadnstore", "loop" };
        String fileName = examples[2];

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

        Controller controller = new Controller(inputFile, outputFile);
        controller.startSimulation();
    }
    // -----------------------------------------------------------------------------------------------------------------
}
