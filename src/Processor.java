import assembler.Assembler;
import assembler.AssemblyException;
import common.Constants;
import common.OpCode;
import common.Word32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @author Chris Gold
 * @version 1.0
 */
public class Processor implements Constants {
    public static final int DEFAULT_MEMSIZE = 2 << 8;
    public static final int DEFAULT_REGSIZE = 10;
    private final Word32[] memory;
    private final Word32[] registers;
    private final Stack<Word32> stack;
    private int executionPointer;

    public Processor(int memSize, int regSize) {
        this.executionPointer = 0;
        this.memory = new Word32[memSize];
        Arrays.fill(memory, new Word32(0));
        this.registers = new Word32[regSize];
        Arrays.fill(registers, new Word32(0));
        this.stack = new Stack<>();
    }

    public Processor() {
        this(DEFAULT_MEMSIZE, DEFAULT_REGSIZE);
    }

    public void setProgram(Word32[] program) {
        Arrays.fill(memory, new Word32(0));
        System.arraycopy(program, 0, memory, 0, program.length);
    }

    public void run() {
        while (executionPointer != -1) {
            executionPointer = executeCurrentInstruction();
        }
    }

    public int executeCurrentInstruction() {
        Word32 firstWord = memory[executionPointer];
        byte[] firstWordBytes = firstWord.bytes();
        OpCode op = OpCode.get(firstWordBytes[0]);
        if (op == OpCode.HALT) {
            return -1;
        }
        byte arg1 = firstWordBytes[1];
        byte arg2 = firstWordBytes[2];
        byte arg3 = firstWordBytes[3];
        List<Word32> argWords = new ArrayList<>(3);
        int argCount = 0;
        if (arg1 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
            argCount++;
        }
        if (arg2 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
            argCount++;
        }
        if (arg3 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
            argCount++;
        }
        Word32 val1;
        if (arg1 == ALL_HIGH) {
            val1 = argWords.get(0);
            argWords.remove(0);
        } else {
            val1 = registers[arg1];
        }
        Word32 val2;
        if (arg2 == ALL_HIGH) {
            val2 = argWords.get(0);
            argWords.remove(0);
        } else {
            val2 = registers[arg2];
        }
        Word32 val3;
        if (arg3 == ALL_HIGH) {
            val3 = argWords.get(0);
            argWords.remove(0);
        } else {
            val3 = registers[arg3];
        }
        int nEP = executionPointer + 1 + argCount;
        switch (op) {
            case MOV:
                registers[arg2] = val1;
                break;
            case LOAD:
                registers[arg2] = memory[val1.intValue()];
                break;
            case SAVE:
                memory[val1.intValue()] = registers[arg2];
                break;
            case NOT:
                registers[arg2] = val1.not();
                break;
            case LSHIFT:
                registers[arg2] = val1.lshift();
                break;
            case RSHIFT:
                registers[arg2] = val1.rshift();
                break;
            case FTOI:
                registers[arg2] = val1.ftoi();
                break;
            case ITOF:
                registers[arg2] = val1.itof();
                break;
            case UTOI:
                registers[arg2] = val1.utoi();
                break;
            case ITOU:
                registers[arg2] = val1.itou();
                break;
            case SWP:
                registers[arg1] = val2;
                registers[arg2] = val1;
                break;
            case PUSH:
                stack.push(val1);
                break;
            case JAD:
                nEP = val1.intValue();
                break;
            case JOF:
                nEP = executionPointer + val1.intValue();
                break;
            case POP:
                registers[arg1] = stack.pop();
                break;
            case PEEK:
                registers[arg1] = stack.peek();
                break;
            case ADD:
                registers[arg3] = val1.add(val2);
                break;
            case SUB:
                registers[arg3] = val1.sub(val2);
                break;
            case MUL:
                registers[arg3] = val1.mul(val2);
                break;
            case DIV:
                registers[arg3] = val1.div(val2);
                break;
            case U_ADD:
                registers[arg3] = val1.uadd(val2);
                break;
            case U_SUB:
                registers[arg3] = val1.usub(val2);
                break;
            case U_MUL:
                registers[arg3] = val1.umul(val2);
                break;
            case U_DIV:
                registers[arg3] = val1.udiv(val2);
                break;
            case F_ADD:
                registers[arg3] = val1.fadd(val2);
                break;
            case F_SUB:
                registers[arg3] = val1.fsub(val2);
                break;
            case F_MUL:
                registers[arg3] = val1.fmul(val2);
                break;
            case F_DIV:
                registers[arg3] = val1.fdiv(val2);
                break;
            case AND:
                registers[arg3] = val1.and(val2);
                break;
            case OR:
                registers[arg3] = val1.or(val2);
                break;
            case XOR:
                registers[arg3] = val1.xor(val2);
                break;
            case JAIZ:
                if (val1.value() == 0) {
                    nEP = val2.intValue();
                }
                break;
            case JANZ:
                if (val1.value() != 0) {
                    nEP = val2.intValue();
                }
                break;
            case JALZ:
                if (val1.value() > 0) {
                    nEP = val2.intValue();
                }
                break;
            case JASZ:
                if (val1.value() < 0) {
                    nEP = val2.intValue();
                }
                break;
            case JOIZ:
                if (val1.value() == 0) {
                    nEP = executionPointer + val2.intValue();
                }
                break;
            case JONZ:
                if (val1.value() != 0) {
                    nEP = executionPointer + val2.intValue();
                }
                break;
            case JOLZ:
                if (val1.value() > 0) {
                    nEP = executionPointer + val2.intValue();
                }
                break;
            case JOSZ:
                if (val1.value() < 0) {
                    nEP = executionPointer + val2.intValue();
                }
                break;
            case SYSCALL:
                syscall(val1, val2);
                break;
            case JAEQ:
                if (val1.value() == val2.value()) {
                    nEP = val3.intValue();
                }
                break;
            case NOOP:
                break;
            case WAIT:
                //TODO
                break;
            case HALT:
                nEP = -1;
                break;
            default:
                throw new RuntimeException();
        }
        return nEP;
    }

    private void syscall(Word32 call, Word32 argument) {
        //TODO
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Registers:\n");
        for (int i = 0; i < registers.length; i++) {
            sb.append(" R");
            sb.append(i);
            sb.append(":        ");
            sb.append(registers[i]);
            sb.append("\n");
        }
        sb.append("Stack:\n");
        for (int i = stack.size() - 1; i >= 0; i--) {
            sb.append(String.format(" 0x%04X", i));
            sb.append(":   ");
            sb.append(stack.get(i));
            sb.append("\n");
        }
        sb.append("Memory:\n");
        sb.append(Word32.arrayToString(memory, 16));
        return sb.toString();
    }
}

class Main {
    public static void main(String[] args) throws AssemblyException {
        Word32[] program = Assembler.assemble("#Place a in R1 and x in R9.\n" +
                "F_PUT 10.0 R1\n" +
                "F_PUT 100.0 R9\n" +
                "PUT 1000 R0\n" +
                "_LOOP F_DIV R9 R1 R2\n" +
                "F_ADD R1 R2 R1\n" +
                "F_MUL R1 0x3f000000 R1\n" +
                "SUB R0 0x1 R0\n" +
                "JNZ R0 LOOP");
        Processor p = new Processor();
        p.setProgram(program);
        p.run();
        System.out.println(p);
    }
}
