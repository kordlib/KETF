package dev.kord.ketf

object EtfTag {
    const val ATOM_CACHE_REF_EXT: Byte = 82

    const val SMALL_INTEGER_EXT: Byte = 97
    const val INTEGER_EXT: Byte = 98

    @Deprecated("This term is used in minor version 0 of the external format; it has been superseded by NewFloatExt")
    const val FLOAT_EXT: Byte = 99

    const val PORT_EXT: Byte = 102
    const val NEW_PORT_EXT: Byte = 89

    const val PID_EXT: Byte = 103
    const val NEW_PID_EXT: Byte = 88

    const val SMALL_TUPLE_EXT: Byte = 104
    const val LARGE_TUPLE_EXT: Byte = 105

    const val MAP_EXT: Byte = 116

    const val NIL_EXT: Byte = 106

    const val STRING_EXT: Byte = 107

    const val LIST_EXT: Byte = 108

    const val BINARY_EXT: Byte = 109

    const val SMALL_BIG_EXT: Byte = 110
    const val LARGE_BIG_EXT: Byte = 110

    @Deprecated("Use NEWER_REFERENCE_EXT instead.")
    const val REFERENCE_EXT: Byte = 111

    @Deprecated("Use NEWER_REFERENCE_EXT instead.")
    const val NEW_REFERENCE_EXT: Byte = 101
    const val NEWER_REFERENCE_EXT: Byte = 90

    const val FUN_EXT: Byte = 117
    const val NEW_FUN_EXT: Byte = 112

    const val EXPORT_EXT: Byte = 113

    const val BIT_BINARY_EXT: Byte = 77

    const val NEW_FLOAT_EXT: Byte = 70

    const val ATOM_UTF8_EXT: Byte = 118
    const val SMALL_ATOM_UTF8_EXT: Byte = 119


    @Deprecated("Deprecated in favor of the UTF8 variant")
    const val ATOM_EXT: Byte = 100

    @Deprecated("Deprecated in favor of the UTF8 variant")
    const val SMALL_ATOM_EXT: Byte = 115

    fun nameOf(byte: Byte): String = when(byte) {
        ATOM_CACHE_REF_EXT -> "ATOM_CACHE_REF_EXT(${ATOM_CACHE_REF_EXT.toUByte()})"
        SMALL_INTEGER_EXT -> "SMALL_INTEGER_EXT(${SMALL_INTEGER_EXT.toUByte()})"
        INTEGER_EXT -> "INTEGER_EXT(${INTEGER_EXT.toUByte()})"
        FLOAT_EXT -> "FLOAT_EXT(${FLOAT_EXT.toUByte()})"
        PORT_EXT -> "PORT_EXT(${PORT_EXT.toUByte()}"
        NEW_PORT_EXT -> "NEW_PORT_EXT(${NEW_PORT_EXT.toUByte()})"
        PID_EXT -> "PID_EXT(${PID_EXT.toUByte()})"
        NEW_PID_EXT -> "NEW_PID_EXT(${NEW_PID_EXT.toUByte()})"
        SMALL_TUPLE_EXT -> "SMALL_TUPLE_EXT(${SMALL_TUPLE_EXT.toUByte()})"
        LARGE_TUPLE_EXT -> "LARGE_TUPLE_EXT(${LARGE_TUPLE_EXT.toUByte()})"
        MAP_EXT -> "MAP_EXT(${MAP_EXT.toUByte()})"
        NIL_EXT -> "NIL_EXT(${NIL_EXT.toUByte()})"
        STRING_EXT -> "STRING_EXT(${STRING_EXT.toUByte()})"
        LIST_EXT -> "LIST_EXT(${LIST_EXT.toUByte()})"
        BINARY_EXT -> "BINARY_EXT(${ BINARY_EXT.toUByte()})"
        SMALL_BIG_EXT -> "SMALL_BIG_EXT(${SMALL_BIG_EXT.toUByte()})"
        LARGE_BIG_EXT -> "LARGE_BIG_EXT(${LARGE_BIG_EXT.toUByte()})"
        REFERENCE_EXT -> "REFERENCE_EXT(${REFERENCE_EXT.toUByte()})"
        NEW_REFERENCE_EXT -> "NEW_REFERENCE_EXT(${NEW_REFERENCE_EXT.toUByte()})"
        NEWER_REFERENCE_EXT -> "NEWER_REFERENCE_EXT(${NEWER_REFERENCE_EXT.toUByte()})"
        FUN_EXT -> "FUN_EXT(${FUN_EXT.toUByte()})"
        NEW_FUN_EXT -> "NEW_FUN_EXT(${NEW_FUN_EXT.toUByte()})"
        EXPORT_EXT -> "EXPORT_EXT(${EXPORT_EXT.toUByte()})"
        BIT_BINARY_EXT -> "BIT_BINARY_EXT(${BIT_BINARY_EXT.toUByte()})"
        NEW_FLOAT_EXT -> "NEW_FLOAT_EXT(${NEW_FLOAT_EXT.toUByte()})"
        ATOM_UTF8_EXT -> "ATOM_UTF8_EXT(${ATOM_UTF8_EXT.toUByte()})"
        SMALL_ATOM_UTF8_EXT -> "SMALL_ATOM_UTF8_EXT(${SMALL_ATOM_UTF8_EXT.toUByte()})"
        ATOM_EXT -> "ATOM_EXT(${ATOM_EXT.toUByte()})"
        SMALL_ATOM_EXT -> "SMALL_ATOM_EXT(${SMALL_ATOM_EXT.toUByte()})"
        else -> "unknown(${byte.toUByte()})"
    }
}
