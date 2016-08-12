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
  val registers = Array(new Register("A"), new Register("B"), new Register("C"), new Register("D"), new Register("E"), new Register("F"), new Register("G"), new Register("H"))
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

  abstract class Instruction {
    abstract def invoke()
  }

  abstract class Value(processor: Processor) {
    def getValue: Word32

    case class RegisterValue(processor: Processor, reg: Int) extends Value(processor) {
      override def getValue = processor.retrieve(reg)
    }

    case class Word(processor: Processor, word: Word32) extends Value(processor) {
      override def getValue = word
    }

  }

  class ValueConsumerOperation(arg1: Value, func: ValueConsumer) extends Instruction {
    override def invoke() = func(arg1.getValue)
  }

  class ValueBiConsumerOperation(arg1: Value, arg2: Value, func: ValueBiConsumer) extends Instruction {
    override def invoke() = func(arg1.getValue, arg2.getValue)
  }

  class RegisterConsumerOperation(reg1: Register, func: RegisterConsumer) extends Instruction {
    override def invoke() = func(reg1)
  }

  class RegisterBiConsumerOperation(reg1: Register, reg2: Register, func: RegisterBiConsumer) extends Instruction {
    override def invoke() = func(reg1, reg2)
  }

  class PredicateJump(arg1: Value, lbl: String, func: Predicate) extends Instruction {
    override def invoke() = if (func(arg1.getValue)) goto(lbl)
  }

  class BiPredicateJump(arg1: Value, arg2: Value, lbl: String, func: BiPredicate) extends Instruction {
    override def invoke() = if (func(arg1.getValue, arg2.getValue)) goto(lbl)
  }

  class ValueRegisterConsumerOperation(arg1: Value, arg2: Register, func: ValueRegisterConsumer) extends Instruction {
    override def invoke() = (arg1, arg2)
  }

  class UnaryOperation(arg1: Value, dest: Register, func: UnaryFunction) extends Instruction {
    override def invoke() = dest.set(func(arg1.getValue))
  }

  class BinaryOperation(arg1: Value, arg2: Value, dest: Register, func: BinaryFunction) extends Instruction {
    override def invoke() = dest.set(func(arg1.getValue, arg2.getValue))
  }

  class StringRegisterConsumerOperation(str: String, reg: Register, func: StringRegisterConsumer) extends Instruction {
    override def invoke() = func(str, reg)
  }

  class StringConsumerOperation(str: String, func: StringConsumer) extends Instruction {
    override def invoke() = func(str)
  }

  class ActionOperation(func: Action) extends Instruction {
    override def invoke() = func()
  }


}