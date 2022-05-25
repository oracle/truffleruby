# Parser

The parser in TruffleRuby is originally from [JRuby's parser](https://github.com/jruby/jruby/blob/master/core/src/main/java/org/jruby/parser/RubyParser.y).
It has several modifications to use different names, uses Rope instead of ByteList, etc.

## Printing the parser AST

```bash
$ jt -q ruby tool/parse_ast.rb '2 ** 42'
Source:
2 ** 42

AST:
RootParseNode
  CallParseNode:**
    FixnumParseNode[value=2]
    ArrayParseNode
      FixnumParseNode[value=42]
, null
```

```bash
$ jt -q ruby tool/parse_ast.rb some_file.rb
```

You can also compare to JRuby's AST with:
```bash
$ jruby tool/parse_ast.rb '2 ** 42'
Source:
2 ** 42

AST:
RootNode line: 0
  CallNode:** line: 0
    FixnumNode line: 0, long: 2
    ArrayNode line: 0
      FixnumNode line: 0, long: 42
, null
```
