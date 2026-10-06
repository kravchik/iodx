# Why another data syntax?

IODX is intended for structured data that people regularly read and edit: configuration, fixtures, examples, logs, and serialized object graphs.

## Structure without indentation rules

Unlike YAML, indentation and line breaks are formatting rather than syntax. The same list can be compact or multiline:

```iodx
(a b c)

(
  a
  b
  c
)
```

This is useful for command-line values, source-code strings, form fields, and spreadsheet cells.

## Less punctuation

IODX does not require commas or semicolons between elements. Simple strings do not require quotes, while single and double quotes remain available for whitespace and syntax characters.

```java
String exampleJson = "{\"type\":\"VBox\",\"key\":\"value\",\"name\":\"Hello World\"}";
String exampleYaml = "type: VBox\nkey: value\nname: Hello World\n";
String exampleIodx = "(type=VBox key=value name='Hello World')";
```

Both quote styles support literal newlines, which keeps longer text readable without YAML block-scalar rules.

## Named entities

Named entities preserve structure without repeating XML-style closing tags or introducing a separate `type` field:

```iodx
HBox(
  pos = (100 200)
  VBox(
    Input(hint = '...input here')
    Button(text = Send)
  )
)
```

## Java mapping and formatting

The Java implementation maps IODX to primitives, collections, and registered classes without annotations. Its printer can produce compact or multiline canonical text, while references preserve shared and cyclic object identity.

IODX also retains single-line and block comments in its syntax model. See [serialization.md](serialization.md) for the exact mapping rules and current limitations.
