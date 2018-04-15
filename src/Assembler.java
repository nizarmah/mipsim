import java.io.File;
import java.util.Scanner;

public class Assembler {
    /* Assembler Explained
    *
    * So the assembler is nothing but a fancy Scanner
    * In order to simplify stuff, we make a Assembler
    * That functions like the Scanner
    * However, it does have some extra features
    *
    * It will be used to ignore any comments
    * Therefore, we will always retrieve instructions
    *
    * In addition, it converts those instructions to binary
    * And it can also inference where to jump to if sequence changes
    *
     */

    private File file;
    private Scanner scanner;
    private int lastReadLine;

    // Assembler Constructor
    // -----------------------------------------------------------------------------------------------------------------
    public Assembler(File file) {
        try {
            this.file = file;

            lastReadLine = 0;
            scanner = new Scanner(file);
        } catch (Exception e) {
            Controller.exit("Assembler Error : Constructor : " + e.getMessage());
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Assembler Next Instruction
    // -----------------------------------------------------------------------------------------------------------------
    public String[] nextInstruction() {
        // If there are no lines left, return null
        // This is some error I have been having so I fixed it with this
        // I personally do not know how this fixes it, it just does.
        // TODO: Understand why the hell this bullshit occurs
        try {
            if (!scanner.hasNextLine())
                return null;
        } catch (Exception e) {
            return null;
        }

        // Keep track of the last read line
        lastReadLine += 1;
        String line = scanner.nextLine();

        // If line is empty or if line starts with a comment
        // Ignore line, and get the next Instruction
        if (line.isEmpty() || line.startsWith("#"))
            return nextInstruction();

        // Check if there is no inline comments
        line = line.split("#")[0];

        // Split the Line into the parameters it needs
        String[] lineFormat = line.replaceAll(",", " ").split("\\s+");

        // Find the Instruction from Instructions.CSV
        // That way, we have the format and the operation code
        String[] instructionFormat = findInstruction(lineFormat);

        // Return the 32 bit Format of the Instruction
        // And the unencoded line for logging
        return new String[] {
            assembleInstruction(lineFormat, instructionFormat),
            line
        };
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Find Instruction from Instructions.CSV
    // -----------------------------------------------------------------------------------------------------------------
    private String[] findInstruction(String[] line) {
        try {
            // Open the instructions.csv file to search for the instruction
            Scanner stdin = new Scanner(new File("resources/instructions.csv"));

            // Keep searching until you find the instruction
            while (stdin.hasNextLine()) {
                String[] instruction = stdin.nextLine().split(",");

                if (instruction[0].equals(line[0])) {
                    stdin.close();
                    return instruction;
                }
            }

            stdin.close();
        } catch (Exception e) {
            Controller.exit("Assembler Error : findInstruction : " + e.getMessage());
        }

        Controller.exit("Assembler Error : findInstruction : Invalid Instruction Found (" + line[0] + ")");
        return null;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Assemble Instruction into 32 Bit Format
    // -----------------------------------------------------------------------------------------------------------------
    private String assembleInstruction (String[] lineFormat, String[] instructionFormat) {
        // This function reads the Instruction's Format from Instructions.csv
        // And Applies the instruction onto the Code Line entered by the user
        // That way the code has a format that the simulator can understand

        // Assembled Instruction in the Making
        String[] instruction = new String[6];

        // Get the OP Code of the instruction
        int opCode = Integer.parseInt(instructionFormat[2]);

        // Switch the instruction's Format in order
        // To know how to assemble the instruction
        switch (instructionFormat[1]) {
            case "R":
                // Instruction is of format [opcode, rs, rt, rd, shift, func]
                instruction = new String[6];

                // Taking Decimal Operation Code from Format
                // And placing it into Instruction as Binary
                instruction[0] = toBitString(opCode, 6);

                // Getting Each Register's Array Index
                instruction[1] = toBitString(Register.getRegister(lineFormat[2]), 5); // Source Register
                instruction[2] = toBitString(Register.getRegister(lineFormat[3]), 5); // Source Register

                instruction[3] = toBitString(Register.getRegister(lineFormat[1]), 5); // Destination Register

                // Adding Shift Amount to Instruction
                // Ignoring Shifting Because we have no SLL or SRL
                instruction[4] = toBitString(0, 5); // For now 00000

                // Taking Decimal Function Code from Format
                // And placing it into Instruction as Binary
                instruction[5] = toBitString(Integer.parseInt(instructionFormat[3]), 6);

                break;
            case "I":
                // Instruction is of format [opcode, rs, rd, address]
                instruction = new String[4];

                // Taking Decimal Operation Code from Format
                // And placing it into Instruction as Binary
                instruction[0] = toBitString(opCode, 6);

                // Getting Each Register's Array Index
                instruction[1] = toBitString(Register.getRegister(lineFormat[2]), 5); // Source Register
                instruction[2] = toBitString(Register.getRegister(lineFormat[1]), 5); // Destination Register

                // If Operation Code is 35 or 43 -> Thus LW or SW
                if (opCode == 35 || opCode == 43) {
                    // Check if Register is Offsetted
                    // And add the offset, or set it to 0
                    // For example : 4($s0)
                    if (lineFormat[2].indexOf('(') > -1) {
                        lineFormat[2] = lineFormat[2].replace('(', ' ');

                        String[] offsetFormat = lineFormat[2].split(" ");

                        offsetFormat[1] = offsetFormat[1].substring(0, offsetFormat[1].length() - 1);

                        instruction[1] = toBitString(Register.getRegister(offsetFormat[1]), 5); // Source Register
                        instruction[3] = toBitString(Integer.parseInt(offsetFormat[0]), 16);
                    } else
                        instruction[3] = toBitString(0, 16);
                } else
                    instruction[3] = toBitString(Integer.parseInt(lineFormat[3]), 16);
                break;
            case "J":
                // Instruction is of Format [opcode, address]
                instruction = new String[2];

                // Taking Decimal Operation Code from Format
                // And placing it into Instruction as Binary
                instruction[0] = toBitString(opCode, 6);

                // Changing Constant or Address in lineFormat[1] into Binary
                instruction[1] = toBitString(Integer.parseInt(lineFormat[1]), 26);

                break;
        }

        String instructionStr = "";
        for (String str : instruction)
            instructionStr += str;

        return instructionStr;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Operate Instruction from Instruction Register
    // -----------------------------------------------------------------------------------------------------------------
    public int[] decodeInstruction (String instructionRegister) {
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

            return new int[] {
                opCode, rs, rt, rd, 0, func
            };
        } else if (opCode == 2) { // Check if it is J format
            int address = Integer.parseInt(instructionRegister.substring(6, 32), 2);

            return new int[] {
                opCode, address
            };
        } else { // Then this is definitely I format
            int rs = Integer.parseInt(instructionRegister.substring(6, 11), 2);
            int rd = Integer.parseInt(instructionRegister.substring(11, 16), 2);

            int address = Integer.parseInt(instructionRegister.substring(16, 32), 2);

            return new int[] {
                opCode, rs, rd, address
            };
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Check if two Instructions are Dependent
    // -----------------------------------------------------------------------------------------------------------------
    public boolean areDependent(int[] registerInstruction, int[] previousInstruction) {
        // Check if One of the instructions is a Jump
        if (registerInstruction[0] == 2 || previousInstruction[0] == 2)
            return false;

        // Compare All Registers and make sure
        // To return true if there's any dependencies
        // And check if instruction is R format or I format
        // Note that R Format has 3 Registers, rs, rt, rd in format [opcode, rs, rt, rd, shift, func]
        // Note that I Format has 2 Regisers, rs, rd in format [opcode, rs, rd, address]
        for (int i = 1; i < (registerInstruction[0] == 0 ? 4 : 3); i++) {
            if (registerInstruction[i] == 0)
                continue;

            for (int j = 1; j < (previousInstruction[0] == 0 ? 4 : 3); j++)
                if (registerInstruction[i] == previousInstruction[j])
                    return true;
        }

        return false;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Assembler Jump To
    // -----------------------------------------------------------------------------------------------------------------
    public void jumpTo(int programCounter) {
        try {
            // Check if the scanner is ahead of the programCounter
            // Or if the scanner is out of lines to read
            // Then reset it so we can read the instruction at programCounter
            if (programCounter <= lastReadLine) {
                scanner = new Scanner(this.file);
                lastReadLine = 0;
            }

            // If the lastReadLine is the directly before the programCounter
            // If it is not, then continue reading until we get to that line
            while (scanner.hasNextLine() && lastReadLine < (programCounter - 1))
                this.nextInstruction();

            // Then next line on nextInstruction()
            // Is gonna be the line at Program Counter
        } catch (Exception e) {
            Controller.exit("Assembler Error : jumpTo : " + e.getMessage());
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Assembler Get Program Counter
    // -----------------------------------------------------------------------------------------------------------------
    public int getProgramCounter() {
        return lastReadLine;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // To Binary String Converter
    // -----------------------------------------------------------------------------------------------------------------
    public static String toBitString(int number, int size) {
        // Assumes it is not Two's Complement
        boolean isTwosComplement = false;
        // If it Is, then Set Bool to True
        if (number < 0)
            isTwosComplement = true;

        // Change Binary using Java's Binary Converter
        String binary = Integer.toBinaryString(number);

        // Make sure the Binary changed by Java is of the size needed
        if (binary.length() < size) {
            // Extend Binary's Length by 1 or 0 depending if its Two's complement or not
            while (binary.length() < size)
                binary = (isTwosComplement ? "1" : "0") + binary;
        } else if (binary.length() > size)
            binary = binary.substring(binary.length() - 32, binary.length());

        return binary;
    }
    // -----------------------------------------------------------------------------------------------------------------
}
