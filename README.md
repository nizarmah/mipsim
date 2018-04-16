# mipsim
A Basic MIPS Simulator

> Supported Instructions are in `instructions.csv`
> Labeling is not supported, use Addresses for Jumps

### Explaining MIPS Pipeline

First of all, in order to understand how to simulate MIPS, we need to understand how it works.

The MIPS Pipeline is based on 5 stages.
They are:
  1. Fetch Instruction
  2. Decode Instruction
  3. Executing Instruction
  4. Memory Access
  5. Writeback to Registers
  
Now when the first instruction is fetched and being decoded, the second is being fetched, and so on...
However, sometimes instructions depend on each other; two instructions directly after each other might need to access the same registers.
Therefore, we would need to stall the pipeline until a certain stage in order to make sure that there will be no conflict.

### Explaining MIPS Pipeline Process

So the process of the MIPS Pipeline goes as follows.

The Controller starts reading the code in memory word by word using an indexer we'll call programCounter.
This programCounter specifies where the Controller is at currently.

In addition, an instruction register stores the current word by the Controller.

The assembler turns the code into words ( 4 bytes ) according to the format of each instruction. Consequently, it loads it into the instruction register.
Then, the Controller will execute the instruction register through the ALU and access memory and write to registers.

Yet, we need not to forget that during this process, the pipeline will be fetching another instruction and doing the same with it.
Therefore, in order to make sure there are no conflicts between those two instructions, the current one and the one preceeding it, we decode the current one and compare it with the decoded preceeding one.

Comparing those two instructions would be by comparing the addresses of the registers being used and of the memory addresses being used in each instruction. Therefore, if there are any matches, we stall the execution of the current instruction, or we do not.
In addition, we need to stall after every BEQ or BNE statement, to know if we are gonna be changing sequence or not.
Moreover, we need to account for any Jumps in the sequence and not bother to decode the instruction after.

### Apply MIPS Pipeline Process

First of all, we need to create all the required parts of the MIPS Simulator.
This includes a Controller, Memory, Register, Assembler, and an ALU.

The Controller will be taking care of Simulating Everything and telling operating the parts.
The Memory will be used to access the Memory. The Register will be used to access the Registers.
The Assembler will be dealing with fetching the code and decoding it.
The ALU will be responsible for the Executing, Memory, and Writeback stages.

> The ALU combines the three stages into one part for simplifying the application of the simulator.

> If you wish for further explanation on the role of each in the program, you can proceed to reading the comments in the top of each class.

Because we wish to simulate a MIPS Pipeline, we will be discussing only the Controller and how it works in Java.
