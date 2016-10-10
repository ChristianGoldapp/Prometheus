package assembler;

import data.Word32;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chris Gold
 * @version 1.0
 */
public class Assembler {
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
            if (OpCode.jumps.contains(op)) {
                String lbl;
                switch (op) {
                    case JMP:
                        lbl = tokens[0];
                        instructions[i] = new JumpInstruction(op, (byte) 0x0, (byte) 0x0, lbl, new Word32(0), new Word32(0), line);
                        break;
                    case JEQ:
                        lbl = tokens[2];
                        instructions[i] = new JumpInstruction(op, parseArgument(tokens[0]), parseArgument(tokens[1]), lbl, parseWord(tokens[0]), parseWord(tokens[1]), line);
                        break;
                    default:
                        lbl = tokens[1];
                        instructions[i] = new JumpInstruction(op, parseArgument(tokens[0]), (byte) 0x00, lbl, parseWord(tokens[0]), null, line);
                }
                if (!labels.containsKey(lbl)) {
                    throw new AssemblyException(i, String.format("Encountered unknown label: %s", lbl));
                }
            } else if (OpCode.literals.contains(op)) {
                switch (op) {
                    case PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, (byte) 0xFF, parseArgument(tokens[1]), (byte) 0, Word32.valueOf(tokens[0]), line);
                        break;
                    case F_PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, (byte) 0xFF, parseArgument(tokens[1]), (byte) 0, Word32.fromFloat(Float.valueOf(tokens[0])), line);
                        break;
                    case U_PUT:
                        instructions[i] = new OneWordInstruction(OpCode.MOV, (byte) 0xFF, parseArgument(tokens[1]), (byte) 0, Word32.valueOf(tokens[0]), line);
                        break;
                }
            } else {
                byte arg1 = 0;
                byte arg2 = 0;
                byte arg3 = 0;
                List<Word32> words = new ArrayList<>();
                //Use fallthrough for easier recognition of arg count
                switch (tokens.length) {
                    case 3:
                        arg3 = parseArgument(tokens[2]);
                    case 2:
                        arg2 = parseArgument(tokens[1]);
                        if (arg2 == (byte) 0xFF) {
                            words.add(parseWord(tokens[1]));
                        }
                    case 1:
                        arg1 = parseArgument(tokens[0]);
                        if (arg1 == (byte) 0xFF) {
                            words.add(parseWord(tokens[0]));
                        }

                }
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
                    default:
                        throw new RuntimeException();
                }
            }
        }
        return instructions;
    }

    public static byte parseArgument(String s) {
        s = s.trim();
        byte b;
        if (s.startsWith("R")) {
            b = Byte.valueOf(s.substring(1));
        } else {
            b = (byte) 0xFF;
        }
        return b;
    }

    public static Word32 parseWord(String s) {
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
            System.out.println(Arrays.toString(i));
        } catch (AssemblyException e) {
            e.printStackTrace();
        }
    }
}
