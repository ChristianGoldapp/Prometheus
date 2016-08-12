/**
  * @author Christian Goldapp
  * @version 1.0
  */
class Word32(rawValue: Int) extends Util{
  val value = rawValue
  def intValue = value
  def uintValue = Integer.toUnsignedLong(value)
  def floatValue = floatFromBits(value)

  def add(that: Word32) : Word32 = new Word32(this.value + that.value)
  def sub(that: Word32) : Word32 = new Word32(this.value - that.value)
  def mul(that: Word32) : Word32 = new Word32(this.value * that.value)
  def div(that: Word32) : Word32 = new Word32(this.value / that.value)

  def uadd(that: Word32) : Word32 = add(that)
  def usub(that: Word32) : Word32 = sub(that)
  def umul(that: Word32) : Word32 = mul(that)
  def udiv(that: Word32) : Word32 = new Word32(Integer.divideUnsigned(this.value, that.value))

  def fadd(that: Word32) : Word32 = fop(that, a => b => a+b)
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
  def itou() : Word32 = new Word32(Math.abs(value))
  def utoi() : Word32 = new Word32(if(value < 0) value + Int.MaxValue else value)

  type FloatBinOp = Float => Float => Float
  private def fop(that: Word32, op: FloatBinOp): Word32 = new Word32(bitsFromFloat(op(this.floatValue)(that.floatValue)))
  override def toString = hex(value)
}
