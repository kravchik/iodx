# IODX

**Input Output Data syntaX**

IODX is a compact, human-readable syntax for structured data, configs, fixtures, serialization, and data exchange. It keeps JSON-like structure without mandatory commas or quotes and does not use indentation as syntax.

* Main site: [iodx.org](https://iodx.org)
* Java implementation: [kravchik/iodx](https://github.com/kravchik/iodx)

## Features

* no commas
* no whitespace indentation or mandatory line breaks
* quotes can be omitted in keys and values when the string is simple
* lists, maps, entities, and primitives
* both `""` and `''` can be used for quoted strings
* any quoted string can span multiple lines
* escape sequences are supported in both quoted forms; only backslashes and the matching quote must be escaped
* comments with `//` and `/* */`

## Syntax

```iodx
// list
(string 'quoted string' 123)

// maps
usualMap = (key = value 'quoted key' = "quoted value")
emptyMap = (=)

// named entity: no whitespace between the name and "("
entity(key = value and some list also)

// strings
can_be_unquoted

'single quotes can contain "double quotes" without escaping'
"double quotes can contain 'single quotes' without escaping"

'any quoted string
can have new lines
in it'

"escapes include \t, \n, \s, \\, and \u263A"

// numbers, booleans, and null
numbers = (123 -42 0xFF 123L 1.23 1.23f -12.3d 1e3)
booleans = (true false)
noValue = null
```

More precise rules for numbers, strings, Java types, and references are in [serialization.md](serialization.md).

## Examples

Some hierarchical UI configuration in IODX:

```iodx
// Some hierarchical UI definition
HBox(
  pos = (100 200)
  VBox(
    Input(hint = '...input here')
    Button(text = Send)
  )
)
```

Roughly the same structure in JSON:

```json
{
  "type": "HBox",
  "pos": [100, 200],
  "children": [
    {
      "type": "VBox",
      "children": [
        {
          "type": "Input",
          "hint": "...input here"
        },
        {
          "type": "Button",
          "text": "Send"
        }
      ]
    }
  ]
}
```

And in YAML:

```yaml
type: HBox
pos:
  - 100
  - 200
children:
  - type: VBox
    children:
      - type: Input
        hint: ...input here
      - type: Button
        text: Send
```

```iodx
// Some config
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```

Roughly the same structure in JSON:

```json
{
  "serverType": "node",
  "port": 8080,
  "data": {
    "info": "Awesome super server",
    "author": "John Doe"
  },
  "services": [
    {
      "type": "AuthService"
    },
    {
      "type": "AdminService"
    }
  ]
}
```

And in YAML:

```yaml
serverType: node
port: 8080
data:
  info: Awesome super server
  author: John Doe
services:
  - type: AuthService
  - type: AdminService
```

```iodx
// Some properties
greeting = 'Hello traveller!'

signature = '
Have a nice day,
traveller!
'
```

Roughly the same structure in JSON:

```json
{
  "greeting": "Hello traveller!",
  "signature": "\nHave a nice day,\ntraveller!\n"
}
```

And in YAML:

```yaml
greeting: Hello traveller!
signature: |

  Have a nice day,
  traveller!
```

## Java API

`yk.lang.iodx.Iodx` is the entry point for common operations.

### API features

* reading and writing text, syntax data, and Java classes
* reading and writing either one top-level value or multiple values
* printing with configurable formatting
* first-class comments that can be created when writing or inspected when reading
* Java serialization and deserialization

```java
Object entity = Iodx.readIodxEntity("hello(world)");
String text = Iodx.printIodxEntity(entity);

Point point = Iodx.readJava(Point.class, "Point(x = 10 y = 20)");
String serialized = Iodx.printJava(point);
```

Use `readIodxEntity`/`readIodxEntities` when you need the syntax model, and `readJava`/`printJava` when you need Java object mapping. The `readJavaBody`/`printJavaBody` methods operate on the contents of an object, list, or map without an outer wrapper.

See [serialization.md](serialization.md) for the complete mapping rules and limitations.

## Maven artifact

```xml
<repositories>
    <repository>
        <id>yk</id>
        <url>https://github.com/kravchik/mvn-repo/raw/master</url>
    </repository>
</repositories>

<dependency>
    <groupId>yk</groupId>
    <artifactId>iodx</artifactId>
    <version>0.4</version>
</dependency>
```

Current development version is `0.5-SNAPSHOT`. The project targets Java 8 and is tested on newer JDK releases in CI.
