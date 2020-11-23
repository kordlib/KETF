package dev.kord.ketf.encoder

import dev.kord.ketf.Etf
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import org.junit.Test
import kotlin.math.exp
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter

fun byteArrayOf(vararg bytes: Int) = bytes.map { it.toByte() }.toByteArray()

fun assertEquals(expected: ByteArray, actual: ByteArray) = assert(expected.contentEquals(actual)) {
    """
        expected ${expected.contentToString()} but was ${actual.contentToString()}
        ${expected.contentToString()}
        ${actual.contentToString()}
    """.trimIndent()
}

internal class EncoderTest {
    lateinit var format: BinaryFormat

    @BeforeTest
    fun setup() {
        format = Etf
    }

    @Test
    fun `encode simple empty class`() {
        @Serializable
        class Item

        val result = format.encodeToByteArray(Item())
        val expect = byteArrayOf(131, 116, 0, 0, 0, 0)

        assertEquals(expect, result)
    }

    @Test
    fun `encode simple list`() {
        @Serializable
        class Item(val list: List<Int>)

        val result = format.encodeToByteArray(Item(listOf(5)))
        val expect = byteArrayOf(
            131,
            116, 0, 0, 0, 1, //map 1 item
            119, 4, 108, 105, 115, 116, //atom 'list'
            108, 0, 0, 0, 1, //list 1 item
            98, 0, 0, 0, 5, //int '5'
            106 //NIL list end
        )

        assertEquals(expect, result)
    }

}