package interpreter

/**
  * @author Christian Goldapp
  * @version 1.0
  */
object Main {
  def main(args: Array[String]): Unit = {
    val p: Processor = new Processor
    p.loadProgram("_LOOP ADD R0 0x1 R0\nPUSH R0\nSUB 0xA R0 R1\nJNZ R1 LOOP")
    p.start()
    println(p.toString)
  }
}
