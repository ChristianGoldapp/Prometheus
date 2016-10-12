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
        this.registers = new Word32[regSize];
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
        byte arg1 = firstWordBytes[1];
        byte arg2 = firstWordBytes[2];
        byte arg3 = firstWordBytes[3];
        List<Word32> argWords = new ArrayList<>(3);
        if (arg1 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
        }
        if (arg2 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
        }
        if (arg3 == ALL_HIGH) {
            argWords.add(memory[executionPointer + argWords.size() + 1]);
        }
        Word32 val1;
        if (arg1 == ALL_HIGH) {
            val1 = argWords.get(0);
        } else {
            val1 = registers[arg1];
        }
        Word32 val2;
        if (arg2 == ALL_HIGH) {
            val2 = argWords.get(1);
        } else {
            val2 = registers[arg2];
        }
        Word32 val3;
        if (arg3 == ALL_HIGH) {
            val3 = argWords.get(2);
        } else {
            val2 = registers[arg3];
        }
        int nEP = executionPointer + 1 + argWords.size();
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
                //TODO
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
                //TODO
                break;
            case JANZ:
                //TODO
                break;
            case JALZ:
                //TODO
                break;
            case JASZ:
                //TODO
                break;
            case SYSCALL:
                //TODO
                break;
            case JAEQ:
                //TODO
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
}
