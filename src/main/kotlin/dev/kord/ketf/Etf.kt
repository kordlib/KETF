package dev.kord.ketf

import dev.kord.ketf.decoder.EtfDecoder
import dev.kord.ketf.encoder.EtfEncoder
import dev.kord.ketf.encoder.writeAtom
import dev.kord.ketf.encoder.writeByte
import dev.kord.ketf.encoder.writeShort
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * @param initialDecodeBufferSize The initial buffer size in bytes used for decoding.
 */
data class EtfConfig(
    val initialDecodeBufferSize: Int = 2000,
    val keyEncoder: KeyEncoder = KeyEncoder.AsString
) {
    sealed class KeyEncoder {
        abstract fun OutputStream.encode(key: String)

        object AsAtom: KeyEncoder() {
            override fun OutputStream.encode(key: String) = writeAtom(key)
        }

        object AsString: KeyEncoder() {
            override fun OutputStream.encode(key: String) {
                writeByte(EtfTag.STRING_EXT)
                val array = key.toByteArray(Charsets.US_ASCII)
                writeShort(array.size)
                write(array)
            }
        }

    }

    companion object {
        val default = EtfConfig()
    }
}

class Etf(
    override val serializersModule: SerializersModule = EmptySerializersModule,
    private val config: EtfConfig = EtfConfig.default
) : BinaryFormat {

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val outputStream = ByteArrayOutputStream(config.initialDecodeBufferSize)
        outputStream.write(131)
        EtfEncoder(outputStream).encodeSerializableValue(serializer, value)
        return outputStream.toByteArray()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.get() //skip version, I'm sure this is fine.
        return EtfDecoder(buffer).decodeSerializableValue(deserializer)
    }

    companion object : BinaryFormat {
        private val default = Etf()

        override val serializersModule: SerializersModule
            get() = EmptySerializersModule

        override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
            default.decodeFromByteArray(deserializer, bytes)

        override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray =
            default.encodeToByteArray(serializer, value)
    }

}
