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

    private Scanner scanner;

    // Assembler Constructor
    // -----------------------------------------------------------------------------------------------------------------
    public Assembler(File file) {
        try {
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
        if (!scanner.hasNextLine())
            return null;

        String line = scanner.nextLine();

        // If line is empty or if line starts with a comment
        // Ignore line, and get the next Instruction
        if (line.isEmpty() || line.startsWith("#"))
            return nextInstruction();

        // Check if there is no inline comments
        line = line.split("#")[0];

        // Split the Line into the parameters it needs
        String[] lineFormat = line.split(" ");

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
            Scanner stdin = new Scanner(new File("resources/instructions.csv"));

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
        String[] instruction = new String[6];

        int opCode = Integer.parseInt(instructionFormat[2]);

        switch (instructionFormat[1]) {
            case "R":
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
                instruction = new String[2];

                // Taking Decimal Operation Code from Format
                // And placing it into Instruction as Binary
                instruction[1] = toBitString(opCode, 6);

                // Changing Constant or Address in lineFormat[1] into Binary
                instruction[3] = toBitString(Integer.parseInt(lineFormat[1]), 26);

                break;
        }

        String instructionStr = "";
        for (String str : instruction)
            instructionStr += str;

        return instructionStr;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // To Binary String Converter
    // -----------------------------------------------------------------------------------------------------------------
    public static String toBitString(int number, int size) {
        boolean isTwosComplement = false;
        if (number < 0)
            isTwosComplement = true;

        String binary = Integer.toBinaryString(number);

        if (binary.length() < size) {
            while (binary.length() < size)
                binary = (isTwosComplement ? "1" : "0") + binary;
        } else if (binary.length() > size)
            binary = binary.substring(binary.length() - 32, binary.length());

        return binary;
    }
    // -----------------------------------------------------------------------------------------------------------------
}
