package assembler

import common.ALL_HIGH
import common.ALL_LOW
import common.OpCode
import common.Word32

import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

/**
 * @author Chris Gold
 * @version 1.0
 */
object Assembler {

    @Throws(AssemblyException::class)
    fun parse(lines: Array<String>): List<Instruction> {
        var lines = lines
        //Split into lines, discard comments
        val lList = Arrays.stream(lines).filter { x -> !x.startsWith("//") && !x.startsWith("#") }.collect(Collectors.toList())
        lines = lList.toTypedArray()
        //Read in labels
        //Map from label to line number
        val labels = HashMap<String, Int>()
        for (i in lines.indices) {
            if (lines[i].startsWith("_")) {
                labels[Scanner(lines[i]).next().substring(1)] = i
            }
        }
        val instructions = ArrayList<Instruction>()
        //For every line
        for (i in lines.indices) {
            val line = lines[i]
            val scan = Scanner(line)
            //Read opcode as string
            var o = scan.next()
            //Skip label
            if (o.startsWith("_")) {
                o = scan.next()
            }
            val op = OpCode.get(o) ?: throw AssemblyException(i, String.format("Encountered unknown OpCode: %s", o))
            //null is returned as error
            //Take apart rest of line into tokens
            val tokens = scan.nextLine().trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //If the opcode is a jump, check if label exists
            if (op.isJump) {
                val lbl: String
                when (op) {
                    OpCode.JMP -> {
                        lbl = tokens[0]
                        instructions.add(JumpInstruction(op, ALL_LOW, ALL_LOW, lbl, labels[lbl]!!, Word32(0), Word32(0), line))
                    }
                    else -> {
                        lbl = tokens[1]
                        instructions.add(JumpInstruction(op, parseArgument(tokens[0]), ALL_LOW, lbl, labels[lbl]!!, parseWord(tokens[0]), Word32.ZEROES, line))
                    }
                }
                if (!labels.containsKey(lbl)) {
                    throw AssemblyException(i, String.format("Encountered unknown label: %s", lbl))
                }
            } else if (op.isLiteral) {
                when (op) {
                    OpCode.PUT -> instructions.add(OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.valueOf(tokens[0])!!, line))
                    OpCode.F_PUT -> instructions.add(OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.fromFloat(java.lang.Float.valueOf(tokens[0])), line))
                    OpCode.U_PUT -> instructions.add(OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.valueOf(tokens[0])!!, line))
                }
            } else {
                var arg1 = ALL_LOW
                var arg2 = ALL_LOW
                var arg3 = ALL_LOW
                val words = ArrayList<Word32>()
                //Use fallthrough for easier recognition of arg count
                when (tokens.size) {
                    3 -> {
                        arg3 = parseArgument(tokens[2])
                        if (arg3 == ALL_HIGH) {
                            words.add(parseWord(tokens[1]))
                        }
                        arg2 = parseArgument(tokens[1])
                        if (arg2 == ALL_HIGH) {
                            words.add(parseWord(tokens[1]))
                        }
                        arg1 = parseArgument(tokens[0])
                        if (arg1 == ALL_HIGH) {
                            words.add(parseWord(tokens[0]))
                        }
                    }
                    2 -> {
                        arg2 = parseArgument(tokens[1])
                        if (arg2 == ALL_HIGH) {
                            words.add(parseWord(tokens[1]))
                        }
                        arg1 = parseArgument(tokens[0])
                        if (arg1 == ALL_HIGH) {
                            words.add(parseWord(tokens[0]))
                        }
                    }
                    1 -> {
                        arg1 = parseArgument(tokens[0])
                        if (arg1 == ALL_HIGH) {
                            words.add(parseWord(tokens[0]))
                        }
                    }
                }
                Collections.reverse(words)
                when (words.size) {
                    0 -> instructions.add(NoWordInstruction(op, arg1, arg2, arg3, line))
                    1 -> instructions.add(OneWordInstruction(op, arg1, arg2, arg3, words[0], line))
                    2 -> instructions.add(TwoWordInstruction(op, arg1, arg2, arg3, words[0], words[1], line))
                    3 -> instructions.add(ThreeWordInstruction(op, arg1, arg2, arg3, words[0], words[1], words[2], line))
                    else -> throw RuntimeException()
                }
            }
        }
        return instructions
    }

    @Throws(AssemblyException::class)
    fun parse(program: String): List<Instruction> {
        return parse(program.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    }

    @Throws(AssemblyException::class)
    fun assemble(program: String): Array<Word32> {
        return assemble(parse(program))
    }

    fun assemble(instructions: List<Instruction>): Array<Word32> {
        val words = ArrayList<Word32>()
        //Shows the point at which every line begins
        val pointers = IntArray(instructions.size)
        var currentPos = 0
        for (i in instructions.indices) {
            pointers[i] = currentPos
            currentPos = currentPos + instructions[i].width
        }
        for (i in instructions.indices) {
            var inst = instructions[i]
            val op = inst.opCode
            //We swap out every Jump instruction with the equivalent Jump to offset.
            if (inst is JumpInstruction) {
                val jumpToLine = pointers[inst.jumpto]
                val currentLine = pointers[i]
                val offset = jumpToLine - currentLine
                val address = Word32(offset)
                when (op) {
                    OpCode.JMP -> inst = OneWordInstruction(OpCode.JOF, ALL_HIGH, ALL_LOW, ALL_LOW, address, inst.toString() + " CONV")
                    OpCode.JIZ -> if (inst.arg1 == ALL_HIGH) {
                        inst = TwoWordInstruction(OpCode.JOIZ, inst.arg1, ALL_HIGH, ALL_HIGH, inst.w1, address, inst.toString() + " CONV")
                    } else {
                        inst = OneWordInstruction(OpCode.JOIZ, inst.arg1, ALL_HIGH, ALL_LOW, address, inst.toString() + " CONV")
                    }
                    OpCode.JNZ -> if (inst.arg1 == ALL_HIGH) {
                        inst = TwoWordInstruction(OpCode.JONZ, inst.arg1, ALL_HIGH, ALL_HIGH, inst.w1, address, inst.toString() + " CONV")
                    } else {
                        inst = OneWordInstruction(OpCode.JONZ, inst.arg1, ALL_HIGH, ALL_LOW, address, inst.toString() + " CONV")
                    }
                    OpCode.JLZ -> if (inst.arg1 == ALL_HIGH) {
                        inst = TwoWordInstruction(OpCode.JOLZ, inst.arg1, ALL_HIGH, ALL_HIGH, inst.w1, address, inst.toString() + " CONV")
                    } else {
                        inst = OneWordInstruction(OpCode.JOLZ, inst.arg1, ALL_HIGH, ALL_LOW, address, inst.toString() + " CONV")
                    }
                    OpCode.JSZ -> if (inst.arg1 == ALL_HIGH) {
                        inst = TwoWordInstruction(OpCode.JOSZ, inst.arg1, ALL_HIGH, ALL_HIGH, inst.w1, address, inst.toString() + " CONV")
                    } else {
                        inst = OneWordInstruction(OpCode.JOSZ, inst.arg1, ALL_HIGH, ALL_LOW, address, inst.toString() + " CONV")
                    }
                    else -> throw RuntimeException()
                }
            }
            val iWords = inst.toWords()
            println(String.format("%20s  :  %-30s", inst.toString(), Word32.arrayToString(iWords)))
            Collections.addAll(words, *iWords)
        }
        return words.toTypedArray()
    }

    private fun parseArgument(s: String): Byte {
        val trimmed = s.trim { it <= ' ' }
        return if (trimmed.startsWith("R")) {
            s.substring(1).toByte()
        } else {
            ALL_HIGH
        }
    }

    private fun parseWord(s: String): Word32 {
        return Word32.valueOf(s) ?: Word32.ZEROES
    }

}
