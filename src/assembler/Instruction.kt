package assembler

import common.ALL_HIGH
import common.OpCode
import common.Word32
import common.bytesToWords

sealed class Instruction(val opCode: OpCode, val arg1: Byte, val arg2: Byte, val arg3: Byte, val lineform: String) {
    abstract fun toBytes(): ByteArray
    fun toWords(): Array<Word32> = toBytes().bytesToWords()
    abstract val width: Int
    override fun toString() = lineform
}

class NoWordInstruction(opCode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) : Instruction(opCode, arg1, arg2, arg3, lineform) {
    override fun toBytes(): ByteArray = byteArrayOf(opCode.code, arg1, arg2, arg3)
    override val width = 1
}

class OneWordInstruction(opCode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, val w1: Word32, lineform: String) : Instruction(opCode, arg1, arg2, arg3, lineform) {
    override fun toBytes() = byteArrayOf(opCode.code, arg1, arg2, arg3, *w1.bytes())
    override val width = 2
}

open class TwoWordInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, val w1: Word32, val w2: Word32, lineform: String) : Instruction(opcode, arg1, arg2, arg3, lineform) {
    override fun toBytes() = byteArrayOf(opCode.code, arg1, arg2, arg3, *w1.bytes(), *w2.bytes())
    override val width = 3
}

class ThreeWordInstruction(opCode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, val w1: Word32, val w2: Word32, val w3: Word32, lineform: String) : Instruction(opCode, arg1, arg2, arg3, lineform) {
    override fun toBytes() = byteArrayOf(opCode.code, arg1, arg2, arg3, *w1.bytes(), *w2.bytes(), *w3.bytes())
    override val width = 4
}

class JumpInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, val label: String, val jumpto: Int, w1: Word32, w2: Word32, lineform: String) : TwoWordInstruction(opcode, arg1, arg2, 0x00, w1, w2, lineform) {
    override fun toBytes(): ByteArray = byteArrayOf(opCode.code, arg1, arg2, ALL_HIGH)
    override val width = when (opcode) {
        OpCode.JMP -> 2
        OpCode.JIZ -> if (arg1 == ALL_HIGH) 3 else 2
        OpCode.JNZ -> if (arg1 == ALL_HIGH) 3 else 2
        OpCode.JLZ -> if (arg1 == ALL_HIGH) 3 else 2
        OpCode.JSZ -> if (arg1 == ALL_HIGH) 3 else 2
        else -> -1
    }
}