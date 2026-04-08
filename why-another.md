# TODO: update

### Why yet another data syntax?

* no white-space indentation and mandatory new lines like in YAML
* no mandatory `""` like in json
* can use `""`  and `''` interchangeably
* can use new-lines in `""` or `''` strings
* no commas
* not verbose like xml with the same capabilities
* built-in serialization/deserialization
* serialization to beautifully formatted text
* comments (both one-liners and multi-liners)  

#### no white-space indentation and mandatory new lines like in YAML
Same as in JSON. One can write the whole file in one line. It is very useful when you want to write your data in a string inside your code or in the command line. Also, very convenient if you want to include parts of the config in some input field, or in an Excel table.

In all these cases, white-space indentation would be a pain.

You can write in both ways:
```Java
    (a b c)
    
    (
        a
        b
        c
    )
```

#### no commas or semicolons
  You don't need to bother about them when adding or removing elements.
  And the noise level is very low.

#### no mandatory `""` like in json
  Which is also reduces noise level.
  Though you'd need `""` or `''` if a string should include spaces.
  Also much simpler to define part of the config in the Java String as don't need to constantly escape those quotes.

#### can use `""`  and `''` interchangeably
  First of all, it is slightly simpler to use `'` instead of `"`. Second - in Java, you don't need to escape `'` in strings. Third - you can choose `'` when `"` prevails in your text and vice versa (You'd have to escape `"` symbol in a string like `"quote: \" "`).

If you need to write something in Java code (for test purposes, or to make a request, for example), IODX seems the most easily writeable and readable.
```  
        String exampleJson = "{\"type\":\"VBox\",\"key\":\"value\",\"name\":\"Hello World\"}";
        String exampleYaml = "    type: VBox\n    key: value\n    name: Hello World\n";
        String exampleYads = "(type=VBox key=value name='Hello World')";
```
  
#### can use new-lines in `""` or `''` strings
  Like in YML
#### not verbose like xml with the same capabilities
  So you can write like:
```Java
HBox(
  pos=(100 200)
  VBox(
    Input(hint='...input here')
    Button(text='Send')
  )
)
```
  Instead of something like:
```XML
<HBox pos="100 200">
  <VBox>
    <Input hint="...input here"/>
    <Button text="Send"/>
  </VBox>
</HBox>
```

#### built-in serialization/deserialization
  Currently - Java only, but syntax provides ways to other languages to be included.
  Serialize any data to the human-readable string, and then back to the same data without any additional effort.
  No annotations needed.
  
#### serialization to beautifully formatted text
  1. convenient to read and edit
  1. can be used for reporting of data (tests)
  1. can be used to generate configs, not only read them

#### comments (both one-liners and multi-liners)  

#### Can be conveniently used for
  1. simple properties file
  1. config (with both read and wright)
  1. serialization in human-readable form

#### UTF, symbol escaping


