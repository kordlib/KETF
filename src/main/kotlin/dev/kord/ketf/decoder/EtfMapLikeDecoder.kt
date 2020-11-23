package dev.kord.ketf.decoder

import dev.kord.ketf.EtfTag
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import java.nio.ByteBuffer

class EtfMapLikeDecoder(
    buffer: ByteBuffer,
    serializersModule: SerializersModule,
    private val pairSize: Int
) : EtfDecoder(buffer, serializersModule) {
    private var currentIndex = -1

    override fun decodeSequentially(): Boolean = false

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = pairSize

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1

        if (currentIndex == pairSize) return CompositeDecoder.DECODE_DONE

        val name = when (val tag = nextTag()) {
            EtfTag.ATOM_UTF8_EXT -> getUtf8String(buffer.int)
            EtfTag.SMALL_ATOM_UTF8_EXT -> getUtf8String( buffer.get().toInt())
            EtfTag.ATOM_EXT -> getLatin1String(buffer.short.toInt())
            EtfTag.SMALL_ATOM_EXT -> getLatin1String(buffer.get().toInt())
            else -> throw SerializationException("${descriptor.serialName}: expected key as atom for but got ${EtfTag.nameOf(tag)}")
        }

        return descriptor.getElementIndex(name)
    }

}
