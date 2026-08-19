# IODX

**Input Output Data syntaX**

IODX is a compact, human-readable syntax for any structured data. It is like JSON or YAML, but better.

* Main site: [iodx.org](https://iodx.org)
* Java implementation: [kravchik/iodx](https://github.com/kravchik/iodx)

## Features

* no commas
* no white-space indentation or mandatory new lines
* quotes can be omitted in keys and values (if the string is simple)
* lists, maps, entities, primitives
* can use `""`  or `''`
* any quoted string supports new lines
* escaping in any quoted string, and it is optional (except `\` and relevant quote)
* comments `//` and `/* */`

## Syntax

```text
// list
(string 'quoted string' 123)

// maps
usualMap = (key=value 'quoted key'="quoted value")
emptMap = (=)

// entity
entity(key=values and some list also)

// strings
can_be_unquoted 

'single quoted do not need to escape "double" quotes, but \"can do so\"'

"double quoted do not need to escape 'single' quotes, but also \'can\'"

'any string
can have new lines
in it'

"escaping is useful\s\s
and it is optional\n\n
except for \\ and \" "

// other primitives
numbers = (123 1.23f -12.3d etc)
booleans = (true false)
nulls = null
```

## Example
        
Some UI config in IODX

```text
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

```text
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

```text
// Some properties
greeting = 'Hello traveller!'

signature = '
Have a nice day,
travaller!
'
```

Roughly the same structure in JSON:

```json
{
  "greeting": "Hello traveller!",
  "signature": "\nHave a nice day,\ntravaller!\n"
}
```

And in YAML:

```yaml
greeting: Hello traveller!
signature: |

  Have a nice day,
  travaller!
```

## API

`yk.lang.iodx.Iodx` is an entry point. Look there for common scenarios and exmples.

### API features

* reading/writing text/data/classes
* can read/write one src with one value, or many values
* printing with tunable formatting
* comments are first level citizen - add them on writing, or analyze on reading
* java ser/deser

## mvn artifact

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

Current development version is `0.5-SNAPSHOT`.
