package dev.kord.ketf.decoder

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import java.nio.ByteBuffer

/**
 * Tuples are basically lists without an NIL at the end.
 */
class EtfTupleDecoder(
    buffer: ByteBuffer,
    serializersModule: SerializersModule,
    private val listSize: Int
) : EtfDecoder(buffer, serializersModule) {
    private var currentIndex = -1

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = listSize

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1

        return if (currentIndex == listSize) CompositeDecoder.DECODE_DONE
        else currentIndex
    }

}
