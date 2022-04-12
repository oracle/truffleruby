# CHANGELOG

## master

## 1.4.0 (2021-08-19)

This release includes feature enhancements including recursive `type` definition validation, improved compatibility of global variable names, and various method type updates.

### Signature updates

* openssl ([\#743](https://github.com/ruby/rbs/pull/743))
* `Array`, `Enumerator`, `Enumerable`, `Hash`, `FalseClass`, `Float`, `Hash`, `Integer`, `Object`, `Range`, `TrueClass` ([\#728](https://github.com/ruby/rbs/pull/728))
* `Array#[]` ([\#732](https://github.com/ruby/rbs/pull/732))
* `Exception#set_backtrace` ([\#738](https://github.com/ruby/rbs/pull/738))
* `Kernel#Array` ([\#733](https://github.com/ruby/rbs/pull/733))
* `Kernel#spawn` ([\#748](https://github.com/ruby/rbs/pull/748))
* `Kernel#String` ([\#745](https://github.com/ruby/rbs/pull/745), [\#751](https://github.com/ruby/rbs/pull/751))
* `URI::Generic#fragment` ([\#752](https://github.com/ruby/rbs/pull/752))
* `URI::Generic#merge` ([\#746](https://github.com/ruby/rbs/pull/746))

### Language updates

* Add global variables signature ([\#749](https://github.com/ruby/rbs/pull/749))

### Library changes

* Add Recursiive type alias defnition validation ([\#719](https://github.com/ruby/rbs/pull/719))
* Generate included modules with complete name ([\#731](https://github.com/ruby/rbs/pull/731))
* Fix `rbs-prototype-rb` error when multi assign with const ([\#740](https://github.com/ruby/rbs/pull/740))

## 1.3.3 (2021-07-28)

This release includes a minor update of `resolv` library RBS and a fix of test for `ruby/ruby` CI.

### Signature updates

* resolv ([#726](https://github.com/ruby/rbs/pull/726))

## 1.3.2 (2021-07-23)

This release is to fix a bug introduced by parser update in 1.3.0.

* Fix parser to accept alias names starting with `_` ([#723](https://github.com/ruby/rbs/pull/723))

## 1.3.1 (2021-07-21)

This release is to fix a bug introduced by parser update in 1.3.0.

* Fix parser to accept param names starting with `_` ([#721](https://github.com/ruby/rbs/pull/721))

## 1.3.0 (2021-07-20)

### Summary

RBS 1.3.0 includes bug fixees of the parser and class/module definition validations.

### Signature updates

* dbm ([#718](https://github.com/ruby/rbs/pull/718))
* net-http ([#686](https://github.com/ruby/rbs/pull/686))
* optparse ([#693](https://github.com/ruby/rbs/pull/693))
* resolv ([#697](https://github.com/ruby/rbs/pull/697))
* socket ([#699](https://github.com/ruby/rbs/pull/699))
* `IO` ([#698](https://github.com/ruby/rbs/pull/698))
* `Marshal` ([#684](https://github.com/ruby/rbs/pull/684))
* `Mutex` ([#683](https://github.com/ruby/rbs/pull/683))
* `Array#shift` ([#707](https://github.com/ruby/rbs/pull/707))
* `BasicObject#method_missing` ([#707](https://github.com/ruby/rbs/pull/706), [#710](https://github.com/ruby/rbs/pull/710))
* `Kernel#caller` ([#705](https://github.com/ruby/rbs/pull/705))

### Library changes

* Interface names starting with lower case characters are now syntax error ([#678](https://github.com/ruby/rbs/pull/678), [#720](https://github.com/ruby/rbs/pull/720))
* Mixins of classes are rejected ([#681](https://github.com/ruby/rbs/pull/681))
* Generate prototype of `initialize` method with return type `void` ([#685](https://github.com/ruby/rbs/pull/685))
* Let `prototype runtime` generate class/module declarations in nested syntax ([#700](https://github.com/ruby/rbs/pull/700))
* Fix race condition for multi-thread support ([#702](https://github.com/ruby/rbs/pull/702))

### Miscellaneous

* Add new doc `docs/rbs_by_example.md` ([#694](https://github.com/ruby/rbs/pull/694))

## 1.2.1 (2021-05-27)

This release includes the following minor changes:

* Fix test to run the tests in ruby repository. ([#679](https://github.com/ruby/rbs/pull/679))
* Remove unnecessary files from the gem package. ([#675](https://github.com/ruby/rbs/pull/675))
* Suppress unused variable warning ([#674](https://github.com/ruby/rbs/pull/674))
* Update documents ([#672](https://github.com/ruby/rbs/pull/672))

## 1.2.0 (2021-04-21)

### Summary

RBS 1.2 ships with better support for AST/token locations and `Locator` utility class. The AST objects now keep the locations of tokens. The `Locator` class is to translate the text position (line and column) to semantic object at the location. The class allows to find if a text position is on the class name of a class declaration.

### Signature updates

* Hash ([#631](https://github.com/ruby/rbs/pull/631), [#632](https://github.com/ruby/rbs/pull/632), [\#637](https://github.com/ruby/rbs/pull/637), [\#638](https://github.com/ruby/rbs/pull/638), [\#639](https://github.com/ruby/rbs/pull/639), )
* Module ([\#645](https://github.com/ruby/rbs/pull/645))
* Enumerable ([\#647](https://github.com/ruby/rbs/pull/647))
* Array ([\#648](https://github.com/ruby/rbs/pull/648))
* Proc ([\#649](https://github.com/ruby/rbs/pull/649))
* Struct ([\#650](https://github.com/ruby/rbs/pull/650), [\#668](https://github.com/ruby/rbs/pull/668))
* Thread ([\#651](https://github.com/ruby/rbs/pull/651))
* Random ([\#669](https://github.com/ruby/rbs/pull/669))
* Shellwords ([\#665](https://github.com/ruby/rbs/pull/665))
* IO ([\#659](https://github.com/ruby/rbs/pull/659))

### Language updates

* Module self type syntax update ([\#653](https://github.com/ruby/rbs/pull/653))

### Library changes

* Token locations ([\#666](https://github.com/ruby/rbs/pull/666))
* Add RBS::Locator ([\#667](https://github.com/ruby/rbs/pull/667))
* Fix runtime type checker ([\#644](https://github.com/ruby/rbs/pull/644))

### Miscellaneous

* Update documentation for overloading ([\#658](https://github.com/ruby/rbs/pull/658))
* Update target ruby version ([\#633](https://github.com/ruby/rbs/pull/633))

## 1.1.1 (2021-03-12)

### Signature updates

* rubygem ([#630](https://github.com/ruby/rbs/pull/630))

## 1.1.0 (2021-03-08)

### Summary

Errors are now organized by `RBS::BaseError`, `RBS::ParsingError`, `RBS::LoadingError`, and `RBS::DefinitionError`.
The library users can rescue RBS related errors with `RBS::BaseError`, parsing errors with `RBS::ParsingError`, and other errors with `RBS::LoadingError` and `RBS::DefinitionErrors`.

Updating a part of environments are supported. Library users can remove declarations read from a set of files, adding new declarations, running name resolution related to the new decls, and deleting `DefinitionBuilder` caches related to the changes.
See `RBS::Environment#reject`, `RBS::Environment#resolve_type_names`, `RBS::AncestorGraph`, and `RBS::DefinitionBuilder#update`.

`RBS::DefinitionBuilder#build_singleton` now returns definitions containing `instance` type, which had returned resolved class instance types. This is a breaking change, but we consider it a bug fix because `RBS::DefinitionBuilder#build_instance` has returned `instance` types and `#build_singleton` has returned `class` type.

### Signature updates

* rubygem ([\#605](https://github.com/ruby/rbs/pull/605), [\#610](https://github.com/ruby/rbs/pull/610))
* Array ([\#612](https://github.com/ruby/rbs/pull/612), [\#614](https://github.com/ruby/rbs/pull/614))
* cgi/core ([\#599](https://github.com/ruby/rbs/pull/599))
* Thread ([\#618](https://github.com/ruby/rbs/pull/618))

### Language updates

* Allow trailing comma for Record and Tuple types ([\#606](https://github.com/ruby/rbs/pull/606))

### Library changes

* Allow partial update of RBS declarations ([\#608](https://github.com/ruby/rbs/pull/608), [\#621](https://github.com/ruby/rbs/pull/621))
* Let errors have `TypeName` ([\#611](https://github.com/ruby/rbs/pull/611))
* Add `Parser::LexerError` ([\#615](https://github.com/ruby/rbs/pull/615))
* Performance improvement ([\#617](https://github.com/ruby/rbs/pull/617), [\#620](https://github.com/ruby/rbs/pull/620))
* No substitute `instance` types on `#build_singleton` ([\#619](https://github.com/ruby/rbs/pull/619))

### Miscellaneous

* Make racc name customizable by `RACC` environment variable ([\#602](https://github.com/ruby/rbs/pull/602))
* Suppress warnings ([\#624](https://github.com/ruby/rbs/pull/624))
* Remove needless `Gem::Version` polyfill ([\#622](https://github.com/ruby/rbs/pull/622))

## 1.0.6 (2021-02-17)

* Signature Updates
  * `Enumerable` ([\#595](https://github.com/ruby/rbs/pull/595), [\#596](https://github.com/ruby/rbs/pull/596), [\#601](https://github.com/ruby/rbs/pull/601))
  * `#as_json` ([\#597](https://github.com/ruby/rbs/pull/597))

## 1.0.5 (2021-02-13)

* Signature Updates
  * Enumerable ([\#596](https://github.com/ruby/rbs/pull/596))
  * Set ([\#595](https://github.com/ruby/rbs/pull/595))
  * `#to_json` ([\#592](https://github.com/ruby/rbs/pull/592))
  * `<=>` ([\#593](https://github.com/ruby/rbs/pull/593))
  * Timeout ([\#586](https://github.com/ruby/rbs/pull/586))
  * URI::RFC2396_Parser ([\#587](https://github.com/ruby/rbs/pull/587))
* Rename generic class parameters on re-open ([\#594](https://github.com/ruby/rbs/pull/594))
* Make `refute_send_type` check that method call doesn't match with types in RBS ([\#588](https://github.com/ruby/rbs/pull/588))

## 1.0.4 (2021-01-31)

* Unbundle `rr` to run test in `ruby/ruby` repo ([#585](https://github.com/ruby/rbs/pull/585))

## 1.0.3 (2021-01-28)

* Set up `#ancestors` and `#location` of `RecursiveAncestorError` ([#583](https://github.com/ruby/rbs/pull/583))

## 1.0.2 (2021-01-28)

* Signature Updates
  * Kernel([#582](https://github.com/ruby/rbs/pull/582))

## 1.0.1 (2021-01-27)

* Signature Updates
  * PrettyPrint ([\#573](https://github.com/ruby/rbs/pull/573))
  * FileUtils ([\#576](https://github.com/ruby/rbs/pull/576))
  * UnboundMethod ([\#555](https://github.com/ruby/rbs/pull/555))
  * IO ([\#571](https://github.com/ruby/rbs/pull/571))
  * Kernel ([\#549](https://github.com/ruby/rbs/pull/549), [\#569](https://github.com/ruby/rbs/pull/569))
  * File ([\#552](https://github.com/ruby/rbs/pull/552))
  * Data is removed ([\#570](https://github.com/ruby/rbs/pull/570))
  * Module ([\#568](https://github.com/ruby/rbs/pull/568))
  * Object ([\#557](https://github.com/ruby/rbs/pull/557))
* Renew test sample code ([#581](https://github.com/ruby/rbs/pull/581))
* Add description to `generate:stdlib_test` Rake task ([\#578](https://github.com/ruby/rbs/pull/578))
* Declare supported ruby version >= 2.6 ([\#548](https://github.com/ruby/rbs/pull/548))
* Fix module self-type override ([\#577](https://github.com/ruby/rbs/pull/577))
* Fix parser to support all operator symbols ([\#550](https://github.com/ruby/rbs/pull/550))
* Migrate from Minitest to Test::Unit ([\#556](https://github.com/ruby/rbs/pull/556))
* Fix type alias parsing ([\#565](https://github.com/ruby/rbs/pull/565))
* Support end-less method definition in `prototype rb` ([\#561](https://github.com/ruby/rbs/pull/561))
* Support method argument forwarding in `prototype rb` ([\#560](https://github.com/ruby/rbs/pull/560))

## 1.0.0 (2020-12-24)

* Signature updates for `URI`, `IO`, `File`, `Pathname`, `Module`, and `Time` ([#529](https://github.com/ruby/rbs/pull/529), [#521](https://github.com/ruby/rbs/pull/521), [#520](https://github.com/ruby/rbs/pull/520), [#511](https://github.com/ruby/rbs/pull/511), [#517](https://github.com/ruby/rbs/pull/517), [#542](https://github.com/ruby/rbs/pull/542), [#546](https://github.com/ruby/rbs/pull/546), [#540](https://github.com/ruby/rbs/pull/540), [#538](https://github.com/ruby/rbs/pull/538))
* `rbs prototype runtime` generates `extend`s ([#535](https://github.com/ruby/rbs/pull/535))
* `rbs prototype runtime` stability improvements ([#533](https://github.com/ruby/rbs/pull/533), [#526](https://github.com/ruby/rbs/pull/526))
* `rbs prototype rb` compatibility improvements ([#545](https://github.com/ruby/rbs/pull/545))
* Implement method names escape in `RBS::Writer` ([#537](https://github.com/ruby/rbs/pull/537))
* Improve runtime type checker compatibility ([#532](https://github.com/ruby/rbs/pull/532), [#528](https://github.com/ruby/rbs/pull/528), [#547](https://github.com/ruby/rbs/pull/547))
* Fix `ruby2_keywords` for `Proc` in Ruby <2.7 ([#513](https://github.com/ruby/rbs/pull/513))
* Better compatibility for record type attribute names ([#525](https://github.com/ruby/rbs/pull/525), [#524](https://github.com/ruby/rbs/pull/524))
* Let module self-types be classes ([#523](https://github.com/ruby/rbs/pull/523))
* Delete `extension` syntax ([#543](https://github.com/ruby/rbs/pull/543))
* Method resolution improvements about `alias` and `.new` ([#522](https://github.com/ruby/rbs/pull/522), [#519](https://github.com/ruby/rbs/pull/519), [#516](https://github.com/ruby/rbs/pull/516))
* Better message for `DuplicatedMethodDefinitionError` ([#539](https://github.com/ruby/rbs/pull/539))

## 0.20.1 (2020-12-06)

* Make the order of RBS load reproducible ([#508](https://github.com/ruby/rbs/pull/508))

## 0.20.0 (2020-12-06)

* Signature updates for `TSort`, `DBM`, `Time`, and `Hash` ([#496](https://github.com/ruby/rbs/pull/496), [#497](https://github.com/ruby/rbs/pull/497), [#499](https://github.com/ruby/rbs/pull/499), [#507](https://github.com/ruby/rbs/pull/507))
* Add _singleton attribute_ syntax ([#502](https://github.com/ruby/rbs/pull/502), [#506](https://github.com/ruby/rbs/pull/506), [#505](https://github.com/ruby/rbs/pull/505))
* Proc types with blocks ([#503](https://github.com/ruby/rbs/pull/503))
* Add support for escape sequences in string literal types ([#501](https://github.com/ruby/rbs/pull/501))
* Fix runtime type checking of blocks with keyword args ([#500](https://github.com/ruby/rbs/pull/500))

## 0.19.0 (2020-12-02)

* Signature updates for `Monitor` and File ([#485](https://github.com/ruby/rbs/pull/485), [#495](https://github.com/ruby/rbs/pull/495))

## 0.18.1 (2020-12-01)

* Fix `EnvironmentWalker#each_type_name` ([#494](https://github.com/ruby/rbs/pull/494))

## 0.18.0 (2020-12-01)

* Signature updates for `YAML`, `ObjectSpace`, and `Singleton` ([#408](https://github.com/ruby/rbs/pull/408), [#477](https://github.com/ruby/rbs/pull/477), [#482](https://github.com/ruby/rbs/pull/482))
* `prototype rb` improvements ([#492](https://github.com/ruby/rbs/pull/492), [#487](https://github.com/ruby/rbs/pull/487), [#486](https://github.com/ruby/rbs/pull/486), [#481](https://github.com/ruby/rbs/pull/481))
* Runtime type checker improvements ([#488](https://github.com/ruby/rbs/pull/488), [#489](https://github.com/ruby/rbs/pull/489), [#490](https://github.com/ruby/rbs/pull/490))
* Update `DependencyWalker` API to receive _Node_ objects instead of `TypeName` ([#484](https://github.com/ruby/rbs/pull/484))
* Assume encoding of RBS files to be UTF-8 ([#493](https://github.com/ruby/rbs/pull/493))

## 0.17.0 (2020-11-14)

* Signature updates for `Enumerable`, `Hash`, and `TSort` ([#462](https://github.com/ruby/rbs/pull/462), [#468](https://github.com/ruby/rbs/pull/468), [#471](https://github.com/ruby/rbs/pull/471), [#472](https://github.com/ruby/rbs/pull/472), [#473](https://github.com/ruby/rbs/pull/473), [#474](https://github.com/ruby/rbs/pull/474))
* Parser error handling improvement ([#463](https://github.com/ruby/rbs/pull/463), [#475](https://github.com/ruby/rbs/pull/475))
* Hash spread syntax handling improvement with `prototype rb` ([#465](https://github.com/ruby/rbs/pull/465))

## 0.16.0 (2020-11-05)

* Signature update for `DBM` ([#441](https://github.com/ruby/rbs/pull/441))
* RBS repository ([#405](https://github.com/ruby/rbs/pull/405))
* Support `alias` in `rbs prototype rb` ([#457](https://github.com/ruby/rbs/pull/457))

## 0.15.0 (2020-11-02)

* Signature updates for `Kernel`, `PStore`, `Enumerable`, and `Array` ([#450](https://github.com/ruby/rbs/pull/450), [#443](https://github.com/ruby/rbs/pull/443), [#438](https://github.com/ruby/rbs/pull/438), [#437](https://github.com/ruby/rbs/pull/437), [#433](https://github.com/ruby/rbs/pull/433), [#432](https://github.com/ruby/rbs/pull/432))
* Add helper interfaces ([#434](https://github.com/ruby/rbs/pull/434), [#428](https://github.com/ruby/rbs/pull/428))
* Change `bool` type semantics ([#456](https://github.com/ruby/rbs/pull/456))
* Support alias in `rbs prototype rb` ([#457](https://github.com/ruby/rbs/pull/457))
* Runtime testing improvements ([#455](https://github.com/ruby/rbs/pull/455), [#447](https://github.com/ruby/rbs/pull/447), [#444](https://github.com/ruby/rbs/pull/444), [#431](https://github.com/ruby/rbs/pull/431))
* Fix proc type parsing ([#451](https://github.com/ruby/rbs/pull/451))
* Fix type variable parsing ([#442](https://github.com/ruby/rbs/pull/442))


## 0.14.0 (2020-10-17)

* Allow keyword names ending with `?` and `!` ([#417](https://github.com/ruby/rbs/pull/417))
* Make `Range[T]` covariant ([#418](https://github.com/ruby/rbs/pull/418))

## 0.13.1 (2020-10-09)

* Fix test for CI of ruby/ruby ([#412](https://github.com/ruby/rbs/pull/412))

## 0.13.0 (2020-10-09)

* Signature updates for `URI` classes.
* Fix tests ([#410](https://github.com/ruby/rbs/pull/410))
* Add `--silent` option for `rbs validate` ([#411](https://github.com/ruby/rbs/pull/411))

## 0.12.2 (2020-09-17)

* Minor signature update for `pty`
* Fix `PTY` stdlib test

## 0.12.1 (2020-09-16)

This version is to improve Ruby 3 testing compatibility. Nothing changed for users.

## 0.12.0 (2020-09-15)

* Signature updates for `forwardable`, `struct`, `set`, `URI::Generic`, `URI::File`, and `BigDecimal`.
* Define `.new` methods from `initialize` included from modules [#390](https://github.com/ruby/rbs/pull/390)

## 0.11.0 (2020-08-31)

* Signature update for `date/datetime` [#367](https://github.com/ruby/rbs/pull/367)
* Add test double support for runtime type checker [#380](https://github.com/ruby/rbs/pull/380)
* Add `rbs test` command for runtime type checking [#366](https://github.com/ruby/rbs/pull/366)
* Fix runtime type checking for record types [#365](https://github.com/ruby/rbs/pull/365)
* Improve EnvironmentLoader API [#370](https://github.com/ruby/rbs/pull/370)
* Allow overloading from super methods [#364](https://github.com/ruby/rbs/pull/364)

## 0.10.0 (2020-08-10)

* Signature update for `Zlib`
* Make "no type checker installed" message a debug print [#363](https://github.com/ruby/rbs/pull/363)
* Print `...` for overloading method definitions [#362](https://github.com/ruby/rbs/pull/362)
* Allow missing method implementation in Ruby code [#359](https://github.com/ruby/rbs/pull/359)
* Runtime testing improvements [#356](https://github.com/ruby/rbs/pull/356)

## 0.9.1 (2020-08-04)

* Ensure using Module#name [#354](https://github.com/ruby/rbs/pull/354)
* Fix runtime test setup [#353](https://github.com/ruby/rbs/pull/353)

## 0.9.0 (2020-08-03)

* Fix signature validation [#351](https://github.com/ruby/rbs/pull/351), [#352](https://github.com/ruby/rbs/pull/352)
* Parsing performance improvement [#350](https://github.com/ruby/rbs/pull/350)

## 0.8.0 (2020-08-01)

* Signature updates for `Enumerator` and `PTY`
* Fix prototype rb/rbi error handling [#349](https://github.com/ruby/rbs/pull/349)
* Runtime test improvements [#344](https://github.com/ruby/rbs/pull/344), [#343](https://github.com/ruby/rbs/pull/343)
* Add `...` syntax [#342](https://github.com/ruby/rbs/pull/342)

## 0.7.0 (2020-07-20)

* Add `DefinitionBuilder#one_instance_ancestors` and `DefinitionBuilder#one_singleton_ancestors` [#341](https://github.com/ruby/rbs/pull/341)
* Bug fix in ConstantTable [#340](https://github.com/ruby/rbs/pull/340)
* Make `rbs validate` faster [#338](https://github.com/ruby/rbs/pull/338)
* Dedup methods generated by `rbs prototype rb` [#334](https://github.com/ruby/rbs/pull/334)

## 0.6.0 (2020-07-12)

* Signature update for `Logger`.
* Clean `Environment#inspect`. [#331](https://github.com/ruby/rbs/pull/331)
* Module self type syntax update. [#329](https://github.com/ruby/rbs/pull/329)
* Better validation. [#328](https://github.com/ruby/rbs/pull/328)
* Parser performance improvement. [#327](https://github.com/ruby/rbs/pull/327)
* Runtime type checking performance improvements with sampling [#323](https://github.com/ruby/rbs/pull/323)

## 0.5.0 (2020-07-04)

* Signature updates for `Mutex_m`, `IO`, and `Enumerable`.
* Syntax update. [#307](https://github.com/ruby/rbs/pull/307)
* AST command prints _absolute_ type names with file name filtering. [#312](https://github.com/ruby/rbs/pull/312)
* Improve CLI message. [#309](https://github.com/ruby/rbs/pull/309)

## 0.4.0 (2020-06-15)

* Signature update for `Fiber`, `Encoding`, and `Enumerator`.
* Fix syntax error for underscore and `!` `?` [#304](https://github.com/ruby/rbs/pull/304)
* Improved return type inference in `rbs prototype rb` [#303](https://github.com/ruby/rbs/pull/303)
* Skip anonymous modules/classes in `rbs prototype runtime` [#302](https://github.com/ruby/rbs/pull/302)
* Fix `--require-relative` option in `rbs prototype runtime` [#299](https://github.com/ruby/rbs/pull/299)
* Add JSON schema for `rbs ast` [#295](https://github.com/ruby/rbs/pull/295)

## 0.3.1 (2020-05-22)

* Fix constant resolution again [#289](https://github.com/ruby/rbs/pull/289)

## 0.3.0 (2020-05-20)

* Fix constant resolution [#288](https://github.com/ruby/rbs/pull/288)

## 0.2.0

* The first release of RBS gem.

## 0.1.0

* Version 0.1.0 is the original `rbs` gem and it is different software from RBS.
