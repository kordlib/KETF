package dev.kord.ketf.decoder

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.modules.SerialModule
import java.nio.ByteBuffer

/**
 * Tuples are basically lists without an NIL at the end.
 */
class EtfTupleDecoder(
    buffer: ByteBuffer,
    context: SerialModule,
    private val listSize: Int
) : EtfDecoder(buffer, context) {
    override val context: SerialModule
        get() = super.context
    override val updateMode: UpdateMode
        get() = super.updateMode

    private var currentIndex = -1

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = listSize

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1

        return if (currentIndex == listSize) CompositeDecoder.READ_DONE
        else currentIndex
    }

}
