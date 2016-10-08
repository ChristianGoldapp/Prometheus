/**
  * @author Christian Goldapp
  * @version 1.0
  */
trait Util {
  def hex(input: Int) = "0x%08X" format Integer.valueOf(input)
  def hex(input: Int, width: Int) = "0x%0" + width + "X" format Integer.valueOf(input)
  def floatFromBits(input: Int) = java.lang.Float.intBitsToFloat(input)
  def bitsFromFloat(input: Float) = java.lang.Float.floatToIntBits(input)
}
