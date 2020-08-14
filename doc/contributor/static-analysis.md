# Static Analysis of TruffleRuby

We apply static analysis to the production code that we maintain -
`lib/truffle`, `lib/cext`, `src/annotations`, `src/launcher`, `src/main`,
`src/services` (some parts of static analysis are also applied to other files,
but these are the key ones).

## C

The [Clang Static Analyzer](https://clang-analyzer.llvm.org) finds bugs in C
code. We occasionally run this tool locally but only take its output as a
suggestion. We use a default configuration.

```bash
scan-build --use-analyzer `which clang` -analyze-headers clang -c --std=c99 -Ilib/cext/include src/main/c/cext/ruby.c src/main/c/truffleposix/truffleposix.c
scan-view ...as instructed by scan-build...
```

## Java

### DSL usage

We have a tool to check that some use of our internal annotations and the
Truffle DSL are correct. Passing this is enforced in our CI gate.

```bash
jt check_dsl_usage
```

### CheckStyle

[CheckStyle](http://checkstyle.sourceforge.net) enforces a Java style guide.
Passing CheckStyle is enforced in our CI gate.

```bash
mx checkstyle
```

### SpotBugs

[SpotBugs](https://spotbugs.github.io) looks for potential Java programming
errors. We run it with the default Graal project configuration. Passing
SpotBugs is enforced in our CI gate.

```bash
mx spotbugs
```

## Ruby

### Rubocop

[Rubocop](https://github.com/rubocop-hq/rubocop) enforces a Ruby style guide.
It's configured in `.rubocop.yml`, and can be run locally as `jt rubocop`.
Passing Rubocop is enforced in our CI gate.

```bash
jt rubocop
```

### Fasterer

[Fasterer](https://github.com/DamirSvrtan/fasterer) looks for potential
performance improvements. We occasionally run this tool locally but only take
its output as a suggestion. We use a default configuration.

```bash
gem install fasterer
fasterer lib/truffle lib/cext src/main
```

### Reek

[Reek](https://github.com/troessner/reek) looks for patterns of Ruby code
which could be improved - generally around making it simpler and more clear.
We occasionally run this tool locally but only take its output as a
suggestion. We disable a lot of the defaults in `.reek.yml`, either because
we're implementing a set API, because we're doing something low-level or
outside normal Ruby semantics, or for performance reasons.

```bash
gem install reek
reek lib/truffle lib/cext src/main
```

### Flog

[Flog](http://ruby.sadi.st/Flog.html) lists methods by complexity. You can
check that your methods do not appear near the top of this list. We
occasionally run this tool locally but only take its output as a suggestion.

```bash
gem install flog
flog -m -t 10 lib/truffle lib/cext src/main
```

### Flay

[Flay](http://ruby.sadi.st/Flay.html) finds similar or identical code, which
could potentially be factored out. We occasionally run this tool locally but
only take its output as a suggestion.

```bash
gem install flay
flay lib/truffle lib/cext src/main
```

### Brakeman

[Brakeman](https://github.com/presidentbeef/brakeman) looks for security
vulnerabilities. It's really designed for Rails, and many of the rules are
specific to Rails, but we do run it ocassionally anyway and take its output as
a suggestion.

```bash
gem install brakeman
brakeman --force-scan --run-all-checks --interprocedural --no-pager --add-libs-path src --only-files lib/truffle/,lib/cext/,src/main/ruby/truffleruby/
```
