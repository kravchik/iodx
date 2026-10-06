# CongoCC grammar layout

`IodxCst.ccc` is the Java generation entry point and the only standalone `.ccc` file. Included fragments use the `.inc.ccc` suffix: the Maven plugin tracks them for incremental builds but does not treat them as separate grammars.

* `common/IodxTokens.inc.ccc` contains the target-independent lexical grammar.
* `common/IodxProductions.inc.ccc` contains the shared parser structure.
* `java/IodxJavaSupport.inc.ccc` constructs the Java `IodxCst` model and implements Java-specific literal conversion, source positions, and errors.

The shared productions depend on the following target support API:

```text
cstBeginNode
cstNewList
cstListAdd
cstListBody
cstSingleLineComment
cstMultiLineComment
cstInteger
cstFloatingPoint
cstRawToken
cstStructuralToken
cstString
cstIdentifier
cstClass
cstRequireClass
```

A new CongoCC target should provide its own entry point and support fragment while reusing both files under `common`. The support implementation owns native CST classes, collection types, number conversion, string unescaping, source positions, and parse errors.
