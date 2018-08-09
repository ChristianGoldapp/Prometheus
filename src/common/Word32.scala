package common

import java.nio.ByteBuffer

/**
  * @author Chris Gold
  * @version 1.0
  */
class Word32(rawValue: Int) extends Util {
  type FloatBinOp = Float => Float => Float
  val value = rawValue

  def div(that: Word32): Word32 = new Word32(this.value / that.value)

  def uadd(that: Word32): Word32 = add(that)

  def add(that: Word32): Word32 = new Word32(this.value + that.value)

  def usub(that: Word32): Word32 = sub(that)

  def sub(that: Word32): Word32 = new Word32(this.value - that.value)

  def umul(that: Word32): Word32 = mul(that)

  def mul(that: Word32): Word32 = new Word32(this.value * that.value)

  def udiv(that: Word32): Word32 = new Word32(Integer.divideUnsigned(this.value, that.value))

  def fadd(that: Word32): Word32 = fop(that, a => b => a + b)

  def fsub(that: Word32): Word32 = fop(that, a => b => a - b)

  def fmul(that: Word32): Word32 = fop(that, a => b => a * b)

  def fdiv(that: Word32): Word32 = fop(that, a => b => a / b)

  private def fop(that: Word32, op: FloatBinOp): Word32 = new Word32(bitsFromFloat(op(this.floatValue)(that.floatValue)))

  def floatValue = floatFromBits(value)

  def and(that: Word32): Word32 = new Word32(this.value & that.value)

  def or(that: Word32): Word32 = new Word32(this.value | that.value)

  def xor(that: Word32): Word32 = new Word32(this.value ^ that.value)

  def not: Word32 = new Word32(~this.value)

  def lshift: Word32 = new Word32(this.value << 1)

  def rshift: Word32 = new Word32(this.value >> 1)

  def ftoi(): Word32 = new Word32(this.floatValue.toInt)

  def itof(): Word32 = new Word32(bitsFromFloat(floatValue))

  def itou(): Word32 = new Word32(Math.abs(value))

  def utoi(): Word32 = new Word32(if (value < 0) value + Int.MaxValue else value)

  def bytes(): Array[Byte] = bytesFromInt(value)

  override def toString = "%s (%d)".format(hex(value), intValue)

  def intValue = value

  def lineReport = "%s %10d %20f %11d %s".format(hex(value), intValue, floatValue, uintValue, String.format("%32s", Integer.toUnsignedString(value, 2)).replace(" ", "0"))

  def uintValue = Integer.toUnsignedLong(value)

  def hexString = hex(value)

  def equals(that: Word32) = this.value == that.value
}

object Word32 extends Util {
  def bytesToWords(bytes: Array[Byte]) = bytes.grouped(4).map(x => fromBytes(x)).toArray

  def fromBytes(bytes: Array[Byte]) = new Word32(ByteBuffer.wrap(bytes).getInt)

  def wordsToBytes(words: Array[Word32]) = words.flatMap(x => x.bytes().toList)

  def valueOf(s: String): Word32 = {
    if (s.startsWith("0x")) new Word32(Integer.parseUnsignedInt(s.substring(2), 16))
    else new Word32(Integer.valueOf(s))
  }

  def fromFloat(f: Float) = new Word32(bitsFromFloat(f))

  def arrayToString(words: Array[Word32]): String = {
    val sb: StringBuilder = new StringBuilder
    words.foreach(x => sb.append(" ").append(x.hexString))
    sb.toString
  }

  def arrayToString(words: Array[Word32], row: Int): String = {
    val sb: StringBuilder = new StringBuilder
    words.grouped(row).foreach(x => {
      x.foreach(x => sb.append(" ").append(x.hexString))
      sb.append("\n")
    })
    sb.toString
  }
}
