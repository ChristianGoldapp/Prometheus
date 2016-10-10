package assembler;

/**
 * @author Christian Goldapp
 * @version 1.0
 */
public class AssemblyException extends Exception {
    public AssemblyException(String reason) {
        super(reason);
    }

    public AssemblyException(int line, String reason) {
        super(String.format("Line %d: %s", line, reason));
    }
}
