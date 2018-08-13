package common

fun hex(input: Int) = "0x%08X".format(Integer.valueOf(input))
fun hex(input: Int, width: Int) = ("0x%0" + width + "X").format(Integer.valueOf(input))
fun floatFromBits(input: Int) = java.lang.Float.intBitsToFloat(input)
fun bitsFromFloat(input: Float) = java.lang.Float.floatToIntBits(input)

fun bytesFromInt(value: Int): ByteArray {
    return byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())
}

val ALL_LOW = 0x00.toByte()
val ALL_HIGH = 0xFF.toByte()