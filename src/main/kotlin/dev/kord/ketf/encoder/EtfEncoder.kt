package dev.kord.ketf.encoder

import dev.kord.ketf.EtfTag
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import java.io.OutputStream
import kotlin.time.Duration

fun SerialDescriptor.isMapLike() = when(kind) {
    StructureKind.CLASS,
    StructureKind.MAP,
    StructureKind.OBJECT
    -> true
    else -> false
}

internal inline fun OutputStream.writeByte(byte: Byte) = write(byte.toInt())

internal inline fun OutputStream.writeAtom(content: String) {
    val utf8 = content.toUtf8Bytes()
    if (utf8.size <= 255) {
        writeByte(EtfTag.SMALL_ATOM_UTF8_EXT)
        write(utf8.size)
    } else {
        writeByte(EtfTag.ATOM_UTF8_EXT)
        writeInt(utf8.size)
    }
    write(utf8)
}

internal inline fun OutputStream.writeShort(short: Int) {
    write(short ushr 8)
    write(short)
}

internal inline fun OutputStream.writeInt(int: Int) {
    write(int ushr 24)
    write(int ushr 16)
    write(int ushr 8)
    write(int)
}

internal inline fun OutputStream.writeLong(long: Long) {
    writeByte((long ushr 56).toByte())
    writeByte((long ushr 48).toByte())
    writeByte((long ushr 40).toByte())
    writeByte((long ushr 32).toByte())
    writeByte((long ushr 24).toByte())
    writeByte((long ushr 16).toByte())
    writeByte((long ushr 8).toByte())
    writeByte((long ushr 0).toByte())
}

class EtfEncoder(
    private val output: OutputStream,
    override val context: SerialModule = EmptyModule
) : CompositeEncoder, Encoder {

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
        vararg typeSerializers: KSerializer<*>
    ): CompositeEncoder {
        when (descriptor.kind) {
            StructureKind.LIST -> {
                output.writeByte(EtfTag.LIST_EXT)
                output.writeInt(collectionSize)
            }

            StructureKind.MAP,
            StructureKind.CLASS,
            StructureKind.OBJECT
            -> {
                output.writeByte(EtfTag.MAP_EXT)
                output.writeInt(collectionSize)
            }

            PolymorphicKind.SEALED,
            PolymorphicKind.OPEN
            -> TODO("SEALED and OPEN kinds are not supported yet")

            else -> TODO("kind not ${descriptor.kind::class.simpleName} not supported yet")
        }

        return this
    }

    override fun beginStructure(
        descriptor: SerialDescriptor,
        vararg typeSerializers: KSerializer<*>
    ): CompositeEncoder {
        when (descriptor.kind) {
            StructureKind.CLASS,
            StructureKind.OBJECT
            -> {
                output.writeByte(EtfTag.MAP_EXT)
                output.writeInt(descriptor.elementsCount)
            }

            PolymorphicKind.SEALED,
            PolymorphicKind.OPEN
            -> TODO("SEALED and OPEN kinds are not supported yet")

            else -> TODO("kind not ${descriptor.kind::class.simpleName} not supported yet")
        }

        return this
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeBoolean(value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeByte(value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeChar(value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeDouble(value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeFloat(value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeInt(value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeLong(value)
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value == null) {
            if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
            encodeNull()
        } else encodeSerializableElement(descriptor, index, serializer, value)
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        serializer.serialize(this, value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeShort(value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        encodeString(value)
    }

    override fun encodeUnitElement(descriptor: SerialDescriptor, index: Int) {
        if (descriptor.isMapLike()) output.writeAtom(descriptor.getElementName(index))
        TODO("How do you encode a unit?")
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (descriptor.kind == StructureKind.LIST) {
            output.writeByte(EtfTag.NIL_EXT)
        }
    }

    override fun encodeBoolean(value: Boolean) {
        output.writeAtom(value.toString())
    }

    override fun encodeByte(value: Byte) {
        output.writeByte(EtfTag.SMALL_INTEGER_EXT)
        output.writeByte(value)
    }

    override fun encodeChar(value: Char) {
        output.writeByte(EtfTag.SMALL_INTEGER_EXT)
        output.writeByte(value.toByte())
    }

    override fun encodeDouble(value: Double) {
        output.writeByte(EtfTag.NEW_FLOAT_EXT)
        output.writeLong(value.toBits())
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val name = enumDescriptor.getElementName(index)
        output.writeAtom(name)

    }

    override fun encodeFloat(value: Float) {
        output.writeByte(EtfTag.NEW_FLOAT_EXT)
        output.writeLong(value.toBits().toLong())
    }

    override fun encodeInt(value: Int) {
        output.writeByte(EtfTag.INTEGER_EXT)
        output.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        output.writeByte(EtfTag.SMALL_BIG_EXT)
        output.writeByte(8)
        output.writeByte(if (value > 0) 0 else 1)
        output.writeLong(value)
    }

    override fun encodeNull() {
        output.writeByte(EtfTag.NIL_EXT)
    }

    override fun encodeShort(value: Short) {
        output.writeByte(EtfTag.INTEGER_EXT)
        output.writeInt(value.toInt())
    }

    override fun encodeString(value: String) {
        output.writeByte(EtfTag.STRING_EXT)
        val array = value.toByteArray(Charsets.US_ASCII)
        output.writeShort(array.size)
        output.write(array)
    }

    override fun encodeUnit() {
        TODO("Imma be honest with you chief, I have no idea how to encode a unit")
    }

}
