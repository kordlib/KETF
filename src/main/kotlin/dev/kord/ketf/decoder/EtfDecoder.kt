package dev.kord.ketf.decoder

import dev.kord.ketf.EtfTag
import kotlinx.serialization.*
import kotlinx.serialization.builtins.AbstractDecoder
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalUnsignedTypes::class)
open class EtfDecoder(
    protected val buffer: ByteBuffer,
    override val context: SerialModule = EmptyModule
) : AbstractDecoder() {

    override val updateMode: UpdateMode
        get() = UpdateMode.BANNED

    protected fun peekTag(): Byte = buffer.get(buffer.position())

    protected fun nextTag():Byte = buffer.get()

    protected fun nextTag(tag: Byte): Byte {
        val nextTag = nextTag()
        if (nextTag != tag) {
            throw SerializationException("expected ${EtfTag.nameOf(tag)} but got ${EtfTag.nameOf(nextTag)}")
        }
        return nextTag
    }

    protected fun getUtf8String(numberOfBytes: Int): String {
        val bytes = ByteArray(numberOfBytes)
        buffer.get(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    protected fun getLatin1String(numberOfBytes: Int): String {
        val bytes = ByteArray(numberOfBytes)
        buffer.get(bytes)
        return String(bytes, StandardCharsets.ISO_8859_1)
    }

    override fun decodeByte(): Byte {
        nextTag(EtfTag.SMALL_INTEGER_EXT)

        return buffer.get()
    }

    override fun decodeBoolean(): Boolean {
        nextTag(EtfTag.SMALL_ATOM_UTF8_EXT)

        val length = buffer.get().toInt()
        val value = getUtf8String(length)

        return when {
            value.equals("true", true) -> true
            value.equals("false", false) -> false
            else -> throw SerializationException("expected boolean atom but got atom with content '$value' instead")
        }
    }

    override fun decodeChar(): Char {
        nextTag(EtfTag.SMALL_INTEGER_EXT)

        return buffer.get().toChar()
    }

    override fun decodeDouble(): Double {
        nextTag(EtfTag.NEW_FLOAT_EXT)

        return buffer.double
    }

    override fun decodeFloat(): Float {
        nextTag(EtfTag.NEW_FLOAT_EXT)

        return buffer.double.toFloat()
    }

    override fun decodeInt(): Int = when (val tag = nextTag()) {
        EtfTag.SMALL_INTEGER_EXT -> buffer.get().toInt()
        EtfTag.INTEGER_EXT -> buffer.int
        else -> throw SerializationException(
            "expected int-like tag but got ${EtfTag.nameOf(tag)}"
        )
    }

    private fun decodeSmallLong(): Long {
        val size = buffer.get().toInt()
        val sign = buffer.get().toInt()

        if (size > 8) throw SerializationException("Cannot decode number of $size bytes into long")

        val unsigned = when (size) { //optimize for existing data size
            1 -> buffer.get().toLong()
            2 -> buffer.short.toLong()
            4 -> buffer.int.toLong()
            8 -> buffer.long
            else -> {
                var unsigned = 0L
                repeat(size) {
                    unsigned = (unsigned shl 8) + buffer.get().toInt()
                }

                return unsigned
            }
        }

        return if (sign == 0) +unsigned
        else -unsigned
    }

    private fun decodeBigLong(): Long {
        val size = buffer.int
        val sign = buffer.get().toInt()

        if (size > 8) throw SerializationException("Cannot decode number of $size bytes into long")

        val unsigned = when (size) { //optimize for existing data size
            1 -> buffer.get().toLong()
            2 -> buffer.short.toLong()
            4 -> buffer.int.toLong()
            8 -> buffer.long
            else -> {
                var unsigned = 0L
                repeat(size) {
                    unsigned = (unsigned shl 8) + buffer.get().toInt()
                }

                return unsigned
            }
        }

        return if (sign == 0) +unsigned
        else -unsigned
    }

    override fun decodeLong(): Long = when (val tag = nextTag()) {
        EtfTag.SMALL_INTEGER_EXT -> buffer.get().toLong()
        EtfTag.INTEGER_EXT -> buffer.int.toLong()
        EtfTag.SMALL_BIG_EXT -> decodeSmallLong()
        EtfTag.LARGE_BIG_EXT -> decodeBigLong()
        else -> throw SerializationException(
            "expected long-like tag but got ${EtfTag.nameOf(tag)}"
        )
    }

    override fun decodeNotNullMark(): Boolean = peekTag() != EtfTag.NIL_EXT

    override fun decodeNull(): Nothing? {
        nextTag(EtfTag.NIL_EXT)
        return null
    }

    override fun decodeShort(): Short = decodeInt().toShort()

    override fun decodeUnit() {
        nextTag(EtfTag.SMALL_ATOM_UTF8_EXT)

        val length = buffer.get().toInt()
        val value = getUtf8String(length)

        if (!value.equals("Unit", true)) {
            throw SerializationException("Expected unit but got atom of '$value'")
        }
    }

    private fun decodeStringExt(): String {
        val length = buffer.short.toInt()
        val bytes = ByteArray(length)
        buffer.get(bytes)
        return String(bytes, StandardCharsets.US_ASCII)
    }

    private fun decodeListAsString(): String {
        TODO("string lists are currently not supported")
    }

    override fun decodeString(): String {
        return when (val tag = nextTag()) {
            EtfTag.STRING_EXT -> decodeStringExt()
            EtfTag.LIST_EXT -> decodeListAsString()
            else -> throw SerializationException(
                "expected string-like tag but got ${EtfTag.nameOf(tag)}"
            )
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        return when (descriptor.kind) {
            PrimitiveKind.BOOLEAN,
            PrimitiveKind.BYTE,
            PrimitiveKind.CHAR,
            PrimitiveKind.SHORT,
            PrimitiveKind.INT,
            PrimitiveKind.LONG,
            PrimitiveKind.FLOAT,
            PrimitiveKind.DOUBLE,
            PrimitiveKind.STRING,
            UnionKind.ENUM_KIND
            -> this
            StructureKind.LIST -> {
                val tag = nextTag()
                return when(tag) {
                    EtfTag.LIST_EXT -> {
                        val size = buffer.int
                        EtfListDecoder(buffer, context, size)
                    }
                    EtfTag.SMALL_TUPLE_EXT -> {
                        val size = buffer.get().toInt()
                        EtfTupleDecoder(buffer, context, size)
                    }
                    EtfTag.LARGE_TUPLE_EXT -> {
                        val size = buffer.int
                        EtfTupleDecoder(buffer, context, size)
                    }
                    else -> throw SerializationException("expected list-like tag but got ${tag}")
                }
            }
            StructureKind.MAP,
            StructureKind.CLASS,
            StructureKind.OBJECT
            -> {
                nextTag(EtfTag.MAP_EXT)
                EtfMapLikeDecoder(buffer, context, buffer.int)
            }
            PolymorphicKind.SEALED,
            PolymorphicKind.OPEN
            -> TODO("SEALED and OPEN are not yet supported")
            else -> throw SerializationException("unsupported SerialKind ${descriptor.kind::class.simpleName}")
        }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val tag = nextTag()

        val length = when (tag) {
            EtfTag.SMALL_ATOM_UTF8_EXT -> buffer.get().toInt()
            EtfTag.ATOM_UTF8_EXT -> buffer.int
            else -> throw SerializationException("enum expected as atom but got ${EtfTag.nameOf(tag)}")
        }

        val name = getUtf8String(length)
        return enumDescriptor.getElementIndexOrThrow(name)
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        TODO(""""This path shouldn't technically happen, but it seems like it did ¯\_(ツ)_/¯""")
    }

}
