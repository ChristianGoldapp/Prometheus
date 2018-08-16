package common

fun Int.hex() = "0x%08X".format(this)

fun Int.hex(width: Int) = ("0x%0" + width + "X").format(this)

fun Int.floatFromBits() = java.lang.Float.intBitsToFloat(this)

fun Float.bits() = toRawBits()

fun Int.bytes(): ByteArray {
    return byteArrayOf((this shr 24).toByte(), (this shr 16).toByte(), (this shr 8).toByte(), this.toByte())
}

val ALL_LOW = 0x00.toByte()
val ALL_HIGH = 0xFF.toByte()

val DEFAULT_MEMSIZE = 2 shl 8
val DEFAULT_REGSIZE = 10

fun ByteArray.bytesToWords() = toList().chunked(4).map { chunk -> Word32.fromBytes(chunk.toByteArray()) }.toTypedArray()

fun wordsToBytes(words: Array<Word32>) = words.map { x -> x.bytes().toList() }

fun arrayToString(words: Iterable<Word32>): String = words.map { it.hexString() }.joinToString(" ", prefix = " ")

fun arrayToString(words: Array<Word32>): String = arrayToString(words.asIterable())

fun arrayToString(words: Iterable<Word32>, row: Int): String = words.toList().chunked(row).map { arrayToString(it) }.joinToString(separator = System.lineSeparator())

fun arrayToString(words: Array<Word32>, row: Int): String = arrayToString(words.asIterable(), row)