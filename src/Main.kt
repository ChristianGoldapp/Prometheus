import assembler.Assembler
import assembler.AssemblyException
import common.Word32

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files

/**
 * @author Chris Gold
 * @version 1.0
 */
internal object Main {
    @Throws(AssemblyException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        assembleAndExecute(File(args[0]))
    }

    @Throws(IOException::class)
    private fun readWordsFromFile(f: File): Array<Word32> {
        val b = Files.readAllBytes(f.toPath())
        return Word32.bytesToWords(b)
    }

    @Throws(IOException::class)
    private fun execute(f: File) {
        val program = readWordsFromFile(f)
        execute(program)
    }

    @Throws(IOException::class, AssemblyException::class)
    private fun assembleToFile(f: File) {
        val assembly = f.name.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + ".pro"
        val lines = Files.readAllLines(f.toPath(), Charset.defaultCharset())
        val program = Assembler.assemble(Assembler.parse(lines.toTypedArray()))
    }

    @Throws(IOException::class, AssemblyException::class)
    private fun assembleAndExecute(f: File) {
        val lines = Files.readAllLines(f.toPath(), Charset.defaultCharset())
        val program = Assembler.assemble(Assembler.parse(lines.toTypedArray()))
        execute(program)
    }

    private fun execute(program: Array<Word32>) {
        val p = Processor()
        p.setProgram(program)
        p.run()
        println(p)
    }
}
