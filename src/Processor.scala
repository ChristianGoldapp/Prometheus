import java.util.Scanner

import scala.collection.mutable

/**
  * @author Christian Goldapp
  * @version 1.0
  */
class Processor {
  type ValueConsumer = Word32 => ()
  type ValueBiConsumer = (Word32, Word32) => ()
  type RegisterConsumer = Register => ()
  type RegisterBiConsumer = (Register, Register) => ()
  type ValueRegisterConsumer = (Value, Register) => ()
  type UnaryFunction = Word32 => Word32
  type BinaryFunction = (Word32, Word32) => Word32
  type StringRegisterConsumer = (String, Register) => ()
  type StringConsumer = String => ()
  type Predicate = Word32 => Boolean
  type BiPredicate = (Word32, Word32) => Boolean
  type Action = () => ()
  val stack = new mutable.Stack[Word32]()
  val registers = buildRegisters()
  val memory = new Array[Word32](2 >> 16)
  var program: Array[Instruction] = null
  var programPointer: Int = 0
  var labels: mutable.Map[String, Int] = new mutable.HashMap[String, Int]

  def load(ptr: Int) = memory(ptr)

  def save(ptr: Int, word: Word32) = memory.update(ptr, word)

  def push(word: Word32) = stack.push(word)

  def pop() = stack.pop()

  def peek() = stack.top

  def store(reg: Int, word: Word32) = registers(reg).set(word)

  def retrieve(reg: Int) = registers(reg).get()

  def goto(lbl: String): Unit = {
    programPointer = labels(lbl)
  }

  def halt(): Unit = {

  }

  def syscall(call: Word32, argument: Word32): Unit = {

  }

  def parseLine(str: String): Instruction = {
    val hasLabel: Boolean = str.startsWith("_")
    if (hasLabel) {
      val scan: Scanner = new Scanner(str)
      scan.next()
      return parseLine(scan.nextLine())
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

      case ("PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(Integer.valueOf(tokens(3)))), str)
      case ("U_PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(Integer.parseUnsignedInt(tokens(3)))), str)
      case ("F_PUT") => new StringRegisterConsumerOperation(tokens(1), getRegister(tokens(2)), (str: String, reg: Register) => reg.set(new Word32(java.lang.Float.floatToIntBits(java.lang.Float.valueOf(tokens(3))))), str)

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

      case ("JEZ") => new BiPredicateJump(parseValue(tokens(1)), parseValue(tokens(2)), tokens(3), (x: Word32, y: Word32) => x.intValue == y.intValue, str)

      case ("NOOP") => new ActionOperation(() => (), str)
      case ("WAIT") => new ActionOperation(() => (), str)
      case ("HALT") => new ActionOperation(() => halt(), str)

    }
  }

  def getRegister(name: String): Register = {
    registers(Integer.valueOf(name.substring(1, 2)))
  }

  def parseValue(str: String): Value = {
    if (str.startsWith("R")) {
      val number: Int = Integer.valueOf(str.substring(1, 2))
      new RegisterValue(registers(number))
    }
    else {
      val value: Int = Integer.valueOf(str, 16)
      new Word(new Word32(value))
    }
  }

  def buildRegisters(): Array[Register] = {
    val registers: Array[Register] = new Array[Register](10)
    for (a <- 0 to 9) {
      registers.update(a, new Register("R" + a))
    }
    registers
  }

  abstract class Instruction(line: String) {
    abstract def invoke()

    override def toString = line
  }

  abstract class Value() {
    def getValue: Word32
  }

  class Word(word: Word32) extends Value() {
    override def getValue = word
  }

  class RegisterValue(register: Register) extends Value() {
    override def getValue = register.content
  }

  class ValueConsumerOperation(arg1: Value, func: ValueConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(arg1.getValue)
  }

  class ValueBiConsumerOperation(arg1: Value, arg2: Value, func: ValueBiConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(arg1.getValue, arg2.getValue)
  }

  class RegisterConsumerOperation(reg1: Register, func: RegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(reg1)
  }

  class RegisterBiConsumerOperation(reg1: Register, reg2: Register, func: RegisterBiConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(reg1, reg2)
  }

  class PredicateJump(arg1: Value, lbl: String, func: Predicate, line: String) extends Instruction(line: String) {
    override def invoke() = if (func(arg1.getValue)) goto(lbl)
  }

  class BiPredicateJump(arg1: Value, arg2: Value, lbl: String, func: BiPredicate, line: String) extends Instruction(line: String) {
    override def invoke() = if (func(arg1.getValue, arg2.getValue)) goto(lbl)
  }

  class ValueRegisterConsumerOperation(arg1: Value, arg2: Register, func: ValueRegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = (arg1, arg2)
  }

  class UnaryOperation(arg1: Value, dest: Register, func: UnaryFunction, line: String) extends Instruction(line: String) {
    override def invoke() = dest.set(func(arg1.getValue))
  }

  class BinaryOperation(arg1: Value, arg2: Value, dest: Register, func: BinaryFunction, line: String) extends Instruction(line: String) {
    override def invoke() = dest.set(func(arg1.getValue, arg2.getValue))
  }

  class StringRegisterConsumerOperation(str: String, reg: Register, func: StringRegisterConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(str, reg)
  }

  class StringConsumerOperation(str: String, func: StringConsumer, line: String) extends Instruction(line: String) {
    override def invoke() = func(str)
  }

  class ActionOperation(func: Action, line: String) extends Instruction(line: String) {
    override def invoke() = func()
  }


}