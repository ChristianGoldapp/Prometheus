package data

import java.nio.ByteBuffer

import common.Util

/**
  * @author Chris Gold
  * @version 1.0
  */
class Word32(rawValue: Int) extends Util{
  type FloatBinOp = Float => Float => Float
  val value = rawValue

  def div(that: Word32) : Word32 = new Word32(this.value / that.value)

  def uadd(that: Word32) : Word32 = add(that)

  def add(that: Word32): Word32 = new Word32(this.value + that.value)

  def usub(that: Word32) : Word32 = sub(that)

  def sub(that: Word32): Word32 = new Word32(this.value - that.value)

  def umul(that: Word32) : Word32 = mul(that)

  def mul(that: Word32): Word32 = new Word32(this.value * that.value)

  def udiv(that: Word32) : Word32 = new Word32(Integer.divideUnsigned(this.value, that.value))

  def fadd(that: Word32) : Word32 = fop(that, a => b => a+b)

  private def fop(that: Word32, op: FloatBinOp): Word32 = new Word32(bitsFromFloat(op(this.floatValue)(that.floatValue)))

  def fsub(that: Word32) : Word32 = fop(that, a => b => a-b)

  def fmul(that: Word32) : Word32 = fop(that, a => b => a*b)

  def fdiv(that: Word32) : Word32 = fop(that, a => b => a/b)

  def and(that: Word32) : Word32 = new Word32(this.value & that.value)

  def or(that: Word32) : Word32 = new Word32(this.value | that.value)

  def xor(that: Word32) : Word32 = new Word32(this.value ^ that.value)

  def not : Word32 = new Word32(~this.value)

  def lshift : Word32 = new Word32(this.value << 1)

  def rshift : Word32 = new Word32(this.value >> 1)

  def ftoi() : Word32 = new Word32(this.floatValue.toInt)

  def itof() : Word32 = new Word32(bitsFromFloat(floatValue))

  def floatValue = floatFromBits(value)

  def itou() : Word32 = new Word32(Math.abs(value))

  def utoi() : Word32 = new Word32(if(value < 0) value + Int.MaxValue else value)

  def bytes(): Array[Byte] = bytesFromInt(value)

  override def toString = "%s %10d %20f %11d %s".format(hex(value), intValue, floatValue, uintValue, String.format("%32s", Integer.toUnsignedString(value, 2)).replace(" ", "0")
  )

  def intValue = value

  def uintValue = Integer.toUnsignedLong(value)
}

object Word32 extends Util {
  def fromBytes(bytes: Array[Byte]) = new Word32(ByteBuffer.wrap(bytes).getInt)

  def valueOf(s: String): Word32 = {
    if (s.startsWith("0x")) new Word32(Integer.parseUnsignedInt(s.substring(2)))
    else new Word32(Integer.valueOf(s))
  }

  def fromFloat(f: Float) = new Word32(bitsFromFloat(f))
}
