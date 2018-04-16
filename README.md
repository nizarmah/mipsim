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

When we have a Jump instruction, the MIPS Pipeline proceeds only to fetch the next instruction, and then it prevents decoding the fetched instruction, but fetches a new instruction.

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

Starting the simulation, we have some variables that we will be explaining when we need to use them.
So, let's start by discussing the `while` loop. The controller will keep simulating until there are no instructions left to execute, or when the Assembler will return `null`.

This instruction we read is an array of 2 strings. The first string is the 4 byte word of the instruction; whereas, the second string is the string converted to the 4 byte word ( used for printing purposes ).
After fetching, we decode the instruction so we know what the operation is and what the registers are, etc...

Before checking if the instruction is dependent on the one before it, we discussed the special case of the Jump instruction before; thus, we will be make sure that the instruction is not a Jump instruction, because, if it is, we will be jumping the Assembler to the new address and new instruction.

If it is not a Jump instruction, it can also include a jump, such as a BEQ or BNE. So, we need to make sure it is not one of those two.
However, if it is we need to make sure that they are not gonna change our sequence. So we wait for the operation to execute. Therefore, we will end up stalling the Pipeline by pausing / locking the `while` loop. Once we get the response from the ALU, we'll know if there is a jump by the positive value returned, or not otherwise. This response from the execution thread will notify the lock to continue the running the while loop.
Then, we'll proceed to jump or not. If we jump, the `jumpPipeline` boolean will be responsible for not executing the instruction we are currently on.

After checking for special instructions, we need to make sure that the instruction we are at and the instruction before it are not by any means dependent on each other. If they are, then we also stall / lock the Pipeline is order to await the response from the execution thread, which will unlock the pipeline.

Now, why an execution thread you might ask. The MIPS Pipeline proceeds to fetch the new instruction as soon as the one before it starts decoding. Therefore, the fetching of the instructions is synchronous, meaning runs in a sequence, one after the other. This leads us to say that the execution does not affect this sequence. Therefore, the execution must be running asynchronously from the latter sequence.

Then, we proceed to execute the instruction, and check if its response is negative.
If the response is positive, then there is a jump involved. So we do so...
And then at last, try to notify the locks that the Pipeline must continue and no longer be stalled.
