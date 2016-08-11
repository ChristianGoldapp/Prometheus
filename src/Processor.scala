import scala.collection.mutable

/**
  * @author Christian Goldapp
  * @version 1.0
  */
class Processor {
  val stack = new mutable.Stack[Word32]()
  val registers = Array(new Register("A"), new Register("B"), new Register("C"), new Register("D"), new Register("E"), new Register("F"), new Register("G"), new Register("H"))
  val memory = new Array[Word32](2 >> 16)

  def load(ptr: Int) = memory(ptr)

  def save(ptr: Int, word: Word32) = memory.update(ptr, word)

  def push(word: Word32) = stack.push(word)

  def pop() = stack.pop()

  def peek() = stack.top

  def store(reg: Int, word: Word32) = registers(reg).set(word)

  def retrieve(reg: Int) = registers(reg).get

  def goto(lbl: String): Unit = {

  }

  def halt(): Unit ={

  }

  def syscall(call: Word32, argument: Word32): Unit ={

  }

  abstract class Instruction {

    abstract def invoke

    abstract class UnaryOperation(arg1: Value, dest: Register) extends Instruction {

      class NOT extends Instruction {
        override def invoke = dest set arg1.getValue.not
      }

    }

    abstract class BinaryOperation(arg1: Value, arg2: Value, dest: Register) extends Instruction

    abstract class RegisterOperation(arg1: Register, arg2: Register) extends Instruction

    abstract class IOOperation(ptr: Value, reg: Register) extends Instruction

    class Push(arg1: Value) extends Instruction {
      override def invoke = {
        push(arg1.getValue)
      }
    }

    class Pop(reg: Register) extends Instruction {
      override def invoke = reg.set(pop())
    }

    class Peek(reg: Register) extends Instruction {
      override def invoke = reg.set(peek())
    }

    class Jump(lbl: String) extends Instruction {
      override def invoke = goto(lbl)
    }

    abstract class UnaryJump(arg: Value, lbl: String) extends Instruction

    abstract class BinaryJump(arg1: Value, arg2: Value, lbl: String) extends Instruction

    object NOOP extends Instruction {
      override def invoke = {}
    }

    object WAIT extends Instruction {
      override def invoke: Unit = {}
    }

    object HALT extends Instruction {
      override def invoke: Unit = halt()
    }

    class Syscall(arg1: Value, arg2: Value){

    }

  }

  abstract class Value(processor: Processor) {
    def getValue: Word32

    case class Register(processor: Processor, reg: Int) extends Value(processor) {
      override def getValue = processor.retrieve(reg)
    }

    case class Word(processor: Processor, word: Word32) extends Value(processor) {
      override def getValue = word
    }

  }

}
