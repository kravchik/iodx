# IODX serialization and Java mapping

IODX has two related but separate layers:

1. The syntax layer parses text into primitives, `IodxEntity`, `IodxComment`, and key-value `Tuple` objects.
2. The Java mapping layer converts those values to and from Java lists, maps, and objects.

Keeping the layers separate is useful when comments and entity names are data themselves, or when Java reflection is not required.

## Processing pipeline

Reading:

```text
text -> IodxCstParser -> IodxCst -> IodxEntityFromCst -> values/IodxEntity
     -> IodxJavaFromEntity -> Java objects
```

Writing:

```text
Java objects -> IodxJavaToEntity -> values/IodxEntity -> IodxPrinter -> text
```

`Iodx` exposes the common entry points:

| Operation | One top-level value | Body without an outer wrapper |
| --- | --- | --- |
| Read syntax model | `readIodxEntity` | `readIodxEntities` |
| Print syntax model | `printIodxEntity` | `printIodxEntities` |
| Read Java object | `readJava` | `readJavaBody` |
| Print Java object | `printJava` | `printJavaBody` |

The singular read methods parse the complete input and require exactly one top-level value. Trailing non-whitespace input is rejected.

## Syntax values

### Numbers

* Decimal integers use `Integer`: `0`, `42`, `-42`.
* Decimal integers with `l` or `L` use `Long`: `42L`, `-42l`.
* Hex integers use `0x` or `0X` and may also have the Long suffix: `0xFF`, `-0X80`, `0x7fffL`.
* Octal notation is not supported. Decimal values other than zero cannot have leading zeroes, so `010` is invalid.
* Floating-point values use `Float` by default: `1.5`, `1.5f`, `.5`, `1e3`.
* The `d` or `D` suffix selects `Double`: `1.5d`, `2D`, `1e3D`.

The printer emits `l`, `f`, and `d` where needed to preserve Java numeric types.

### Strings and escapes

Simple strings can be unquoted. Use single or double quotes when a value contains whitespace, syntax characters, or could otherwise be read as a number, boolean, or `null`. Both quoted forms can span lines.

Quoted strings accept these escapes:

| Escape | Value |
| --- | --- |
| `\t` | tab |
| `\b` | backspace |
| `\r` | carriage return |
| `\f` | form feed |
| `\n` | newline |
| `\s` | one U+0020 space |
| `\\` | backslash |
| `\"` | double quote |
| `\'` | single quote |
| `\uXXXX` | one UTF-16 code unit written with exactly four hexadecimal digits |

A supplementary Unicode code point can be written as a valid high/low surrogate pair, for example `\uD83D\uDE00`. Lone or malformed surrogates are rejected. Printable Unicode is emitted literally; other ISO control characters are emitted as uppercase `\uXXXX`. Literal newlines remain literal, and ordinary spaces are not rewritten as `\s`.

Escapes are recognized only inside quoted strings.

### Collections and entities

```iodx
// list
(a b c)

// map
(key = value another = 42)

// empty list and empty map
()
(=)

// named entity; adjacency between the name and "(" is significant
Point(x = 10 y = 20)
```

An unnamed value containing key-value tuples is mapped to a Java map; an unnamed value containing positional elements is mapped to a Java list. Mixing tuples and positional values is not valid Java map input.

Comments are retained by the syntax layer. During Java object and map deserialization they are ignored. Comments inside a Java list are currently not supported by `IodxJavaFromEntity`.

## Java object mapping

```java
public class Point {
    public int x;
    public int y;
}

Point point = Iodx.readJava(Point.class, "Point(x = 10 y = 20)");
String text = Iodx.printJava(point);
```

`IodxJavaToEntity` serializes non-static, non-transient fields and omits fields equal to a newly created default instance unless `setSkipDefaultValues(false)` is used.

Serialization allows arbitrary POJO classes by default. Deserialization is intentionally explicit: every named entity must be registered through the `Iodx.readJava` class arguments, the `IodxJavaFromEntity` constructor, or `addImport`. Registration maps the IODX entity name to a Java class; there is no `import` statement in IODX text.

```java
Scene scene = Iodx.readJava(Scene.class, text, Point.class, Actor.class);

IodxJavaFromEntity reader = new IodxJavaFromEntity()
    .addImport("point", Point.class);
```

To restrict serialization to registered POJO classes, use:

```java
new IodxJavaToEntity(Point.class)
    .setAllClassesAvailable(false);
```

By default, object creation may fall back to constructor-free instantiation when no argumentless constructor exists. Set `setAllowInstantiationWithoutDefaultConstructor(false)` on the serializer and deserializer to require an argumentless constructor.

Custom conversions can be registered with `addSerializerByClass` and `addDeserializerByName`.

### Supported Java values

| Java value | Text representation | Round-trip status |
| --- | --- | --- |
| `null` | `null` | supported |
| `String` | quoted or unquoted string | supported |
| `Integer` | decimal or hex integer | supported |
| `Long` | integer with `l`/`L` suffix | supported |
| `Float` | floating point, optionally with `f`/`F` | supported |
| `Double` | floating point with `d`/`D` | supported |
| `Boolean` | `true` or `false` | supported |
| `List` | positional unnamed value | supported; deserializes as `YList` |
| `Map` | key-value unnamed value; `(=)` when empty | supported; deserializes as `YMap` |
| Registered POJO | named entity with field tuples | supported |
| `Character` | string | serialized value reads back as `String` |
| `Short` | decimal integer | serialized value reads back as `Integer` |
| `Byte` | none through `IodxJavaToEntity` | not supported |

Arrays, enums, dates, and other special types require a custom converter or explicit conversion to supported values.

## Body mapping

Body methods omit the outer entity/list/map wrapper:

```java
Point point = Iodx.readJavaBody(Point.class, "x = 10 y = 20");
String body = Iodx.printJavaBody(point); // x = 10 y = 20
```

For list and map bodies, pass the desired collection class and any named element classes to `readJavaBody`.

## Shared and cyclic references

The Java serializer tracks object identity for lists, maps, and POJOs. Repeated or cyclic objects are represented with `ref`:

```iodx
(
  ref(1 Point(x = 10 y = 20))
  ref(1)
)
```

`ref(id value)` defines and returns a value. `ref(id)` returns the previously defined object. A definition must appear before later uses; self-references work because the object is registered before its body is deserialized. Duplicate reference IDs and undefined IDs are rejected.
