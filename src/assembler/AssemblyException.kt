package assembler

/**
 * @author Christian Goldapp
 * @version 1.0
 */
class AssemblyException(line: Int? = null, reason: String) :
        Exception(String.format("Line %s: %s", line ?: "?", reason))
