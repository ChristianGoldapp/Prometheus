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
        List<Word32> argWords = new ArrayList<>(3);
        int argCount = 0;
        byte[] args = new byte[3];
        System.arraycopy(firstWordBytes, 1, args, 0, firstWordBytes.length - 1);
        for (byte arg : args) {
            if (arg == ALL_HIGH) {
                argWords.add(memory[executionPointer + argWords.size() + 1]);
                argCount++;
            }
        }
        Word32[] vals = new Word32[3];
        for (int i = 0; i < args.length; i++) {
            byte arg = args[i];
            if (arg == ALL_HIGH) {
                vals[i] = argWords.get(0);
                argWords.remove(0);
            } else {
                vals[i] = registers[args[i]];
            }
        }
        int nEP = executionPointer + 1 + argCount;
        switch (op) {
            case MOV:
                registers[args[1]] = vals[0];
                break;
            case LOAD:
                registers[args[1]] = memory[vals[0].intValue()];
                break;
            case SAVE:
                memory[vals[0].intValue()] = registers[args[1]];
                break;
            case NOT:
                registers[args[1]] = vals[0].not();
                break;
            case LSHIFT:
                registers[args[1]] = vals[0].lshift();
                break;
            case RSHIFT:
                registers[args[1]] = vals[0].rshift();
                break;
            case FTOI:
                registers[args[1]] = vals[0].ftoi();
                break;
            case ITOF:
                registers[args[1]] = vals[0].itof();
                break;
            case UTOI:
                registers[args[1]] = vals[0].utoi();
                break;
            case ITOU:
                registers[args[1]] = vals[0].itou();
                break;
            case SWP:
                registers[args[0]] = vals[1];
                registers[args[1]] = vals[0];
                break;
            case PUSH:
                stack.push(vals[0]);
                break;
            case JAD:
                nEP = vals[0].intValue();
                break;
            case JOF:
                nEP = executionPointer + vals[0].intValue();
                break;
            case POP:
                registers[args[0]] = stack.pop();
                break;
            case PEEK:
                registers[args[0]] = stack.peek();
                break;
            case ADD:
                registers[args[2]] = vals[0].add(vals[1]);
                break;
            case SUB:
                registers[args[2]] = vals[0].sub(vals[1]);
                break;
            case MUL:
                registers[args[2]] = vals[0].mul(vals[1]);
                break;
            case DIV:
                registers[args[2]] = vals[0].div(vals[1]);
                break;
            case U_ADD:
                registers[args[2]] = vals[0].uadd(vals[1]);
                break;
            case U_SUB:
                registers[args[2]] = vals[0].usub(vals[1]);
                break;
            case U_MUL:
                registers[args[2]] = vals[0].umul(vals[1]);
                break;
            case U_DIV:
                registers[args[2]] = vals[0].udiv(vals[1]);
                break;
            case F_ADD:
                registers[args[2]] = vals[0].fadd(vals[1]);
                break;
            case F_SUB:
                registers[args[2]] = vals[0].fsub(vals[1]);
                break;
            case F_MUL:
                registers[args[2]] = vals[0].fmul(vals[1]);
                break;
            case F_DIV:
                registers[args[2]] = vals[0].fdiv(vals[1]);
                break;
            case AND:
                registers[args[2]] = vals[0].and(vals[1]);
                break;
            case OR:
                registers[args[2]] = vals[0].or(vals[1]);
                break;
            case XOR:
                registers[args[2]] = vals[0].xor(vals[1]);
                break;
            case JAIZ:
                if (vals[0].value() == 0) {
                    nEP = vals[1].intValue();
                }
                break;
            case JANZ:
                if (vals[0].value() != 0) {
                    nEP = vals[1].intValue();
                }
                break;
            case JALZ:
                if (vals[0].value() > 0) {
                    nEP = vals[1].intValue();
                }
                break;
            case JASZ:
                if (vals[0].value() < 0) {
                    nEP = vals[1].intValue();
                }
                break;
            case JOIZ:
                if (vals[0].value() == 0) {
                    nEP = executionPointer + vals[1].intValue();
                }
                break;
            case JONZ:
                if (vals[0].value() != 0) {
                    nEP = executionPointer + vals[1].intValue();
                }
                break;
            case JOLZ:
                if (vals[0].value() > 0) {
                    nEP = executionPointer + vals[1].intValue();
                }
                break;
            case JOSZ:
                if (vals[0].value() < 0) {
                    nEP = executionPointer + vals[1].intValue();
                }
                break;
            case SYSCALL:
                syscall(vals[0], vals[1]);
                break;
            case JAEQ:
                if (vals[0].value() == vals[1].value()) {
                    nEP = vals[2].intValue();
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
