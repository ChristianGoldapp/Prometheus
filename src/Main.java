import assembler.Assembler;
import assembler.AssemblyException;
import common.Word32;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Chris Gold
 * @version 1.0
 */
class Main {
    public static void main(String[] args) throws AssemblyException, IOException {
        List<String> lines = Files.readAllLines(Paths.get(args[0]), Charset.defaultCharset());
        Word32[] program = Assembler.assemble(Assembler.parse(lines.toArray(new String[lines.size()])));
        Processor p = new Processor();
        p.setProgram(program);
        p.run();
        System.out.println(p);
    }
}
