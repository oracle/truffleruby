# Updating the Parser from JRuby

Before you do anything, check with Benoit Daloze for clearance to upgrade.

First, checkout JRuby in `../jruby`.

`tool/parser_renames.rb` can be used to handle most renames we do in
TruffleRuby's version of the parser:

```bash
ruby tool/parser_renames.rb < ../jruby/core/src/main/java/org/jruby/parser/RubyParser.y > src/main/java/org/truffleruby/parser/parser/RubyParser.y
```

Use it like `tool/import-mri-files.sh` is used in [Updating Ruby](updating-ruby.md).
See [Updating Ruby](updating-ruby.md) for how to reapply our changes on top.
