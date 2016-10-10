package common

/**
  * @author Chris Gold
  * @version 1.0
  */
trait Util {
  def hex(input: Int) = "0x%08X" format Integer.valueOf(input)
  def hex(input: Int, width: Int) = "0x%0" + width + "X" format Integer.valueOf(input)
  def floatFromBits(input: Int) = java.lang.Float.intBitsToFloat(input)
  def bitsFromFloat(input: Float) = java.lang.Float.floatToIntBits(input)

  def bytesFromInt(input: Int) = Array((input & 0xFF000000) >> 24, (input & 0x00FF0000) >> 16, (input & 0x0000FF00) >> 8, (input & 0x000000FF) >> 0).map(x => x.toByte)
}
