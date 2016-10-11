import java.util.Scanner

import common.Util
import data.Word32

import scala.collection.mutable

class Processor extends Util {
  private type ValueConsumer = Word32 => Any
  private type ValueBiConsumer = (Word32, Word32) => Any
  private type RegisterConsumer = Register => Any
  private type RegisterBiConsumer = (Register, Register) => Any
  private type ValueRegisterConsumer = (Value, Register) => Any
  private type UnaryFunction = Word32 => Word32
  private type BinaryFunction = (Word32, Word32) => Word32
  private type StringRegisterConsumer = (String, Register) => Any
  private type StringConsumer = String => Any
  private type Predicate = Word32 => Boolean
  private type BiPredicate = (Word32, Word32) => Boolean
  private type Action = Any => Any
  private val stack = new mutable.Stack[Word32]()
  private val registers = buildRegisters()
  private val memory = buildMemory(2 << 8)
  private val MEM_PRINT_EMPTY_LINES = false
  private var program: Array[Instruction] = null
  private var programPointer: Int = 0
  private var labels: mutable.Map[String, Int] = new mutable.HashMap[String, Int]
  private var run = false

  def loadProgram(input: String): Unit = {
    val lines: Array[String] = input.split("\n")
    var i = 0
    program = lines.map(x => {
      if (x.startsWith("_")) {
        val scan = new Scanner(x.substring(1))
        val label = scan.next()
        labels(label) = i
      }
      i = i + 1
      parseLine(x)
    })
  }

  def start(): Unit = {
    programPointer = 0
    run = true
    while (run) {
      program(programPointer).invoke()
      programPointer = programPointer + 1
      if (programPointer >= program.length) run = false
    }
  }

  override def toString: String = {
    val sb: StringBuilder = new StringBuilder("Processor:\n")
    sb.append("Registers:\n")
    //Simply print out all registers in order
    registers.foreach(elem => sb.append("%s    %s%n".format(elem.name, elem.content.toString)))
    //Print out the stack, with a running index
    sb.append("Stack:\n")
    stack.zipWithIndex.foreach(elem => sb.append("%s %s\n".format(hex(stack.length - elem._2 - 1, 4), elem._1)))
    //Print memory
    sb.append("Memory:\n            ")
    Range(0, 16).foreach(x => sb.append("%11s".format(hex(x, 1))))
    sb.append("\n")
    memory.grouped(16).zipWithIndex.foreach(line => {
      val elem = line._1
      val linenum = line._2
      if (MEM_PRINT_EMPTY_LINES || !elem.forall(x => x.value == 0)) {
        //Prepare printed strings
        val s = elem.map(x => hex(x.value))
        //Print start of line
        sb.append(hex(linenum) + "   ")
        elem.map(x => hex(x.value)).foreach(hexstring => sb.append(hexstring + " "))
        sb.append("\n")
      }
    })
    sb.toString()
  }

  private def parseLine(str: String): Instruction = {
    val hasLabel: Boolean = str.startsWith("_")
    if (hasLabel) {
      val scan: Scanner = new Scanner(str)
      scan.next()
      return parseLine(scan.nextLine().trim)
    }
    val tokens: Array[String] = str.split(" ")
    val opcode = tokens(0)
    opcode match {
      case ("PUSH") => new ValueConsumerOperation(parseValue(tokens(1)), (x: Word32) => push(x), str)

      case ("SYSCALL") => new ValueBiConsumerOperation(parseValue(tokens(1)), parseValue(tokens(2)), (x: Word32, y: Word32) => syscall(x, y), str)

      case ("POP") => new RegisterConsumerOperation(getRegister(tokens(1)), (x: Register) => x.set(pop()), str)
      case ("PEEK") => new RegisterConsumerOperation(getRegister(tokens(1)), (x: Register) => x.set(peek()), str)

      case ("SWP") => new RegisterBiConsumerOperation(getRegister(tokens(1)), getRegister(tokens(2)), (x: Register, y: Register) => {
        val temp: Word32 = x.content
        x.set(y.content)
        y.set(temp)
      }, str)

      case ("LOAD") => new ValueRegisterConsumerOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Value, y: Register) => y.set(memory(x.getValue.intValue)), str)
      case ("SAVE") => new ValueRegisterConsumerOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Value, y: Register) => memory.update(x.getValue.intValue, y.content), str)

      case ("MOV") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x, str)
      case ("NOT") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.not, str)
      case ("LSHIFT") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.lshift, str)
      case ("RSHIFT") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.rshift, str)
      case ("FTOI") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.ftoi(), str)
      case ("ITOF") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.itof(), str)
      case ("ITOU") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.itou(), str)
      case ("UTOI") => new UnaryOperation(parseValue(tokens(1)), getRegister(tokens(2)), (x: Word32) => x.utoi(), str)

      case ("PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(Integer.valueOf(str))), str)
      case ("U_PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(Integer.parseUnsignedInt(str))), str)
      case ("F_PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(java.lang.Float.floatToIntBits(java.lang.Float.valueOf(str)))), str)

      case ("ADD") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x add y, str)
      case ("SUB") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x sub y, str)
      case ("MUL") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x mul y, str)
      case ("DIV") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x div y, str)

      case ("U_ADD") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x uadd y, str)
      case ("U_SUB") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x usub y, str)
      case ("U_MUL") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x umul y, str)
      case ("U_DIV") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x udiv y, str)

      case ("F_ADD") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x fadd y, str)
      case ("F_SUB") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x fsub y, str)
      case ("F_MUL") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x fmul y, str)
      case ("F_DIV") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x fdiv y, str)

      case ("AND") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x and y, str)
      case ("OR") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x or y, str)
      case ("XOR") => new BinaryOperation(parseValue(tokens(1)), parseValue(tokens(2)), getRegister(tokens(3)), (x: Word32, y: Word32) => x xor y, str)

      case ("JMP") => new StringConsumerOperation(tokens(1), (x: String) => goto(x), str)
      case ("JIZ") => new PredicateJump(parseValue(tokens(1)), tokens(2), (x: Word32) => x.intValue == 0, str)
      case ("JNZ") => new PredicateJump(parseValue(tokens(1)), tokens(2), (x: Word32) => x.intValue != 0, str)
      case ("JLZ") => new PredicateJump(parseValue(tokens(1)), tokens(2), (x: Word32) => x.intValue < 0, str)
      case ("JSZ") => new PredicateJump(parseValue(tokens(1)), tokens(2), (x: Word32) => x.intValue > 0, str)

      case ("JEQ") => new BiPredicateJump(parseValue(tokens(1)), parseValue(tokens(2)), tokens(3), (x: Word32, y: Word32) => x.intValue == y.intValue, str)

      case ("NOOP") => new ActionOperation(Any => Any, str)
      case ("WAIT") => new ActionOperation(Any => Any, str)
      case ("HALT") => new ActionOperation(Any => halt(), str)

    }
  }

  private def load(ptr: Int) = memory(ptr)

  private def save(ptr: Int, word: Word32) = memory.update(ptr, word)

  private def push(word: Word32) = stack.push(word)

  private def pop() = stack.pop()

  private def peek() = stack.top

  private def store(reg: Int, word: Word32) = registers(reg).set(word)

  private def retrieve(reg: Int) = registers(reg).get()

  private def goto(lbl: String): Unit = {
    programPointer = labels(lbl) - 1
  }

  private def halt(): Unit = {
    run = false
  }

  private def syscall(call: Word32, argument: Word32): Unit = {

  }

  private def getRegister(name: String): Register = {
    registers(Integer.valueOf(name.substring(1, 2)))
  }

  private def parseValue(str: String): Value = {
    if (str.startsWith("R")) {
      val number: Int = Integer.valueOf(str.substring(1, 2))
      new RegisterValue(registers(number))
    }
    else if (str.startsWith("0x")) {
      val value: Int = Integer.valueOf(str.substring(2), 16)
      new Word(new Word32(value))
    }
    else {
      val value: Int = Integer.valueOf(str, 16)
      new Word(new Word32(value))
    }
  }

  private def buildRegisters(): Array[Register] = {
    val registers: Array[Register] = new Array[Register](10)
    Range(0, 10).foreach(a => registers.update(a, new Register("R" + a)))
    registers
  }

  private def buildMemory(length: Int): Array[Word32] = {
    val memory: Array[Word32] = new Array[Word32](length)
    Range(0, length).foreach(a => memory.update(a, new Word32(0)))
    memory
  }

  private abstract class Value() {
    def getValue: Word32
  }

  private abstract class Instruction(line: String) {
    def invoke()

    override def toString = line
  }

  class Register(n: String) extends Util {
    val name = n
    var content: Word32 = new Word32(0)

    override def toString = String.format("%s:%s", n, content.toString)

    def set(word: Word32) = content = word

    def get() = content
  }

  private class Word(word: Word32) extends Value() {
    override def getValue = word
  }

  private class RegisterValue(register: Register) extends Value() {
    override def getValue = register.content
  }

  private class ValueConsumerOperation(arg1: Value, func: ValueConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(arg1.getValue)
  }

  private class ValueBiConsumerOperation(arg1: Value, arg2: Value, func: ValueBiConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(arg1.getValue, arg2.getValue)
  }

  private class RegisterConsumerOperation(reg1: Register, func: RegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(reg1)
  }

  private class RegisterBiConsumerOperation(reg1: Register, reg2: Register, func: RegisterBiConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(reg1, reg2)
  }

  private class PredicateJump(arg1: Value, lbl: String, func: Predicate, line: String) extends Instruction(line: String) {
    override def invoke() = if (func(arg1.getValue)) goto(lbl)
  }

  private class BiPredicateJump(arg1: Value, arg2: Value, lbl: String, func: BiPredicate, line: String) extends Instruction(line: String) {
    override def invoke() = if (func(arg1.getValue, arg2.getValue)) goto(lbl)
  }

  private class ValueRegisterConsumerOperation(arg1: Value, arg2: Register, func: ValueRegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(arg1, arg2)
  }

  private class UnaryOperation(arg1: Value, dest: Register, func: UnaryFunction, line: String) extends Instruction(line: String) {
    override def invoke() = dest.set(func(arg1.getValue))
  }

  private class BinaryOperation(arg1: Value, arg2: Value, dest: Register, func: BinaryFunction, line: String) extends Instruction(line: String) {
    override def invoke() = dest.set(func(arg1.getValue, arg2.getValue))
  }

  private class StringRegisterConsumerOperation(str: String, reg: Register, func: StringRegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(str, reg)
  }

  private class StringConsumerOperation(str: String, func: StringConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(str)
  }

  private class ActionOperation(func: Action, line: String) extends Instruction(line: String) {
    override def invoke() = func()
  }

}

object Main {
  def main(args: Array[String]): Unit = {
    val p: Processor = new Processor
    p.loadProgram("MOV 0x30 R9\nMOV R9 R0\nPUSH 0x1\nPUSH 0x1\n_LOOP POP R1\nPOP R2\nADD R1 R2 R3\nSUB R9 R0 R4\nSAVE R4 R3\nPUSH R2\nPUSH R1\nPUSH R3\nSUB R0 0x1 R0\nJNZ R0 LOOP")
    p.start()
    println(p.toString)
  }
}