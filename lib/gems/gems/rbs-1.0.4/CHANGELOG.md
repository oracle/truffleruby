# CHANGELOG

## master

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
