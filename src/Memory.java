import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Memory {
    /* Memory Explained
    *
    * Instead of saving the memory in an array,
    * we will save it in a file, and access it
    * directly from that file it is saved in.
    *
    * Why is that ? Well, to avoid using RAM
    *
    * For that, we need a file, a reader, and a writer
    *
    * The file reader will save the last read line
    * that way, if we need to continue reading, we don't
    * have to read again from the start, but we'll continue
    * from where we last left
    *
     */
    private static Memory memory;

    private File file;

    private Scanner reader;

    private int lastReadLine;
    private int instructionCount;

    // Memory Constructor
    // -----------------------------------------------------------------------------------------------------------------
    public Memory(String filePath) {
        file = new File(filePath);

        lastReadLine = 0;
        instructionCount = 0;

        // Make sure File exists, if it doesn't create it
        // If it cannot be created, then abort running program
        try {
            if (!file.exists())
                if (!file.createNewFile())
                    throw new Exception("File was not created due to insufficient permissions");

            reader = new Scanner(file);
        } catch (Exception e) {
            Controller.exit("Memory Error : Constructor : " + e.getMessage());
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Default Memory Instance
    // -----------------------------------------------------------------------------------------------------------------
    public static synchronized Memory getInstance() {
        if (memory == null)
            memory = new Memory("resources/memory");
        return memory;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Memory Instruction Loader
    // -----------------------------------------------------------------------------------------------------------------
    public String loadInstruction(String instruction) {
        // Loads instruction into Memory and Increases Instruction Count

        instructionCount += 1;
        this.write((instructionCount - 1) * 4, instruction);

        return instruction;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Get Instruction Count
    // -----------------------------------------------------------------------------------------------------------------
    public int getInstructionCount () {
        return instructionCount;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Get Instruction At Program Counter
    // -----------------------------------------------------------------------------------------------------------------
    public String getInstruction (int programCounter) {
        try {
            // Check if the scanner is ahead of the programCounter
            // Or if the scanner is out of lines to read
            // Then reset it so we can read the instruction at programCounter
            if (!reader.hasNextLine() || programCounter <= lastReadLine) {
                reader = new Scanner(file);
                lastReadLine = 0;
            }

            // If the lastReadLine is the directly before the programCounter
            // If it is not, then continue reading until we get to that line
            while (reader.hasNextLine() && lastReadLine < (programCounter - 1)) {
                reader.nextLine();
                lastReadLine += 1;
            }

            String instruction = "";
            for (int i = 0; i < 4; i++) {
                instruction += reader.nextLine();
                lastReadLine += 1;
            }

            return instruction;
        } catch (Exception e) {
            Controller.exit("Memory Error : getInstruction : " + e.getMessage());
        }

        return null;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Memory Read Data From Address
    // -----------------------------------------------------------------------------------------------------------------
    public String read(int address) {
        try {
            // Check if the scanner is ahead of the programCounter
            // Or if the scanner is out of lines to read
            // Then reset it so we can read the instruction at programCounter
            if (!reader.hasNextLine() || address <= lastReadLine) {
                reader = new Scanner(file);
                lastReadLine = 0;
            }

            // If the lastReadLine is the directly before the programCounter
            // If it is not, then continue reading until we get to that line
            while (reader.hasNextLine() && lastReadLine < (address - 1)) {
                reader.nextLine();
                lastReadLine += 1;
            }

            lastReadLine += 1;
            return reader.nextLine();
        } catch (Exception e) {
            Controller.exit("Memory Error : read : " + e.getMessage());
        }

        return null;
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Memory Write Data to Address
    // -----------------------------------------------------------------------------------------------------------------
    public void write(int address, String line) {
        // A Fancy File Writer to a certain Line that I found on stackoverflow
        // This reads the file as an array of lines and replaces line at specified
        // Address with a new specified line

        // I added the Try - Catch part, where if the address doesn't exist
        // We keep adding new lines to the array, until we get to the desired index
        // Where we replace that address with a new specified line

        try {
            Path path = Paths.get("resources/memory");
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            try {
                // Note That writing Words to Memory Breaks it into 4 Bytes
                for (int i = 0; i < 4; i++)
                    lines.set((address + i) - 1, line.substring((8 * i), (8 * (i + 1))));
            } catch (Exception e) {
                while (lines.size() < address)
                    lines.add(Assembler.toBitString(0, 8));

                // Note That writing Words to Memory Breaks it into 4 Bytes
                for (int i = 0; i < 4; i++)
                    lines.add(line.substring((8 * i), (8 * (i + 1))));
            } finally {
                Files.write(path, lines, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            Controller.exit("Memory Error : write : " + e.getMessage());
        }
    }
    // -----------------------------------------------------------------------------------------------------------------

    // Memory to String Dump
    // -----------------------------------------------------------------------------------------------------------------
    public String toString () {
        String str = "";
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine())
                str += scanner.nextLine() + "\n";

            scanner.close();
        } catch (Exception e) {
            Controller.exit("Memory Error : toString : " + e.getMessage());
        }

        return str;
    }
    // -----------------------------------------------------------------------------------------------------------------
}
