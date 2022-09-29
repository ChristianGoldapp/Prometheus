# The Prometheus Programming Language #

## 1. Introduction ##

I was playing the video game [TIS-100](http://www.zachtronics.com/tis-100/) by Zachtronics, when I realised how fun playing with such a programming language was; safeguarded from all the intricacies of "real" ASM like, say, x86. Unfortunately (for me at least), the programming language used in TIS-100 is extremely limited, and based on multiple coprocessors. I set to designing a more capable language, and that is what I did.

Prometheus is an ISA with its assembly language that is assembled and interpreted by Java. It's purely for entertainment (and maybe educational) purposes. It is based on opcodes that are executed sequentially, one after another, by the processor.

## 2. Execution Enviroment ##

Prometheus programs are executed on the "Prometheus Processor". The Prometheus processor consists of a number of registers, a stack, an execution pointer and a memory.

Once an execution is over, the state of the processor is printed out. (Note that I have not added I/O yet.)

### 2.1 Data ###

The Prometheus Processor operates on binary words 32 bits in width. These words can be interpreted as unsigned integers, signed two's complement integers and IEEE-754 floating point numbers.

### 2.2 Registers ###

A Register is a container for a single word. The theoretical limit for the number of registers is 255, from 0x00 to 0xFE. By default, a Prometheus Processor has 10 registers from 0x00 to 0x0A.

### 2.3 Stack ###

The Prometheus Processor contains a pushdown stack independant of the other registers and memory. It holds single words that can be pushed and popped one by one.

### 2.4 Memory ###

The Prometheus Processor has a memory that is an array of words. It can be written to and read off of one word at a time. By default, the memory has the size 512 words. It contains both the program and any data the programmer stores in it. The program always starts out at the very beginning of the memory.

### 2.5 Execution Pointer ###

The execution pointer always points to the first word of the next instruction to be executed. It cannot be directly modified, only through the Jump-to-Label, Jump-to-Address and Jump-to-Offset instructions.

## Instructions ##

###Instruction Format 

An instruction is an OpCode, followed by up to three arguments. An argument can be:

* A hexadecimal word literal, starting with "0x"
* A register, with the format "RX", where X is a decimal number between 0 and 255 inclusively.
* A label (Alphanumeric characters only)

Instructions can be prepended with a label, that is signalled with a leading "_". The "_" is not part of the label itself and thus not added when the label is referred to in the arguments.

### Op Codes ###

| Hex | OpCode | arg1 | arg2 | arg3 | Description |
|------|--------|------|------|------|--------------------------------------------------------------------------|
| 0x00 | HALT | | |  | Halt the processor |
| 0x01 | WAIT | | |  | Wait for input | Not implemented yet|
| 0x0F | NOOP | | |  | Do Nothing |
| 0x10 | PUT | LIT | REG |  | Put the integer literal arg1 into the register arg2 |
| 0x10 | U_PUT | LIT | REG |  | Put the unsigned integer literal arg1 into the register arg2 |
| 0x10 | F_PUT | LIT | REG |  | Put the float literal arg1 into the register arg2 |
| 0x10 | MOV | VAL | REG |  | Move the value arg1 into the register arg2 |
| 0x11 | SWP | REG | REG |  | Swap the values of the registers arg1 and arg2 |
| 0x12 | LOAD | VAL | REG |  | Load the value at memory location arg1 into register arg2 |
| 0x13 | SAVE | VAL | REG |  | Save the value in register arg2 into memory location arg1 |
| 0x20 | ADD | VAL | VAL | REG | Add the integer value arg1 to the integer value arg2 and write result into register arg3 |
| 0x21 | SUB | VAL | VAL | REG | Subtract the integer value arg2 from the integer value arg1 and write result into register arg3 |
| 0x22 | MUL | VAL | VAL | REG | Multiply the integer value arg1 with the v arg2 and write result into register arg3 |
| 0x23 | DIV | VAL | VAL | REG | Divide the integer value arg1 by the integer value arg2 and write result into register arg3 |
| 0x30 | U_ADD | VAL | VAL | REG | Add the unsigned value arg1 to the unsigned value  arg2 and write result into register arg3 |
| 0x31 | U_SUB | VAL | VAL | REG | Subtract the unsigned value arg2 from the v arg1 and write result into register arg3 |
| 0x32 | U_MUL | VAL | VAL | REG | Multiply the value arg1 with the unsigned value  arg2 and write result into register arg3 |
| 0x33 | U_DIV | VAL | VAL | REG | Divide the unsigned value  arg1 by the unsigned value  arg2 and write result into register arg3 |
| 0x40 | F_ADD | VAL | VAL | REG | Add the float value arg1 to the float value arg2 and write result into register arg3 |
| 0x41 | F_SUB | VAL | VAL | REG | Subtract the float value arg2 from the float value arg1 and write result into register arg3 |
| 0x42 | F_MUL | VAL | VAL | REG | Multiply the float value arg1 with the float value arg2 and write result into register arg3 |
| 0x43 | F_DIV | VAL | VAL | REG | Divide the float value arg1 by the float value arg2 and write result into register arg3 |
| 0x50 | NOT | VAL | REG | | NOT the bits of value arg1 and write result into register arg2 |
| 0x51 | AND | VAL | VAL | REG | AND the bits of value arg1 with the value arg2 and write result into register arg3 |
| 0x52 | OR | VAL | VAL | REG | OR the bits of value arg1 with the value arg2 and write result into register arg3 |
| 0x53 | XOR | VAL | VAL | REG | XOR the bits of value arg1 with the value arg2 and write result into register arg3 |
| 0x5E | LSHIFT | VAL | REG |  | Left-shift the bits of value arg1 and write result into register arg3 (undefined bits are set to 0)|
| 0x5F | RHIFT | VAL | REG |  | Right-shift the bits of value arg1 and write result into register arg3 (undefined bits are set to 0)|
| 0x60 | FTOI | VAL | REG |  | Convert the float value arg1 into an integer and write result into register arg2 |
| 0x61 | ITOF | VAL | REG |  | Convert the integer value arg1 into a float and write result into register arg2 |
| 0x62 | UTOI | VAL | REG |  | Convert the unsigned value arg1 into an integer and write result into register arg2 |
| 0x63 | ITOU | VAL | REG |  | Convert the integer value arg1 into an unsigned integer and write result into register arg2 |
| 0x70 | PEEK | REG | |  | Copy the top value of the stack into the register arg1 |
| 0x71 | PUSH | VAL | |  | Push the value arg1 onto the stack|
| 0x72 | POP | REG | |  | Copy the top value of the stack into the register arg1 and then remove it from the stack|
| 0xE0 | JOF | VAL | |  | Unconditional jump to the relative address arg1|
| 0xE1 | JOIZ | VAL | REG |  | Jump to relative address arg1 if register arg2 is zero|
| 0xE2 | JONZ | VAL | REG |  | Jump to relative address arg1 if register arg2 is not zero|
| 0xE3 | JOLZ | VAL | REG |  | Jump to relative address arg1 if register arg2 larger than zero|
| 0xE4 | JOSZ | VAL | REG |  | Jump to relative address arg1 if register arg2 smaller than zero|
| 0xF0 | JMP | LBL | |  | Unconditional jump to the label arg1|
| 0xF1 | JIZ | LBL | REG |  | Jump to label arg1 if register arg2 is zero|
| 0xF2 | JNZ | LBL | REG |  | Jump to label arg1 if register arg2 is not zero|
| 0xF3 | JLZ | LBL | REG |  | Jump to label arg1 if register arg2 larger than zero|
| 0xF4 | JSZ | LBL | REG |  | Jump to label arg1 if register arg2 smaller than zero|
| 0xF0 | JAD | LBL | |  | Unconditional jump to the address arg1|
| 0xF1 | JAIZ | LBL | REG |  | Jump to address arg1 if register arg1 is zero|
| 0xF2 | JANZ | LBL | REG |  | Jump to address arg1 if register arg1 is not zero|
| 0xF3 | JALZ | LBL | REG |  | Jump to address arg1 if register arg1 larger than zero|
| 0xF4 | JASZ | LBL | REG |  | Jump to address arg1 if register arg1 smaller than zero|
| 0xFE | SYSCALL | VAL | VAL | REG | Make syscall arg1 with argument arg2| Not implemented yet|

The PUT, U_PUT, F_PUT, JMP, JIZ, JNZ, JLZ and JSZ are turned into MOV and Jump-to-Address instructions during assembly and thus never appear directly in the bytecode.

### Example: Fibonacci numbers ###
    
    PUT 32 R9
    MOV R9 R0
    PUSH 0x1
    PUSH 0x1
    _LOOP POP R1
    POP R2
    ADD R1 R2 R3
    PUSH R2
    PUSH R1
    PUSH R3
    SUB R0 0x1 R0
    JNZ R0 LOOP

This program pushes each Fibonacci number onto the stack one by one.

## 3. Bytecode format ##

Every instruction consists of 1 to 4 words. The first word is the so-called "Op-Word", the next 0 to 3 words are the "Argument-Word"s. The Op-Word is again divided into four bytes: The OpCode, and three argument bytes. An argument byte between 0x00 and 0xFE is interpreted as the corresponding register, while an argument byte of 0xFF is interpreted as a placeholder for a word. These words are then appended as argument words.

### Example: Above, assembled ###

    0x10FF0900 0x00000020 0x10090000 0x71FF0000 
    0x00000001 0x71FF0000 0x00000001 0x72010000 
    0x72020000 0x20010203 0x71020000 0x71010000 
    0x71030000 0x2100FF00 0x00000001 0xE200FF00 
    0xFFFFFFF8

## 4. Syscalls ##

Every syscall is called with an address, an argument and a register for the result.

|Call address||

## 5. Output ##

Since I have not yet added I/O functions (which will arrive via SYSCALL and WAIT), I make do by printing the state of the processor out to stdin after execution is complete. The assembler also prints its result out. (Note that CONV denotes an opcode for which the original mnemonic has been preserved, even though the assembler changed the opcode into another one in the binary.)

       PUT 32 R9  :   0x10FF0900 0x00000020
       MOV R9 R0  :   0x10090000   
    PUSH 0x1  :   0x71FF0000 0x00000001
    PUSH 0x1  :   0x71FF0000 0x00000001
    _LOOP POP R1  :   0x72010000   
      POP R2  :   0x72020000   
    ADD R1 R2 R3  :   0x20010203   
     PUSH R2  :   0x71020000   
     PUSH R1  :   0x71010000   
     PUSH R3  :   0x71030000   
       SUB R0 0x1 R0  :   0x2100FF00 0x00000001
    JNZ R0 LOOP CONV  :   0xE200FF00 0xFFFFFFF8
    Registers:
     R0:0x00000000 (0)
     R1:0x0035C7E2 (3524578)
     R2:0x00213D05 (2178309)
     R3:0x005704E7 (5702887)
     R4:0x00000000 (0)
     R5:0x00000000 (0)
     R6:0x00000000 (0)
     R7:0x00000000 (0)
     R8:0x00000000 (0)
     R9:0x00000020 (32)
    Stack:
     0x0021:   0x005704E7 (5702887)
     0x0020:   0x0035C7E2 (3524578)
     0x001F:   0x00213D05 (2178309)
     0x001E:   0x00148ADD (1346269)
     0x001D:   0x000CB228 (832040)
     0x001C:   0x0007D8B5 (514229)
     0x001B:   0x0004D973 (317811)
     0x001A:   0x0002FF42 (196418)
     0x0019:   0x0001DA31 (121393)
     0x0018:   0x00012511 (75025)
     0x0017:   0x0000B520 (46368)
     0x0016:   0x00006FF1 (28657)
     0x0015:   0x0000452F (17711)
     0x0014:   0x00002AC2 (10946)
     0x0013:   0x00001A6D (6765)
     0x0012:   0x00001055 (4181)
     0x0011:   0x00000A18 (2584)
     0x0010:   0x0000063D (1597)
     0x000F:   0x000003DB (987)
     0x000E:   0x00000262 (610)
     0x000D:   0x00000179 (377)
     0x000C:   0x000000E9 (233)
     0x000B:   0x00000090 (144)
     0x000A:   0x00000059 (89)
     0x0009:   0x00000037 (55)
     0x0008:   0x00000022 (34)
     0x0007:   0x00000015 (21)
     0x0006:   0x0000000D (13)
     0x0005:   0x00000008 (8)
     0x0004:   0x00000005 (5)
     0x0003:   0x00000003 (3)
     0x0002:   0x00000002 (2)
     0x0001:   0x00000001 (1)
     0x0000:   0x00000001 (1)
    Memory:
     0x10FF0900 0x00000020 0x10090000 0x71FF0000 0x00000001 0x71FF0000 0x00000001 0x72010000 0x72020000 0x20010203 0x71020000 0x71010000 0x71030000 0x2100FF00 0x00000001 0xE200FF00
     0xFFFFFFF8 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
     0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000 0x00000000
