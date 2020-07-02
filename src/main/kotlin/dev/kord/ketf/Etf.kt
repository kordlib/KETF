package dev.kord.ketf

import dev.kord.ketf.decoder.EtfDecoder
import dev.kord.ketf.encoder.EtfEncoder
import dev.kord.ketf.encoder.writeByte
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * @param initialDecodeBufferSize The initial buffer size in bytes used for decoding.
 */
data class EtfConfig(
    val initialDecodeBufferSize: Int = 2000
) {
    companion object {
        val default = EtfConfig()
    }
}

class Etf(
    override val context: SerialModule = EmptyModule,
    private val config: EtfConfig = EtfConfig.default
) : BinaryFormat {

    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val outputStream = ByteArrayOutputStream(config.initialDecodeBufferSize)
        outputStream.writeByte(131.toByte())
        EtfEncoder(outputStream).encode(serializer, value)
        return outputStream.toByteArray()
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.get() //skip version, I'm sure this is fine.
        return EtfDecoder(buffer).decode(deserializer)
    }

    companion object : BinaryFormat {
        private val default = Etf()

        override val context: SerialModule
            get() = EmptyModule

        override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray =
            default.dump(serializer, value)

        override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
            default.load(deserializer, bytes)

    }

}
