# IODX

**Input Output Data syntaX**

IODX is a compact, human-readable data syntax for structured data in Java. It sits in the same space as JSON and YAML, but favors terse input, optional quoting, and readable entity-style notation.

* Main site: [iodx.org](https://iodx.org)
* Main repository: [kravchik/iodx](https://github.com/kravchik/iodx)

## Features

* no commas
* no white-space indentation or mandatory new lines
* lists, maps, entities, primitives
* API for parsing and serialization
* quotes are optional everywhere - keys, values, lists
* can use `""`  or `''`
* can use new-lines in `""` or `''` strings
* comments `//` and `/* */`
* escaping is optional (except `\` and relevant quote)

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
can be unquoted 

'single quoted do not need to escape "double" quotes'

"double quoted do not need to escape 'single' quotes"

'any string
can have new lines
in it'

"escaping is useful\s\s
though optional\n\n
except \\ and \" "

// other primitives
numbers = (123 1.23f -12.3d etc)
booleans = (true false)
nulls = null
```

## Real life examples

```java
// Some hierarchical UI definition
HBox(
  pos = (100 200)
  VBox(
    Input(hint = '...input here')
    Button(text = Send)
  )
)
```

```java
// Some config
serverType = node
port = 8080
//port = 80
data = (info = "Awesome super server" author = "John Doe")
services = (AuthService() AdminService())
```

```java
// Some properties
greeting = 'Hello traveller!'

signature = '
Have a nice day,
travaller!
'
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