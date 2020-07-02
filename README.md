# KETF

kotlinx.serialization support for the [External Term Format](http://erlang.org/doc/apps/erts/erl_ext_dist.html).

## Supported types

| ETF type            | Kotlin type            |
|---------------------|------------------------|
| SMALL_INTEGER_EXT   | Byte, Char, Int, Long  |
| INTEGER_EXT         | Short, Int, Long       |
| SMALL_TUPLE_EXT     | List                   |
| LARGE_TUPLE_EXT     | List                   |
| MAP_EXT             | Map, Class             |
| NIL_EXT             | null                   |
| STRING_EXT          | String                 |
| LIST_EXT            | List, String¹          |
| SMALL_BIG_EXT       | Long²                  |
| LARGE_BIG_EXT       | Long²                  |
| NEW_FLOAT_EXT       | Double, Float          |
| ATOM_UTF8_EXT       | Boolean, property name |
| SMALL_ATOM_UTF8_EXT | Boolean, property name |
| ATOM_EXT            | Boolean, property name |
| SMALL_ATOM_EXT      | Boolean, property name |
| ATOM_CACHE_REF      | none                   |
| FLOAT_EXT           | none                   |
| PORT_EXT            | none                   |
| NEW_PORT_EXT        | none                   |
| PID_EXT             | none                   |
| NEW_PID_EXT         | none                   |
| BINARY_EXT          | none                   |
| REFERENCE_EXT       | none                   |
| NEW_REFERENCE_EXT   | none                   |
| NEWER_REFERENCE_EXT | none                   |
| FUN_EXT             | none                   |
| NEW_FUN_EXT         | none                   |
| EXPORT_EXT          | none                   |
| BIT_BINARY_EXT      | none                   |
 
* ¹ Strings longer than [65535 characters](http://erlang.org/doc/apps/erts/erl_ext_dist.html#list_ext) will be represented as a list of chars instead.
* ² Values decoded from this tag may be bigger than Long.MAX_VALUE. Decoding values that would cause an overflow will throw a `SerializationException` instead.
