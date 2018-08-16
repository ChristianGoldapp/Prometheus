package common

import java.nio.ByteBuffer

fun hex(input: Int) = "0x%08X".format(Integer.valueOf(input))

fun hex(input: Int, width: Int) = ("0x%0" + width + "X").format(Integer.valueOf(input))

fun floatFromBits(input: Int) = java.lang.Float.intBitsToFloat(input)

fun bitsFromFloat(input: Float) = java.lang.Float.floatToIntBits(input)

fun bytesFromInt(value: Int): ByteArray {
    return byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())
}

val ALL_LOW = 0x00.toByte()
val ALL_HIGH = 0xFF.toByte()

val DEFAULT_MEMSIZE = 2 shl 8
val DEFAULT_REGSIZE = 10

fun bytesToWords(bytes: ByteArray) = bytes.toList().chunked(4).map { chunk -> fromBytes(chunk.toByteArray()) }.toTypedArray()

fun fromBytes(bytes: ByteArray) = Word32(ByteBuffer.wrap(bytes).getInt())

fun wordsToBytes(words: Array<Word32>) = words.map { x -> x.bytes().toList() }

fun arrayToString(words: Iterable<Word32>): String = words.map { it.hexString() }.joinToString(" ", prefix = " ")

fun arrayToString(words: Array<Word32>): String = arrayToString(words.asIterable())

fun arrayToString(words: Iterable<Word32>, row: Int): String = words.toList().chunked(row).map { arrayToString(it) }.joinToString(separator = System.lineSeparator())

fun arrayToString(words: Array<Word32>, row: Int): String = arrayToString(words.asIterable(), row)