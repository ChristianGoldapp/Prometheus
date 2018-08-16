import common.ALL_HIGH
import common.OpCode
import common.Word32

import java.util.*

/**
 * @author Chris Gold
 * @version 1.0
 */
class Processor @JvmOverloads constructor(memSize: Int = DEFAULT_MEMSIZE, regSize: Int = DEFAULT_REGSIZE) {
    private val stdin = Scanner(System.`in`)
    private val memory: Array<Word32> = Array(memSize, { Word32.ZEROES })
    private val registers: Array<Word32> = Array(regSize, { Word32.ZEROES })
    private val stack: Stack<Word32> = Stack()
    private var executionPointer: Int = 0

    fun setProgram(program: Array<Word32>) {
        Arrays.fill(memory, Word32(0))
        System.arraycopy(program, 0, memory, 0, program.size)
    }

    fun run() {
        while (executionPointer != -1) {
            executionPointer = executeCurrentInstruction()
        }
    }

    fun executeCurrentInstruction(): Int {
        val firstWord = memory[executionPointer]
        val firstWordBytes = firstWord.bytes()
        val op = OpCode[firstWordBytes[0]]
        val argWords = ArrayList<Word32>(3)
        var argCount = 0
        val args = firstWordBytes.copyOfRange(1, 4)
        for (arg in args) {
            if (arg == ALL_HIGH) {
                argWords.add(memory[executionPointer + argWords.size + 1])
                argCount++
            }
        }
        val vals = Array(3, { Word32.ONES })
        for (i in args.indices) {
            vals[i] = if (ALL_HIGH == args[i]) argWords.removeAt(0)
            else registers[args[i]]
        }
        var nEP = executionPointer + 1 + argCount
        when (op) {
            OpCode.MOV -> registers[args[1]] = vals[0]
            OpCode.LOAD -> registers[args[1]] = memory[vals[0].intValue()]
            OpCode.SAVE -> memory[vals[0].intValue()] = registers[args[1]]
            OpCode.NOT -> registers[args[1]] = vals[0].not()
            OpCode.LSHIFT -> registers[args[1]] = vals[0].lshift()
            OpCode.RSHIFT -> registers[args[1]] = vals[0].rshift()
            OpCode.FTOI -> registers[args[1]] = vals[0].ftoi()
            OpCode.ITOF -> registers[args[1]] = vals[0].itof()
            OpCode.UTOI -> registers[args[1]] = vals[0].utoi()
            OpCode.ITOU -> registers[args[1]] = vals[0].itou()
            OpCode.SWP -> {
                registers[args[0]] = vals[1]
                registers[args[1]] = vals[0]
            }
            OpCode.PUSH -> stack.push(vals[0])
            OpCode.JAD -> nEP = vals[0].intValue()
            OpCode.JOF -> nEP = executionPointer + vals[0].intValue()
            OpCode.POP -> registers[args[0]] = stack.pop()
            OpCode.PEEK -> registers[args[0]] = stack.peek()
            OpCode.ADD -> registers[args[2]] = vals[0].add(vals[1])
            OpCode.SUB -> registers[args[2]] = vals[0].sub(vals[1])
            OpCode.MUL -> registers[args[2]] = vals[0].mul(vals[1])
            OpCode.DIV -> registers[args[2]] = vals[0].div(vals[1])
            OpCode.U_ADD -> registers[args[2]] = vals[0].uadd(vals[1])
            OpCode.U_SUB -> registers[args[2]] = vals[0].usub(vals[1])
            OpCode.U_MUL -> registers[args[2]] = vals[0].umul(vals[1])
            OpCode.U_DIV -> registers[args[2]] = vals[0].udiv(vals[1])
            OpCode.F_ADD -> registers[args[2]] = vals[0].fadd(vals[1])
            OpCode.F_SUB -> registers[args[2]] = vals[0].fsub(vals[1])
            OpCode.F_MUL -> registers[args[2]] = vals[0].fmul(vals[1])
            OpCode.F_DIV -> registers[args[2]] = vals[0].fdiv(vals[1])
            OpCode.AND -> registers[args[2]] = vals[0].and(vals[1])
            OpCode.OR -> registers[args[2]] = vals[0].or(vals[1])
            OpCode.XOR -> registers[args[2]] = vals[0].xor(vals[1])
            OpCode.JAIZ -> if (vals[0].value == 0) {
                nEP = vals[1].intValue()
            }
            OpCode.JANZ -> if (vals[0].value != 0) {
                nEP = vals[1].intValue()
            }
            OpCode.JALZ -> if (vals[0].value > 0) {
                nEP = vals[1].intValue()
            }
            OpCode.JASZ -> if (vals[0].value < 0) {
                nEP = vals[1].intValue()
            }
            OpCode.JOIZ -> if (vals[0].value == 0) {
                nEP = executionPointer + vals[1].intValue()
            }
            OpCode.JONZ -> if (vals[0].value != 0) {
                nEP = executionPointer + vals[1].intValue()
            }
            OpCode.JOLZ -> if (vals[0].value > 0) {
                nEP = executionPointer + vals[1].intValue()
            }
            OpCode.JOSZ -> if (vals[0].value < 0) {
                nEP = executionPointer + vals[1].intValue()
            }
            OpCode.SYSCALL -> {
            }
            OpCode.JAEQ -> if (vals[0].value == vals[1].value) {
                nEP = vals[2].intValue()
            }
            OpCode.NOOP -> {
            }
            OpCode.WAIT -> {
            }
            OpCode.HALT -> nEP = -1
            else -> throw RuntimeException()
        }//TODO
        return nEP
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Registers:\n")
        for (i in registers.indices) {
            sb.append(" R")
            sb.append(i)
            sb.append(":        ")
            sb.append(registers[i])
            sb.append("\n")
        }
        sb.append("Stack:\n")
        for (i in stack.indices.reversed()) {
            sb.append(String.format(" 0x%04X", i))
            sb.append(":   ")
            sb.append(stack[i])
            sb.append("\n")
        }
        sb.append("Memory:\n")
        sb.append(Word32.arrayToString(memory, 16))
        return sb.toString()
    }

    companion object {
        val DEFAULT_MEMSIZE = 2 shl 8
        val DEFAULT_REGSIZE = 10
    }

}

private operator fun <T> Array<T>.get(byte: Byte): T = get(byte.toInt())

private operator fun <T> Array<T>.set(byte: Byte, value: T?) = set(byte.toInt(), value!!)
