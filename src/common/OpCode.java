package common;

import java.util.Arrays;
import java.util.List;

/**
 * @author Chris Gold
 * @version 1.0
 */
public enum OpCode {
    //Value-Register
    MOV(0x10), LOAD(0x12), SAVE(0x13), NOT(0x50), LSHIFT(0x5E), RSHIFT(0x5F), FTOI(0x60), ITOF(0x61), UTOI(0x62), ITOU(0x63),
    //Register-Register
    SWP(0x11),
    //Value
    PUSH(0x71), JAD(0xF0),
    //Register
    POP(0x72), PEEK(0x70),
    //Value-Value-Register
    ADD(0x20), SUB(0x21), MUL(0x22), DIV(0x23), U_ADD(0x30), U_SUB(0x31), U_MUL(0x32), U_DIV(0x33), F_ADD(0x40), F_SUB(0x41), F_MUL(0x42), F_DIV(0x43), AND(0x51), OR(0x52), XOR(0x53),
    //Value-Value
    JAIZ(0xF1), JANZ(0xF2), JALZ(0xF3), JASZ(0xF4), SYSCALL(0xFE),
    //Value-Value-Value
    JAEQ(0xF5),
    //None
    NOOP(0x0F), WAIT(0x01), HALT(0x00),
    //String-Register
    PUT(0x10), U_PUT(0x10), F_PUT(0x10),
    //String
    JMP(0xF0),
    //Value-String
    JIZ(0xF1), JNZ(0xF2), JLZ(0xF3), JSZ(0xF4);

    static List<OpCode> jumps = Arrays.asList(JMP, JIZ, JNZ, JLZ, JSZ);
    static List<OpCode> literals = Arrays.asList(PUT, F_PUT, U_PUT);
    public final byte code;

    OpCode(int i) {
        this.code = (byte) i;
    }

    public static OpCode get(String s) {
        for (OpCode opCode : OpCode.values()) {
            if (opCode.toString().equalsIgnoreCase(s)) {
                return opCode;
            }
        }
        return null;
    }

    public static OpCode get(byte c) {
        for (OpCode opCode : OpCode.values()) {
            if (opCode.code == c) {
                return opCode;
            }
        }
        return null;
    }

    public boolean isJump() {
        return jumps.contains(this);
    }

    public boolean isLiteral() {
        return literals.contains(this);
    }
}
