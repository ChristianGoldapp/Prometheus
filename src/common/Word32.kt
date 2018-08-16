package common

class Word32(rawValue: Int) {

    val value = rawValue

    fun div(that: Word32): Word32 = Word32(this.value / that.value)

    fun uadd(that: Word32): Word32 = add(that)

    fun add(that: Word32): Word32 = Word32(this.value + that.value)

    fun usub(that: Word32): Word32 = sub(that)

    fun sub(that: Word32): Word32 = Word32(this.value - that.value)

    fun umul(that: Word32): Word32 = mul(that)

    fun mul(that: Word32): Word32 = Word32(this.value * that.value)

    fun udiv(that: Word32): Word32 = Word32(Integer.divideUnsigned(this.value, that.value))

    fun fadd(that: Word32): Word32 = fop(that, { a, b -> a + b })

    fun fsub(that: Word32): Word32 = fop(that, { a, b -> a - b })

    fun fmul(that: Word32): Word32 = fop(that, { a, b -> a * b })

    fun fdiv(that: Word32): Word32 = fop(that, { a, b -> a / b })

    fun fop(that: Word32, op: FloatBinOp): Word32 = Word32(bitsFromFloat(op(this.floatValue(), that.floatValue())))

    fun floatValue() = floatFromBits(value)

    fun and(that: Word32): Word32 = Word32(this.value and that.value)

    fun or(that: Word32): Word32 = Word32(this.value or that.value)

    fun xor(that: Word32): Word32 = Word32(this.value xor that.value)

    fun not(): Word32 = Word32(this.value.inv())

    fun lshift(): Word32 = Word32(this.value shl 1)

    fun rshift(): Word32 = Word32(this.value ushr 1)

    fun ftoi(): Word32 = Word32(this.floatValue().toInt())

    fun itof(): Word32 = Word32(bitsFromFloat(floatValue()))

    fun itou(): Word32 = Word32(Math.abs(value))

    fun utoi(): Word32 = Word32(if (value < 0) value + Int.MAX_VALUE else value)

    fun bytes(): ByteArray = bytesFromInt(value)

    override fun toString() = "%s (%d)".format(hex(value), intValue())

    fun intValue() = value

    fun lineReport() = "%s %10d %20f %11d %s".format(hex(value), intValue(), floatValue(), uintValue(), String.format("%32s", Integer.toUnsignedString(value, 2)).replace(" ", "0"))

    fun uintValue() = Integer.toUnsignedLong(value)

    fun hexString() = hex(value)

    fun equals(that: Word32) = this.value == that.value

    companion object {
        @JvmStatic
        val ONES = Word32(0.inv())

        @JvmStatic
        val ZEROES = Word32(0)

        fun valueOf(s: String): Word32? {
            val trimmed: String = if (s.startsWith("0x")) s.substring(2) else s
            return trimmed.toLongOrNull(16)?.toInt()?.let { Word32(it) }
        }

        fun fromFloat(f: Float) = Word32(bitsFromFloat(f))
    }
}

typealias FloatBinOp = (Float, Float) -> Float