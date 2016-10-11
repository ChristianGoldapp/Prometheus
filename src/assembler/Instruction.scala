package assembler

import data.Word32

/**
  * @author Christian Goldapp
  * @version 1.0
  */
abstract class Instruction(opcode: OpCode, a1: Byte, a2: Byte, a3: Byte, lineform: String) {
  def getBytes: Array[Byte]

  def getWords: Array[Word32] = Word32.bytesToWords(getBytes)

  def getOpCode = opcode

  def getWidth = 1

  override def toString = lineform
}

class NoWordInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) extends Instruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) {
  override def getBytes: Array[Byte] = Array(opcode.code, arg1, arg2, arg3)
  override def getWidth = 1
}

class OneWordInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, w1: Word32, lineform: String) extends Instruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) {

  override def getWords: Array[Word32] = Word32.bytesToWords(getBytes)

  override def getBytes: Array[Byte] = {
    val w1bytes = w1.bytes()
    Array(opcode.code, arg1, arg2, arg3, w1bytes(0), w1bytes(1), w1bytes(2), w1bytes(3))
  }

  override def getWidth = 2

}

class TwoWordInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, w1: Word32, w2: Word32, lineform: String) extends Instruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) {
  override def getBytes: Array[Byte] = {
    val w1bytes = w1.bytes()
    val w2bytes = w2.bytes()

    Array(opcode.code, arg1, arg2, arg3, w1bytes(0), w1bytes(1), w1bytes(2), w1bytes(3), w2bytes(0), w2bytes(1), w2bytes(2), w2bytes(3))
  }

  override def getWidth = 3
}

class ThreeWordInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, w1: Word32, w2: Word32, w3: Word32, lineform: String) extends Instruction(opcode: OpCode, arg1: Byte, arg2: Byte, arg3: Byte, lineform: String) {
  override def getBytes: Array[Byte] = {
    val w1bytes = w1.bytes()
    val w2bytes = w2.bytes()
    val w3bytes = w3.bytes()

    Array(opcode.code, arg1, arg2, arg3, w1bytes(0), w1bytes(1), w1bytes(2), w1bytes(3), w2bytes(0), w2bytes(1), w2bytes(2), w2bytes(3), w3bytes(0), w3bytes(1), w3bytes(2), w3bytes(3))
  }

  override def getWidth = 4
}

//Placeholder for jumps as addresses are calculated later
class JumpInstruction(opcode: OpCode, arg1: Byte, arg2: Byte, label: String, jumpto: Int, w1: Word32, w2: Word32, lineform: String) extends TwoWordInstruction(opcode: OpCode, arg1, arg2, 0x00, w1, w2, lineform: String) {
  override def getBytes: Array[Byte] = Array(opcode.code, arg1, arg2, 0xFF.toByte)

  //This is the width used for the equivalent jump-to-address
  override def getWidth = {
    opcode match {
      case OpCode.JMP => 2
      case OpCode.JIZ => if (arg1 == 0xFF) 3 else 2
      case OpCode.JNZ => if (arg1 == 0xFF) 3 else 2
      case OpCode.JLZ => if (arg1 == 0xFF) 3 else 2
      case OpCode.JSZ => if (arg1 == 0xFF) 3 else 2
    }
  }
  def getJump = jumpto

  def getArg1 = arg1

  def getArg2 = arg2

  def getWord1 = w1

  def getWord2 = w2
}