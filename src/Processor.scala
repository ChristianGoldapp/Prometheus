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

  def retrieve(reg: Int) = registers(reg).get()

  def goto(lbl: String): Unit = {

  }

  def halt(): Unit = {

  }

  def syscall(call: Word32, argument: Word32): Unit = {

  }

  abstract class Instruction {

    abstract def invoke()

    abstract class UnaryOperation(arg1: Value, dest: Register) extends Instruction {

      class MOV extends Instruction {
        override def invoke() = dest.set(arg1.getValue)
      }

      class NOT extends Instruction {
        override def invoke() = dest.set(arg1.getValue.not)
      }

      class LSHIFT extends Instruction {
        override def invoke() = dest.set(arg1.getValue.lshift)
      }

      class RSHIFT extends Instruction {
        override def invoke() = dest.set(arg1.getValue.rshift)
      }

      class FTOI extends Instruction {
        override def invoke() = dest.set(arg1.getValue.ftoi())
      }

      class ITOF extends Instruction {
        override def invoke() = dest.set(arg1.getValue.itof())
      }

      class UTOI extends Instruction {
        override def invoke() = dest.set(arg1.getValue.utoi())
      }

      class ITOU extends Instruction {
        override def invoke() = dest.set(arg1.getValue.not)
      }

    }

    abstract class BinaryOperation(arg1: Value, arg2: Value, dest: Register) extends Instruction {

      class ADD extends Instruction {
        override def invoke() = dest.set(arg1.getValue.add(arg2.getValue))
      }

      class SUB extends Instruction {
        override def invoke() = dest.set(arg1.getValue.sub(arg2.getValue))
      }

      class MUL extends Instruction {
        override def invoke() = dest.set(arg1.getValue.mul(arg2.getValue))
      }

      class DIV extends Instruction {
        override def invoke() = dest.set(arg1.getValue.div(arg2.getValue))
      }

      class F_ADD extends Instruction {
        override def invoke() = dest.set(arg1.getValue.fadd(arg2.getValue))
      }

      class F_SUB extends Instruction {
        override def invoke() = dest.set(arg1.getValue.fsub(arg2.getValue))
      }

      class F_MUL extends Instruction {
        override def invoke() = dest.set(arg1.getValue.fmul(arg2.getValue))
      }

      class F_DIV extends Instruction {
        override def invoke() = dest.set(arg1.getValue.fdiv(arg2.getValue))
      }

      class U_ADD extends Instruction {
        override def invoke() = dest.set(arg1.getValue.uadd(arg2.getValue))
      }

      class U_SUB extends Instruction {
        override def invoke() = dest.set(arg1.getValue.usub(arg2.getValue))
      }

      class U_MUL extends Instruction {
        override def invoke() = dest.set(arg1.getValue.umul(arg2.getValue))
      }

      class U_DIV extends Instruction {
        override def invoke() = dest.set(arg1.getValue.udiv(arg2.getValue))
      }

      class AND extends Instruction {
        override def invoke() = dest.set(arg1.getValue.and(arg2.getValue))
      }

      class OR extends Instruction {
        override def invoke() = dest.set(arg1.getValue.or(arg2.getValue))
      }

      class XOR extends Instruction {
        override def invoke() = dest.set(arg1.getValue.xor(arg2.getValue))
      }

    }

    class SWP(arg1: Register, arg2: Register) extends Instruction {
      override def invoke() = {
        val word1 = arg1.content
        val word2 = arg2.content
        arg1.set(word2)
        arg2.set(word1)
      }
    }

    class LOAD(ptr: Value, reg: Register) extends Instruction {
      override def invoke() = reg.set(load(ptr.getValue.intValue))
    }

    class SAVE(arg1: Value, reg: Register) extends Instruction {
      override def invoke() = save(arg1.getValue.intValue, reg.content)
    }

    class PUSH(arg1: Value) extends Instruction {
      override def invoke() = {
        push(arg1.getValue)
      }
    }

    class POP(reg: Register) extends Instruction {
      override def invoke() = reg.set(pop())
    }

    class PEEK(reg: Register) extends Instruction {
      override def invoke() = reg.set(peek())
    }

    class JMP(lbl: String) extends Instruction {
      override def invoke() = goto(lbl)
    }

    abstract class UnaryJump(arg1: Value, lbl: String) extends Instruction {

      class JIZ extends Instruction {
        override def invoke() = if (arg1.getValue.intValue == 0) goto(lbl)
      }

      class JNZ extends Instruction {
        override def invoke() = if (arg1.getValue.intValue != 0) goto(lbl)
      }

      class JGZ extends Instruction {
        override def invoke() = if (arg1.getValue.intValue > 0) goto(lbl)
      }

      class JLZ extends Instruction {
        override def invoke() = if (arg1.getValue.intValue < 0) goto(lbl)
      }

    }

    class JEZ(arg1: Value, arg2: Value, lbl: String) extends Instruction {
      override def invoke(): Unit = if (arg1.getValue.intValue == arg1.getValue.intValue) goto(lbl)
    }

    object NOOP extends Instruction {
      override def invoke() = {}
    }

    object WAIT extends Instruction {
      override def invoke(): Unit = {}
    }

    object HALT extends Instruction {
      override def invoke(): Unit = halt()
    }

    class Syscall(arg1: Value, arg2: Value) {

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
