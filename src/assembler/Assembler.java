package assembler;

import common.OpCode;
import common.Word32;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chris Gold
 * @version 1.0
 */
public class Assembler {

    private static final byte ALL_LOW = 0;
    private static final byte ALL_HIGH = -1;
    
    public static Instruction[] parse(String program) throws AssemblyException {
        //Split into lines, discard comments
        List<String> lList = Arrays.stream(program.split("\n")).filter(x -> !x.startsWith("//") && !x.startsWith("#")).collect(Collectors.toList());
        String[] lines = lList.toArray(new String[lList.size()]);
        //Read in labels
        //Map from label to line number
        final Map<String, Integer> labels = new HashMap<>();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("_")) {
                labels.put(new Scanner(lines[i]).next().substring(1), i);
            }
        }
        Instruction[] instructions = new Instruction[lines.length];
        //For every line
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Scanner scan = new Scanner(line);
            //Read opcode as string
            String o = scan.next();
            //Skip label
            if (o.startsWith("_")) {
                o = scan.next();
            }
            OpCode op = OpCode.get(o);
            //null is returned as error
            if (op == null) {
                throw new AssemblyException(i, String.format("Encountered unknown OpCode: %s", o));
            }
            //Take apart rest of line into tokens
            String[] tokens = scan.nextLine().trim().split(" ");
            //If the opcode is a jump, check if label exists
            if (op.isJump()) {
                String lbl;
                switch (op) {
                    case JMP:
                        lbl = tokens[0];
                        instructions[i] = new JumpInstruction(op, ALL_LOW, ALL_LOW, lbl, labels.get(lbl), new Word32(0), new Word32(0), line);
                        break;
                    default:
                        lbl = tokens[1];
                        instructions[i] = new JumpInstruction(op, parseArgument(tokens[0]), ALL_LOW, lbl, labels.get(lbl), parseWord(tokens[0]), null, line);
                }
                if (!labels.containsKey(lbl)) {
                    throw new AssemblyException(i, String.format("Encountered unknown label: %s", lbl));
                }
            } else if (op.isLiteral()) {
                switch (op) {
                    case PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.valueOf(tokens[0]), line);
                        break;
                    case F_PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.fromFloat(Float.valueOf(tokens[0])), line);
                        break;
                    case U_PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, ALL_HIGH, parseArgument(tokens[1]), ALL_LOW, Word32.valueOf(tokens[0]), line);
                        break;
                }
            } else {
                byte arg1 = ALL_LOW;
                byte arg2 = ALL_LOW;
                byte arg3 = ALL_LOW;
                List<Word32> words = new ArrayList<>();
                //Use fallthrough for easier recognition of arg count
                switch (tokens.length) {
                    case 3:
                        arg3 = parseArgument(tokens[2]);
                        if (arg3 == ALL_HIGH) {
                            words.add(parseWord(tokens[1]));
                        }
                    case 2:
                        arg2 = parseArgument(tokens[1]);
                        if (arg2 == ALL_HIGH) {
                            words.add(parseWord(tokens[1]));
                        }
                    case 1:
                        arg1 = parseArgument(tokens[0]);
                        if (arg1 == ALL_HIGH) {
                            words.add(parseWord(tokens[0]));
                        }

                }
                Collections.reverse(words);
                switch (words.size()) {
                    case 0:
                        instructions[i] = new NoWordInstruction(op, arg1, arg2, arg3, line);
                        break;
                    case 1:
                        instructions[i] = new OneWordInstruction(op, arg1, arg2, arg3, words.get(0), line);
                        break;
                    case 2:
                        instructions[i] = new TwoWordInstruction(op, arg1, arg2, arg3, words.get(0), words.get(1), line);
                        break;
                    case 3:
                        instructions[i] = new ThreeWordInstruction(op, arg1, arg2, arg3, words.get(0), words.get(1), words.get(2), line);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        }
        return instructions;
    }

    public static Word32[] assemble(String program) throws AssemblyException {
        return assemble(parse(program));
    }

    public static Word32[] assemble(Instruction[] instructions) {
        List<Word32> words = new ArrayList<>();
        //Shows the point at which every line begins
        int[] pointers = new int[instructions.length];
        int currentPos = 0;
        for (int i = 0; i < instructions.length; i++) {
            pointers[i] = currentPos;
            currentPos = currentPos + instructions[i].getWidth();
        }
        for (int i = 0; i < instructions.length; i++) {
            Instruction inst = instructions[i];
            OpCode op = inst.getOpCode();
            //We swap out every Jump instruction with the equivalent Jump to offset.
            if (inst instanceof JumpInstruction) {
                JumpInstruction jInst = (JumpInstruction) inst;
                int jumpToLine = pointers[jInst.getJump()];
                int offset = jumpToLine - i;
                Word32 address = new Word32(offset);
                switch (op) {
                    case JMP:
                        inst = new OneWordInstruction(OpCode.JOF, ALL_HIGH, ALL_LOW, ALL_LOW, address, jInst.toString() + " CONV");
                        break;
                    case JIZ:
                        if (jInst.getArg1() == ALL_HIGH) {
                            inst = new TwoWordInstruction(OpCode.JOIZ, jInst.getArg1(), ALL_HIGH, ALL_HIGH, jInst.getWord1(), address, jInst.toString() + " CONV");
                        } else {
                            inst = new OneWordInstruction(OpCode.JOIZ, jInst.getArg1(), ALL_HIGH, ALL_LOW, address, jInst.toString() + " CONV");
                        }
                        break;
                    case JNZ:
                        if (jInst.getArg1() == ALL_HIGH) {
                            inst = new TwoWordInstruction(OpCode.JONZ, jInst.getArg1(), ALL_HIGH, ALL_HIGH, jInst.getWord1(), address, jInst.toString() + " CONV");
                        } else {
                            inst = new OneWordInstruction(OpCode.JONZ, jInst.getArg1(), ALL_HIGH, ALL_LOW, address, jInst.toString() + " CONV");
                        }
                        break;
                    case JLZ:
                        if (jInst.getArg1() == ALL_HIGH) {
                            inst = new TwoWordInstruction(OpCode.JOLZ, jInst.getArg1(), ALL_HIGH, ALL_HIGH, jInst.getWord1(), address, jInst.toString() + " CONV");
                        } else {
                            inst = new OneWordInstruction(OpCode.JOLZ, jInst.getArg1(), ALL_HIGH, ALL_LOW, address, jInst.toString() + " CONV");
                        }
                        break;
                    case JSZ:
                        if (jInst.getArg1() == ALL_HIGH) {
                            inst = new TwoWordInstruction(OpCode.JOSZ, jInst.getArg1(), ALL_HIGH, ALL_HIGH, jInst.getWord1(), address, jInst.toString() + " CONV");
                        } else {
                            inst = new OneWordInstruction(OpCode.JOSZ, jInst.getArg1(), ALL_HIGH, ALL_LOW, address, jInst.toString() + " CONV");
                        }
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            Word32[] iWords = inst.getWords();
            System.out.println(String.format("%20s  :  %-30s", inst.toString(), Word32.arrayToString(iWords)));
            Collections.addAll(words, iWords);
        }
        return words.toArray(new Word32[words.size()]);
    }

    private static byte parseArgument(String s) {
        s = s.trim();
        byte b;
        if (s.startsWith("R")) {
            b = Byte.valueOf(s.substring(1));
        } else {
            b = ALL_HIGH;
        }
        return b;
    }

    private static Word32 parseWord(String s) {
        try {
            return Word32.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Instruction[] i = parse("PUT 32 R9\n" +
                    "MOV R9 R0\n" +
                    "PUSH 0x1\n" +
                    "PUSH 0x1\n" +
                    "_LOOP POP R1\n" +
                    "POP R2\n" +
                    "ADD R1 R2 R3\n" +
                    "SUB R9 R0 R4\n" +
                    "SAVE R4 R3\n" +
                    "PUSH R2\n" +
                    "PUSH R1\n" +
                    "PUSH R3\n" +
                    "SUB R0 0x1 R0\n" +
                    "JNZ R0 LOOP");
            Word32[] w = assemble(i);
            System.out.println(Arrays.toString(i));
            System.out.println(Word32.arrayToString(w));
        } catch (AssemblyException e) {
            e.printStackTrace();
        }
    }
}
