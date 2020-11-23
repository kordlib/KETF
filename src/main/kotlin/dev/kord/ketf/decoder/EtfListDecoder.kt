package dev.kord.ketf.decoder

import dev.kord.ketf.EtfTag
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import java.nio.ByteBuffer

class EtfListDecoder(
    buffer: ByteBuffer,
    serializersModule: SerializersModule,
    private val listSize: Int
) : EtfDecoder(buffer, serializersModule) {

    private var currentIndex = -1

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = listSize

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        println(currentIndex)

        if (currentIndex < listSize) return currentIndex

        //if we're already past the nil tag, done
        if (currentIndex > listSize) return CompositeDecoder.DECODE_DONE

        //if the etf list is not proper the tail is a value
        if (peekTag() != EtfTag.NIL_EXT) return currentIndex

        //if the tail is nil, the list could still not be proper,
        //check if the descriptor accepts nulls (who uses a list of nullables?!).
        //If it does, we'll provide the nil tail as a value.
        val nullable = descriptor.getElementDescriptor(0).isNullable
        return if (nullable) currentIndex
        else CompositeDecoder.DECODE_DONE
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (currentIndex == -1) { //used the sequential decoder
            nextTag(EtfTag.NIL_EXT)//popping NIL in proper list
        }

        if (currentIndex == listSize) { //after manual decoder
            nextTag(EtfTag.NIL_EXT) //popping NIL in proper list
        }
    }

}
