import assembler.Assembler;
import assembler.AssemblyException;
import common.Word32;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Chris Gold
 * @version 1.0
 */
class Main {
    public static void main(String[] args) throws AssemblyException, IOException {
        assembleAndExecute(new File(args[0]));
    }

    private static Word32[] readWordsFromFile(File f) throws IOException {
        byte[] b = Files.readAllBytes(f.toPath());
        return Word32.bytesToWords(b);
    }

    private static void execute(File f) throws IOException {
        Word32[] program = readWordsFromFile(f);
        execute(program);
    }

    private static void assembleToFile(File f) throws IOException, AssemblyException {
        String assembly = f.getName().split(".")[0] + ".pro";
        List<String> lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
        Word32[] program = Assembler.assemble(Assembler.parse(lines.toArray(new String[lines.size()])));
    }

    private static void assembleAndExecute(File f) throws IOException, AssemblyException {
        List<String> lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
        Word32[] program = Assembler.assemble(Assembler.parse(lines.toArray(new String[lines.size()])));
        execute(program);
    }

    private static void execute(Word32[] program) {
        Processor p = new Processor();
        p.setProgram(program);
        p.run();
        System.out.println(p);
    }
}
