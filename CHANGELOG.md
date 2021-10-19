# 22.0.0

New features:


Bug fixes:

* Fix `File.utime` to use nanoseconds (#2448).
* Capture the intercepted feature path during patching to reuse during patch require (#2441).
* Update `Module#constants` to filter invalid constant identifiers (#2452).
* Fixed `-0.0 <=> 0.0` and `-0.0 <=> 0` to return `0` like on CRuby (#1391).

Compatibility:

* Implement `rb_sprintf` in our format compiler to provide consistent formatting across C standard libraries.
* Update `defined?` to return frozen strings (#2450).
* Use compensated summation for `{Array,Enumerable}#sum` when floating point values are included.
* `Module#attr_*` methods now return an array of method names (#2498, @gogainda).
* Fixed `Socket#(local|remote)_address` to retrieve family and type from the file descriptor (#2444, @larskanis).
* Add `Thread.ignore_deadlock` accessor (#2453).

Performance:

* Regexp objects are now interned in a similar way to symbols.
* Improve performance of regexps using POSIX bracket expressions (e.g., `[[:lower:]]`) matching against ASCII-only strings (#2447).
* `String#sub`, `sub!`, `gsub`, and `gsub!` have been refactored for better performance.

Changes:


# 21.3.0

New features:

* [TRegex](https://github.com/oracle/graal/tree/master/regex) is now used by default, which provides large speedups for matching regular expressions.
* Add `Polyglot.languages` to expose the list of available languages.
* Add `Polyglot::InnerContext` to eval code in any available language in an inner isolated context (#2169).
* Foreign objects now have a dynamically-generated class based on their interop traits like `ForeignArray` and are better integrated with Ruby objects (#2149).
* Foreign arrays now have all methods of Ruby `Enumerable` and many methods of `Array` (#2149).
* Foreign hashes now have all methods of Ruby `Enumerable` and many methods of `Hash` (#2149).
* Foreign iterables (`InteropLibrary#hasIterator`) now have all methods of Ruby `Enumerable` (#2149).
* Foreign objects now implement `#instance_variables` (readable non-invocable members) and `#methods` (invocable members + Ruby methods).

Bug fixes:

* Fix `Marshal.load` of multiple `Symbols` with an explicit encoding (#1624).
* Fix `rb_str_modify_expand` to preserve existing bytes (#2392).
* Fix `String#scrub` when replacement is frozen (#2398, @LillianZ).
* Fix `Dir.mkdir` error handling for `Pathname` paths (#2397).
* `BasicSocket#*_nonblock(exception: false)` now only return `:wait_readable/:wait_writable` for `EAGAIN`/`EWOULDBLOCK` like MRI (#2400).
* Fix issue with `strspn` used in the `date` C extension compiled as a macro on older glibc and then missing the `__strspn_c1` symbol on newer glibc (#2406).
* Fix constant lookup when loading the same file multiple times (#2408).
* Fix handling of `break`, `next` and `redo` in `define_method(name, &block)` methods (#2418).
* Fix handling of incompatible types in `Float#<=>` (#2432, @chrisseaton).
* Fix issue with escaping curly braces for `Dir.glob` (#2425).
* Fix `base64` decoding issue with missing output (#2435).
* Fix `StringIO#ungetbyte` to treat a byte as a byte, not a code point (#2436). 
* Fix `defined?(yield)` when used inside a block (#2446).
* Fix a couple issues related to native memory allocation and release.

Compatibility:

* Implement `Process::Status.wait` (#2378).
* Update `rb_str_modify` and `rb_str_modify_expand` to raise a `FrozenError` when given a frozen string (#2392).
* Implement `rb_fiber_*` functions (#2402).
* Implement `rb_str_vcatf`.
* Add support for tracing allocations from C functions (#2403, @chrisseaton).
* Implement `rb_str_catf`.
* Search the executable in the passed env `PATH` for subprocesses (#2419).
* Accept a string as the pattern argument to `StringScanner#scan` and `StringScanner#check` (#2423).

Performance:

* Moved most of `MonitorMixin` to primitives to deal with interrupts more efficiently (#2375).
* Improved the performance of `rb_enc_from_index` by adding cached lookups (#2379, @nirvdrum).
* Improved the performance of many `MatchData` operations (#2384, @nirvdrum).
* Significantly improved performance of TRegex calls by allowing Truffle splitting (#2389, @nirvdrum).
* Improved `String#gsub` performance by adding a fast path for the `string_byte_index` primitive (#2380, @nirvdrum).
* Improved `String#index` performance by adding a fast path for the `string_character_index` primitive (#2383, @LillianZ).
* Optimized conversion of strings to integers if the string contained a numeric value (#2401, @nirvdrum).
* Use Truffle's `ContextThreadLocal` to speedup access to thread-local data.
* Provide a new fast path for `rb_backref*` and `rb_lastline*`functions from C extensions.

Changes:

* `foreign_object.class` on foreign objects is no longer special and uses `Kernel#class` (it used to return the `java.lang.Class` object for a Java type or `getMetaObject()`, but that is too incompatible with Ruby code).
* `Java.import name` imports a Java class in the enclosing module instead of always as a top-level constant.
* `foreign_object.keys` no longer returns members, use `foreign_object.instance_variables` or `foreign_object.methods` instead.
* `foreign_object.respond_to?(:class)` is now always true (before it was only for Java classes), since the method is always defined.

Security:

* Updated to Ruby 2.7.4 to fix CVE-2021-31810, CVE-2021-32066 and CVE-2021-31799.

# 21.2.0

New features:

* New `TruffleRuby::ConcurrentMap` data structure for use in [`concurrent-ruby`](https://github.com/ruby-concurrency/concurrent-ruby) (#2339, @wildmaples).

Bug fixes:

* Fix of different values of self in different scopes.
* `Truffle::POSIX.select` was being redefined repeatedly (#2332).
* Fix the `--backtraces-raise` and `--backtraces-rescue` options in JVM mode (#2335).
* Fix `File.{atime, mtime, ctime}` to include nanoseconds (#2337).
* Fix `Array#[a, b] = "frozen string literal".freeze` (#2355).
* `rb_funcall()` now releases the C-extension lock (similar to MRI).

Compatibility:

* Updated to Ruby 2.7.3. The `resolv` stdlib was not updated (`resolv` in 2.7.3 has [bugs](https://bugs.ruby-lang.org/issues/17748)).
* Make interpolated strings frozen for compatibility with Ruby 2.7 (#2304, @kirs).
* `require 'socket'` now also requires `'io/wait'` like CRuby (#2326).
* Support precision when formatting strings (#2281, @kirs).
* Make rpartition compatible with Ruby 2.7 (#2320, @gogainda).
* Include the type name in exception messages from `rb_check_type` (#2307).
* Fix `Hash#rehash` to remove duplicate keys after modifications (#2266, @MattAlp)
* Only fail `rb_check_type` for typed data, not wrapped untyped structs (#2331).
* Decide the visibility in `Module#define_method` based on `self` and the default definee (#2334).
* Configure `mandir` value in `RbConfig::CONFIG` and `RbConfig::MAKEFILE_CONFIG` (#2315).
* TruffleRuby now supports the Truffle polyglot Hash interop API.
* Implement `Fiber#raise` (#2338).
* Update `File.basename` to return new `String` instances (#2343).
* Allow `Fiber#raise` after `Fiber#transfer` like Ruby 3.0 (#2342).
* Fix `ObjectSpace._id2ref` for Symbols and frozen String literals (#2358).
* Implemented `Enumerator::Lazy#filter_map` (#2356).
* Fix LLVM toolchain issue on macOS 11.3 (#2352, [oracle/graal#3383](https://github.com/oracle/graal/issues/3383)).
* Implement `IO#set_encoding_by_bom` (#2372, pawandubey).
* Implemented `Enumerator::Lazy#with_index` (#2356).
* Implement `rb_backref_set`.
* Fix `Float#<=>` when comparing `Infinity` to other `#infinite?` values.
* Implement `date` library as a C extension to improve compatibility (#2344).

Performance:

* Make `#dig` iterative to make it faster and compile better for calls with 3+ arguments (#2301, @chrisseaton, @jantnovi).
* Make `Struct#dig` faster in interpreter by avoiding exceptions (#2306, @kirs).
* Reduce the number of AST nodes created for methods and blocks (#2261).
* Fiber-local variables are much faster now by using less synchronization.
* Improved the performance of the exceptional case of `String#chr` (#2318, @chrisseaton).
* Improved the performance of `IO#read_nonblock` when no data is available to be read.
* `TruffleSafepoint` is now used instead of custom logic, which no longer invalidates JITed code for guest safepoints (e.g., `Thread#{backtrace,raise,kill}`, `ObjectSpace`, etc)
* Significantly improved performance of `Time#strftime` for common formats (#2361, @wildmaples, @chrisseaton).
* Faster solution for lazy integer length (#2365, @lemire, @chrisseaton).
* Speedup `rb_funcallv*()` by directly unwrapping the C arguments array instead of going through a Ruby `Array` (#2089).
* Improved the performance of several `Truffle::RegexOperations` methods (#2374, @wildmapes, @nirvdrum).

Changes:

* `rb_iterate()` (deprecated since 1.9) no longer magically passes the block to `rb_funcall()`, use `rb_block_call()` instead.

Security:

* Updated to Ruby 2.7.3 to fix CVE-2021-28965 and CVE-2021-28966.

# 21.1.0

New features:

* Access to local variables of the interactive Binding via language bindings is now supported: `context.getBindings("ruby").putMember("my_var", 42);` (#2030).
* `VALUE`s in C extensions now expose the Ruby object when viewed in the debugger, as long as they have not been converted to native values.
* Signal handlers can now be run without triggering multi-threading.
* Fibers no longer trigger Truffle multi-threading.

Bug fixes:

* `Range#to_a` wasn't working for `long` ranges (#2198, @tomstuart and @LillianZ).
* Show the interleaved host and guest stacktrace for host exceptions (#2226).
* Fix the label of the first location reported by `Thread#backtrace_locations` (#2229).
* Fix `Thread.handle_interrupt` to defer non-pure interrupts until the end of the `handle_interrupt` block (#2219).
* Clear and restore errinfo on entry and normal return from methods in C extensions (#2227).
* Fix extra whitespace in squiggly heredoc with escaped newline (#2238, @wildmaples and @norswap).
* Fix handling of signals with `--single-threaded` (#2265).
* Fix `Enumerator::Lazy#{chunk_while, slice_before, slice_after, slice_when}` to return instances of `Enumerator::Lazy` (#2273).
* Fix `Truffle::Interop.source_location` to return unavailable source sections for modules instead of null (#2257).
* Fix usage of `Thread.handle_interrupt` in `MonitorMixin#mon_synchronize`.
* Fixed `TruffleRuby.synchronized` to handle guest safepoints (#2277).
* Fix control flow bug when assigning constants using ||= (#1489).
* Fix `Kernel#raise` argument handling for hashes (#2298).
* Set errinfo when `rb_protect` captures a Ruby exception (#2245).
* Fixed handling of multiple optional arguments and keywords when passed a positional `Hash` (#2302).

Compatibility:

* Prepend the GraalVM LLVM Toolchain to `PATH` when installing gems (#1974, #1088, #1343, #1400, #1947, #1931, #1588).
* Installing the `nokogiri` gem now defaults to use the vendored `libxml2` and `libxslt`, similar to CRuby, which means the corresponding system packages are no longer needed (#62).
* Implemented `$LOAD_PATH.resolve_feature_path`.
* Add `Pathname#/` alias to `Pathname#+` (#2178).
* Fixed issue with large `Integer`s in `Math.log` (#2184).
* Updated `Regexp.last_match` to support `Symbol` and `String` parameter (#2179).
* Added support for numbered block parameters (`_1` etc).
* Fixed `String#upto` issue with non-ascii strings (#2183).
* Implemented partial support for pattern matching (#2186).
* Make `File.extname` return `'.'` if the path ends with one (#2192, @tomstuart).
* Include fractional seconds in `Time#inspect` output (#2194, @tomstuart).
* Add support for `Integer#[Range]` and `Integer#[start, length]` (#2182, @gogainda).
* Allow private calls with `self` as an explicit receiver (#2196, @wildmaples).
* Fixed `:perm` parameter for `File.write`.
* Implemented `Time#floor` and `#ceil` (#2201, @wildmaples).
* Allow `Range#include?` and `#member?` with `Time` (#2202, @wildmaples).
* Implemented `Comparable#clamp(Range)` (#2200, @wildmaples).
* Added a `Array#minmax` to override `Enumerable#minmax` (#2199, @wildmaples).
* Implemented `chomp` parameter for `IO.{readlines, foreach}` (#2205).
* Implemented the Debug Inspector C API.
* Added beginless range support for `Range#{new, bsearch, count, each, equal_value, first, inspect, max, min, size, cover?, include?, ===}`.
* Added beginless range support for `Array#{[], []=, slice, slice!, to_a, fill, values_at}` (#2155, @LillianZ).
* Added beginless range support for `String#{byteslice, slice, slice!}` and `Symbol#slice` (#2211, @LillianZ).
* Added beginless range support for `Kernel#{caller, caller_locations}` and `Thread#backtrace_locations` (#2211, @LillianZ).
* Make rand work with exclusive range with Float (#1506, @gogainda)
* Fixed `String#dump`'s formatting of escaped unicode characters (#2217, @meganniu).
* Switched to the io-console C extension from C ruby for better performance and compatibility in `irb`.
* Coerce the message to a `String` for `BasicSocket#send` (#2209, @HoneyryderChuck).
* Support buffer argument for `UDPSocket#recvfrom_nonblock` (#2209, @HoneyryderChuck).
* Fixed `Integer#digits` implementation to handle more bases (#2224, #2225).
* Support the `inherit` parameter for `Module#{private, protected, public}_method_defined?`.
* Implement `Thread.pending_interrupt?` and `Thread#pending_interrupt?` (#2219).
* Implement `rb_lastline_set` (#2170).
* Implemented `Module#const_source_location` (#2212, @tomstuart and @wildmaples).
* Do not call `File.exist?` in `Dir.glob` as `File.exist?` is often mocked (#2236, @gogainda).
* Coerce the inherit argument to a boolean in `Module#const_defined?` and `Module#const_get` (#2240).
* Refinements take place at `Object#method` and `Module#instance_method` (#2004, @ssnickolay).
* Add support for `rb_scan_args_kw` in C API (#2244, @LillianZ).
* Update random implementation layout to be more compatible (#2234).
* Set `RbConfig::CONFIG['LIBPATHFLAG'/'RPATHFLAG']` like MRI to let `$LIBPATH` changes in `extconf.rb` work.
* Access to path and mode via `rb_io_t` from C has been changed to improve compatibility for io-console.
* Implemented the `Time.at` `in:` parameter.
* Implemented `Kernel#raise` `cause` parameter.
* Improved compatibility of `Signal.trap` and `Kernel#trap` (#2287, @chrisseaton).
* Implemented `GC.stat(:total_allocated_objects)` as `0` (#2292, @chrisseaton).
* `ObjectSpace::WeakMap` now supports immediate and frozen values as both keys and values (#2267).
* Call `divmod` when coercion to `Float` fails for `#sleep` (#2289, @LillianZ).

Performance:

* Multi-Tier compilation is now enabled by default, which improves warmup significantly.
* Improve the performance of checks for recursion (#2189, @LillianZ).
* Improve random number generation performance by avoiding synchronization (#2190, @ivoanjo).
* We now create a single call target per block by default instead of two.
* Some uses of class variables are now much better optimized (#2259, @chrisseaton).
* Several methods that need the caller frame are now always inlined in their caller, which speeds up the interpreter and reduces footprint.
* Pasting code in IRB should be reasonably fast, by updating to `irb` 1.3.3 and `reline` 0.2.3 (#2233).

Changes:

* Standalone builds of TruffleRuby are now based on JDK11 (they used JDK8 previously). There should be no user-visible changes. Similarly, JDK11 is now used by default in development instead of JDK8.
* The deprecated `Truffle::System.synchronized` has been removed.
* `Java.synchronized` has been removed, it did not work on host objects.

# 21.0.0

Release notes:

* The new IRB is quite slow when copy/pasting code into it. This is due to an inefficient `io/console` implementation which will be addressed in the next release. A workaround is to use `irb --readline`, which disables some IRB features but is much faster for copy/pasting code.

New features:

* Updated to Ruby 2.7.2 (#2004).

Bug fixes:

* Fix error message when the method name is not a Symbol or String for `Kernel#respond_to?` (#2132, @ssnickolay)
* Fixed setting of special variables in enumerators and enumerables (#1484).
* Fixed return value of `Enumerable#count` and `Enumerable#uniq` with multiple yielded arguments (#2145, @LillianZ).
* Fixed `String#unpack` for `w*` format (#2143).
* Fixed issue with ``Kernel#` `` when invalid UTF-8 given (#2118).
* Fixed issue with `Method#to_proc` and special variable storage (#2156).
* Add missing `offset` parameter for `FFI::Pointer#put_array_of_*` (#1525).
* Fixed issue with different `Struct`s having the same hash values (#2214).

Compatibility:

* Implement `String#undump` (#2131, @kustosz)
* `Errno` constants with the same `errno` number are now the same class.
* Implement `Enumerable#tally` and `Enumerable#filter_map` (#2144 and #2152, @LillianZ).
* Implement `Range#minmax`.
* Pass more `Enumerator::Lazy#uniq` and `Enumerator::Lazy#chunk` specs (#2146, @LillianZ).
* Implement `Enumerator#produce` (#2160, @zverok)
* Implement `Complex#<=>` (#2004, @ssnickolay).
* Add warning for `proc` without block (#2004, @ssnickolay).
* Implemented `FrozenError#receiver`.
* `Proc#<<` and `Proc#>>` raises TypeError if passed not callable object (#2004, @ssnickolay).
* Support time and date related messages for `Time` (#2166).
* Updated `Dir.{glob,[]}` to raise `ArgumentError` for nul-separated strings.
* `Kernel#lambda` with no block in a method called with a block raises an exception (#2004, @ssnickolay).
* Implemented `BigDecimal` as C extension to improve compatibility.
* Comment lines can be placed between fluent dot now (#2004, @ssnickolay).
* Implemented `rb_make_exception`.
* `**kwargs` now accept non-Symbol keys like Ruby 2.7.
* Updated the Unicode Emoji version (#2173, @wildmaples).
* Added `Enumerator::Yielder#to_proc`.
* Implemented `Enumerator::Lazy#eager`.
* Updated `Method#inspect` to include paremeter information.
* Update `Module#name` to return the same frozen string.
* Implemented `inherit` argument for `Module#autoload?`.

Performance:

* Refactor and implement more performant `MatchData#length` (#2147, @LillianZ).
* Refactor and implement more performant `Array#sample` (#2148, @LillianZ).
* `String#inspect` is now more efficient.

Changes:

* All `InteropLibrary` messages are now exposed consistently as methods on `Truffle::Interop` (#2139). Some methods were renamed to match the scheme described in the documentation.

# 20.3.0

Bug fixes:

* Handle foreign null object as falsy value (#1902, @ssnickolay)
* Fixed return value of `Enumerable#first` with multiple yielded arguments (#2056, @LillianZ).
* Improve reliability of the post install hook by disabling RubyGems (#2075).
* Fixed top level exception handler to print exception cause (#2013).
* Fixed issue when extending FFI from File (#2094).
* Fixed issue with `Kernel#freeze` not freezing singleton class (#2093).
* Fixed `String#encode` with options issue (#2091, #2095, @LillianZ)
* Fixed issue with `spawn` when `:close` redirect is used (#2097).
* Fixed `coverage` issue when `*eval` is used (#2078).
* Use expanded load paths for feature matching (#1501).
* Fixed handling of post arguments for `super()` (#2111).
* Fixed `SystemStackError` sometimes replaced by an internal Java `NoClassDefFoundError` on JVM (#1743).
* Fixed constant/identifier detection in lexer for non-ASCII encodings (#2079, #2102, @ivoanjo).
* Fixed parsing of `--jvm` as an application argument (#2108).
* Fix `rb_rescue2` to ignore the end marker `(VALUE)0` (#2127, #2130).
* Fix status and output when SystemExit is subclassed and raised (#2128)
* Fix `String#{chomp, chomp!}` issue with invalid encoded strings (#2133).

Compatibility:

* Run `at_exit` handlers even if parsing the main script fails (#2047).
* Load required libraries (`-r`) before parsing the main script (#2047).
* `String#split` supports block (#2052, @ssnickolay)
* Implemented `String#{grapheme_clusters, each_grapheme_cluster}`.
* Fix the caller location for `#method_added` (#2059).
* Fix issue with `Float#round` when `self` is `-0.0`.
* Fix `String#unpack` issue with `m0` format (#2065).
* Fix issue with `File.absolute_path` returning a path to current directory (#2062).
* Update `Range#cover?` to handle `Range` parameter.
* Fix `String#{casecmp, casecmp?}` parameter conversion.
* Fix `Regexp` issue which raised syntax error instead of `RegexpError` (#2066).
* Handle `Object#autoload` when autoload itself (#1616, @ssnickolay)
* Skip upgraded default gems while loading RubyGems (#2075).
* Verify that gem paths are correct before loading RubyGems (#2075).
* Implement `rb_ivar_count`.
* Implemented `rb_yield_values2`.
* Implemented `Digest::Base#{update, <<}` (#2100).
* Pass the final `super` specs (#2104, @chrisseaton).
* Fix arity for arguments with optional kwargs (#1669, @ssnickolay)
* Fix arity for `Proc` (#2098, @ssnickolay)
* Check bounds for `FFI::Pointer` accesses when the size of the memory behind is known.
* Implement negative line numbers for eval (#1482).
* Support refinements for `#to_s` called by string interpolation (#2110, @ssnickolay)
* Module#using raises error in method scope (#2112, @ssnickolay)
* `File#path` now returns a new mutable String on every call like MRI (#2115).
* Avoid infinite recursion when redefining `Warning#warn` and calling `Kernel#warn` (#2109).
* Convert objects with `#to_path` in `$LOAD_PATH` (#2119).
* Handle the functions being native for `rb_thread_call_without_gvl()` (#2090).
* Support refinements for Kernel#respond_to? (#2120, @ssnickolay)
* JCodings has been updated from 1.0.45 to 1.0.55.
* Joni has been updated from 2.1.30 to 2.1.40.

Performance:

* Calls with a literal block are no longer always split but instead the decision is made by the Truffle splitting heuristic.
* `Symbol#to_proc` is now AST-inlined in order to not rely on splitting and to avoid needing the caller frame to find refinements which apply.
* `Symbol#to_proc` is now globally cached per Symbol and refinements, to avoid creating many redundant `CallTargets`.
* Setting and access to the special variables `$~` and `$_` has been refactored to require less splitting.

Changes:

* Migrated from JLine 2 to JLine 3 for the `readline` standard library.

# 20.2.0

New features:

* Updated to Ruby 2.6.6.
* Use `InteropLibrary#toDisplayString()` to better display objects from other languages.
* Implement writing to the top scope for global variables (#2024).
* `foreign_object.to_s` now uses `InteropLibrary#toDisplayString()` (and still `asString()` if `isString()`).
* `foreign_object.inspect` has been improved to be more useful (include the language and meta object).
* `foreign_object.class` now calls `getMetaObject()` (except for Java classes, same as before).
* Add basic support for Linux ARM64.
* `foreign_object.name = value` will now call `Interoplibrary#writeMember("name", value)` instead of `invokeMember("name=", value)`.
* Always show the Ruby core library files in backtraces (#1414).
* The Java stacktrace is now shown when sending SIGQUIT to the process, also on TruffleRuby Native, see [Debugging](doc/user/debugging.md) for details (#2041).
* Calls to foreign objects with a block argument will now pass the block as the last argument.
* `foreign.name` will now use `invokeMember` if invocable and if not use `readMember`, see `doc/contrib/interop_implicit_api.md` for details.
* `foreign.to_f` and `foreign.to_i` will now attempt to convert to Ruby `Float` and `Integer` (#2038).
* `foreign.equal?(other)` now uses `InteropLibrary#isIdentical(other)` and `foreign.object_id/__id__` now uses `InteropLibrary#identityHashCode()`.

Bug fixes:

* Fix `#class_exec`, `#module_exec`, `#instance_eval`, and `instance_exec` to use activated refinements (#1988, @ssnickolay).
* Fixed missing method error for FFI calls with `blocking: true` when interrupted.
* Use upgraded default gems when installed (#1956).
* Fixed `NameError` when requiring an autoload path that does not define the autoload constant (#1905).
* Thread local IO buffers are now allocated using a stack to ensure safe operating if a signal handler uses one during an IO operation.
* Fixed `TracePoint` thread-safety by storing the state on the Ruby `Thread` (like MRI) instead of inside the `TracePoint` instance.
* Make `require 'rubygems/package'` succeed and define `Gem::Deprecate` correctly (#2014).
* Fix `MBCLEN_CHARFOUND_P` error.
* Fix `rb_enc_str_new` when `NULL` encoding is given with a constant string.
* Fixed `rb_enc_precise_mbclen` to handle more inputs.
* The output for `--engine.TraceCompilation` is now significantly easier to read, by having shorter method names and source names (oracle/graal#2052).
* Fix indentation for squiggly heredoc with single quotes (#1564).
* Only print members which are readable for foreign `#inspect` (#2027).
* Fixed the return value of the first call to `Kernel#srand` in a Thread (#2028).
* Fix missing flushing when printing an exception at top-level with a custom backtrace, which caused no output being shown (#1750, #1895).
* Use the mode of the given `IO` for `IO#reopen(IO)` which is important for the 3 standard IOs (#2034).
* Fix potential deadlock when running finalizers (#2041).
* Let `require 'rubygems/specification'` work before `require 'rubygems'`.

Compatibility:

* Implement `UnboundMethod#bind_call`.
* Implemented `ObjectSpace::WeakMap` (#1385, #1958).
* Implemented `strtod` and `ruby_strtod` (#2007).
* Fix detection of `#find_type` in FFI to ignore `MakeMakefile#find_type` from `mkmf` (#1896, #2010).
* Implemented `rb_uv_to_utf8` (#1998, @skateman).
* Implemented `rb_str_cat_cstr`.
* Implemented `rb_fstring`.
* Support `#refine` for Module (#2021, @ssnickolay).
* Implemented `rb_ident_hash_new`.
* Improved the compatibility of `Symbol.all_symbols` (#2022, @chrisseaton).
* Implemented `rb_enc_str_buf_cat`.
* Implemented `rb_int_positive_pow`.
* Implemented `rb_usascii_str_new_lit`.
* Define `#getch` and `#getpass` on `StringIO` when `io/console` is required.
* Implemented `rb_uv_to_utf8` (#1998).
* Single character IDs now behave more like those in MRI to improve C extension compatibility, so `rb_funcall(a, '+', b)` will now do the same thing as in MRI.
* Removed extra public methods on `String`.
* Implemented `rb_array_sort` and `rb_array_sort_bang`.
* Do not create a finalizers `Thread` if there are other public languages, which is helpful for polyglot cases (#2035).
* Implemented `rb_enc_isalnum` and `rb_enc_isspace`.
* `RUBY_REVISION` is now the full commit hash used to build TruffleRuby, similar to MRI 2.7+.
* Implemented `rb_enc_mbc_to_codepoint`.
* Changed the lookup methods to achieve Refinements specification (#2033, @ssnickolay)
* Implemented `Digest::Instance#new` (#2040).
* Implemented `ONIGENC_MBC_CASE_FOLD`.
* Fixed `Thread#raise` to call the exception class' constructor with no arguments when given no message (#2045).
* Fixed `refine + super` compatibility (#2039, #2048, @ssnickolay)
* Make the top-level exception handler more compatible with MRI (#2047).
* Implemented `rb_enc_codelen`.
* Implemented `Ripper` by using the C extension (#1585).

Changes:

* RubyGems gem commands updated to use the `--no-document` option by default.

Performance:

* Enable lazy translation from the parser AST to the Truffle AST for user code by default. This should improve application startup time (#1992).
* `instance variable ... not initialized` and similar warnings are now optimized to have no peak performance impact if they are not printed (depends on `$VERBOSE`).
* Implement integer modular exponentiation using `BigInteger#mod_pow` (#1999, @skateman)
* Fixed a performance issue when computing many substrings of a given non-leaf `String` with non-US-ASCII characters.
* Speedup native handle to Ruby object lookup for C extensions.

# 20.1.0

New features:

* Nightly builds of TruffleRuby are now available, see the README for details (#1483).
* `||=` will not compile the right-hand-side if it's only executed once, to match the idiomatic lazy-initialisation use-case ([blog post](https://engineering.shopify.com/blogs/engineering/optimizing-ruby-lazy-initialization-in-truffleruby-with-deoptimization), #1887, @kipply).
* Added `--metrics-profile-require` option to profile searching, parsing, translating and loading files.
* Added support for captured variables for the Truffle instruments (e.g. Chrome debugger).

Bug fixes:

* Fixed `Exception#dup` to copy the `Exception#backtrace` string array.
* Fixed `rb_warn` and `rb_warning` when used as statements (#1886, @chrisseaton).
* Fixed `NameError.new` and `NoMethodError.new` `:receiver` argument.
* Correctly handle large numbers of arguments to `rb_funcall` (#1882).
* Added arity check to `Module#{include, prepend}`.
* Fix `OpenSSL::Digest.{digest,hexdigest,base64digest}` to handle `algorithm, data` arguments (#1889, @bdewater).
* Fixed `SystemCallError.new` parameter conversion.
* Fixed `File#{chmod, umask}` argument conversion check.
* Added warning in `Hash.[]` for non-array elements.
* Fixed `File.lchmod` to raise `NotImplementedError` when not available.
* `RSTRING_PTR()` now always returns a native pointer, resolving two bugs `memcpy`ing to (#1822) and from (#1772) Ruby Strings.
* Fixed issue with duping during splat (#1883).
* Fixed `Dir#children` implementation.
* Fixed `SignalException.new` error when bad parameter given.
* Added deprecation warning to `Kernel#=~`.
* Fixed `puts` for a foreign objects, e.g. `puts Polyglot.eval('js', '[]')` (#1881).
* Fixed `Exception#full_message` implementation.
* Updated `Kernel.Complex()` to handle the `exception: false` parameter.
* Fixed `Kernel#dup` to return self for `Complex` and `Rational` objects.
* Updated `Kernel.Float()` to handle the `exception: false` parameter.
* Fixed `String#unpack` `M` format (#1901).
* Fixed error when `SystemCallError` message contained non-ASCII characters.
* Fixed `rb_rescue` to allow null rescue methods. (#1909, @kipply).
* Fixed incorrect comparisons between bignums and doubles.
* Prevented some internal uses of `Kernel#caller_locations` to be overridden by user code (#1934).
* Fixed an issue caused by recursing inlining within `Regexp#quote` (#1927).
* Updated `Kernel.Float()` to return given string in error message (#1945).
* Parameters and arity of methods derived from `method_missing` should now match MRI (#1921).
* Fixed compile error in `RB_FLOAT_TYPE_P` macro (#1928).
* Fixed `Symbol#match` to call the block with the `MatchData` (#1933).
* Fixed `Digest::SHA2.hexdigest` error with long messages (#1922).
* Fixed `Date.parse` to dup the coerced string to not modify original (#1946).
* Update `Comparable` error messages for special constant values (#1941).
* Fixed `File.ftype` parameter conversion (#1961).
* Fixed `Digest::Instance#file` to not modify string literals (#1964).
* Make sure that string interpolation returns a `String`, and not a subclass (#1950).
* `alias_method` and `instance_methods` should now work correctly inside a refinement (#1942).
* Fixed `Regexp.union` parameter conversion (#1963).
* `IO#read(n)` no longer buffers more than needed, which could cause hanging if detecting readability via a native call such as `select(2)` (#1951).
* Fixed `Random::DEFAULT.seed` to be different on boot (#1965, @kipply)
* `rb_encoding->name` can now be read even if the `rb_encoding` is stored in native memory.
* Detect and cut off recursion when inspecting a foreign object, substituting an ellipsis instead.
* Fixed feature lookup order to check every `$LOAD_PATH` path entry for `.rb`, then every entry for native extension when `require` is called with no extension.
* Define the `_DARWIN_C_SOURCE` macro in extension makefiles (#1592).
* Change handling of var args in `rb_rescue2` to handle usage in C extensions (#1823).
* Fixed incorrect `Encoding::CompatibilityError` raised for some interpolated Regexps (#1967).
* Actually unset environment variables with a `nil` value for `Process.spawn` instead of setting them to an empty String.
* Core library methods part of the Native Image heap are no longer added in the compilation queue on the first call, but after they reach the thresholds like other methods.
* Fix `RbConfig::CONFIG['LIBRUBY_SO']` file extension.
* Fix `char`, `short`, `unsigned char`,  `unsigned int`, and `unsigned short` types in `Fiddle` (#1971).
* Fix `IO#select` to reallocate its buffer if it is interrupted by a signal.
* Fix issue where interpolated string matched `#` within string as being a variable (#1495).
* Fix `File.join` to raise error on strings with null bytes.
* Fix initialization of Ruby Thread for foreign thread created in Java.
* Fix registration of default specs in RubyGems (#1987).

Compatibility:

* The C API type `VALUE` is now defined as `unsigned long` as on MRI. This enables `switch (VALUE)` and other expressions which rely on `VALUE` being an integer type (#1409, #1541, #1675, #1917, #1954).
* Implemented `Float#{floor, ceil}` with `ndigits` argument.
* Implemented `Thread#fetch`.
* Implemented `Float#truncate` with `ndigits` argument.
* Made `String#{byteslice, slice, slice!}` and `Symbol#slice` compatible with endless ranges.
* Implemented "instance variable not initialized" warning.
* Make `Kernel#{caller, caller_locations}` and `Thread#backtrace_locations` compatible with endless ranges.
* Implemented `Dir#each_child`.
* Implemented `Kernel.{chomp, chop}` and `Kernel#{chomp, chop}`.
* Implemented `-p` and `-a`, and `-l` CLI options.
* Convert the argument to `File.realpath` with `#to_path` (#1894).
* `StringIO#binmode` now sets the external encoding to BINARY like MRI (#1898).
* `StringIO#inspect` should not include the contents of the `StringIO` (#1898).
* Implemented `rb_fd_*` functions (#1623).
* Fixed uninitialized variable warnings in core and lib (#1897).
* Make `Thread#backtrace` support omit, length and range arguments.
* Implemented `Range#%`.
* Fixed the type of the `flags` field of `rb_data_type_t` (#1911).
* Implemented `rb_obj_is_proc` (#1908, @kipply, @XrXr).
* Implemented C API macro `RARRAY_ASET()`.
* Implemented `num2short` (#1910, @kipply).
* `RSTRING_END()` now always returns a native pointer.
* Removed `register` specifier for `rb_mem_clear()` (#1924).
* Implemented `Thread::Backtrace::Locations#base_label` (#1920).
* Implemented `rb_mProcess` (#1936).
* Implemented `rb_gc_latest_gc_info` (#1937).
* Implemented `RBASIC_CLASS` (#1935).
* Yield 2 arguments for `Hash#map` if the arity of the block is > 1 (#1944).
* Add all `Errno` constants to match MRI, needed by recent RubyGems.
* Silence `ruby_dep` warnings since that gem is unmaintained.
* Clarify error message for not implemented `Process.daemon` (#1962).
* Allow multiple assignments in conditionals (#1513).
* Update `NoMethodError#message` to match MRI (#1957).
* Make `StringIO` work with `--enable-frozen-string-literal` (#1969).
* Support `NULL` for the status of `rb_protect()`.
* Ensure `BigDecimal#inspect` does not call `BigDecimal#to_s` to avoid behaviour change on `to_s` override (#1960).
* Define all C-API `rb_{c,m,e}*` constants as C global variables (#1541).
* Raise `ArgumentError` for `Socket.unpack_sockaddr_un` if the socket family is incorrect.
* Implemented `RTYPEDDATA_*()` macros and `rb_str_tmp_new()` (#1975).
* Implemented `rb_set_end_proc` (#1959).
* Implemented `rb_to_symbol`.
* Implemented `rb_class_instance_methods`, `rb_class_public_instance_methods`, `rb_class_protected_instance_methods`, and `rb_class_private_instance_methods`.
* Implemented `rb_tracepoint_new`, `rb_tracepoint_disable`, `rb_tracepoint_enable`, and `rb_tracepoint_enabled_p` (#1450).
* Implemented `RbConfig::CONFIG['AR']` and `RbConfig::CONFIG['STRIP']` (#1973).
* Not yet implemented C API functions are now correctly detected as missing via `mkmf`'s `have_func` (#1980).
* Accept `RUBY_INTERNAL_EVENT_{NEWOBJ,FREEOBJ}` events but warn they are not triggered (#1978, #1983).
* `IO.copy_stream(in, STDOUT)` now writes to `STDOUT` without buffering like MRI.
* Implemented `RbConfig['vendordir']`.
* Implemented `Enumerator::ArithmeticSequence`.
* Support `(struct RBasic *)->flags` and `->klass` from `ruby.h` (#1891, #1884, #1978).

Changes:

* `TRUFFLERUBY_RESILIENT_GEM_HOME` has been removed. Unset `GEM_HOME` and `GEM_PATH` instead if you need to.
* The deprecated `Truffle::System.full_memory_barrier`, `Truffle::Primitive.logical_processors`, and  `Truffle::AtomicReference` have been removed.
* The implicit interface for allowing Ruby objects to behave as polyglot arrays with `#size`, `#[]` methods has been removed and replaced with an explicit interface where each method starts with `polyglot_*`.
* Hash keys are no longer reported as polyglot members.
* All remaining implicit polyglot behaviour for `#[]` method was replaced with `polyglot_*` methods.
* Rename dynamic API to match InteropLibrary. All the methods keep the name as it is in InteropLibrary with the following changes: use snake_case, add `polyglot_` prefix, drop `get` and `is` prefix, append `?` on all predicates.
* Split `Truffle::Interop.write` into `.write_array_element` and `.write_member` methods.
* Rename `Truffle::Interop.size` to `.array_size`.
* Rename `Truffle::Interop.is_boolean?` to `.boolean?`.
* Split `Truffle::Interop.read` into `.read_member` and `.read_array_element`.
* Drop `is_` prefix in `Truffle::Interop.is_array_element_*` predicates.
* `Truffle::Interop.hash_keys_as_members` has been added to treat a Ruby Hash as a polyglot object with the Hash keys as members.

Performance:

* Optimized `RSTRING_PTR()` accesses by going to native directly, optimized various core methods, use Mode=latency and tune GC heap size for Bundler. This speeds up `bundle install` from 84s to 19s for a small Gemfile with 6 gems (#1398).
* Fixed memory footprint issue due to large compilation on Native Image, notably during `bundle install` (#1893).
* `ArrayBuilderNode` now uses a new Truffle library for manipulating array stores.
* Ruby objects passed to C extensions are now converted less often to native handles.
* Calling blocking system calls and running C code with unblocking actions has been refactored to remove some optimisation boundaries.
* `return` expressions are now rewritten as implicit return expressions where control flow allows this to be safely done as a tail optimisation. This can improve interpreter performance by up to 50% in some benchmarks, and can be applied to approximately 80% of return nodes seen in Rails and its dependencies (#1977).
* The old array strategy code has been removed and all remaining nodes converted to the new `ArrayStoreLibrary`.
* Updated `nil` to be a global immutable singleton (#1835).

# 20.0.0

New features:

* Enable and document `--coverage` option (#1840, @chrisseaton).
* Update the internal LLVM toolchain to LLVM 9 and reduce its download size.
* Updated to Ruby 2.6.5 (#1749).
* Automatically set `PKG_CONFIG_PATH` as needed for compiling OpenSSL on macOS (#1830).

Bug fixes:

* Fix `Tempfile#{size,length}` when the IO is not flushed (#1765, @rafaelfranca).
* Dump and load instance variables in subclasses of `Exception` (#1766, @rafaelfranca).
* Fix `Date._iso8601` and `Date._rfc3339` when the string is an invalid date (#1773, @rafaelfranca).
* Fail earlier for bad handle unwrapping (#1777, @chrisseaton).
* Match out of range `ArgumentError` message with MRI (#1774, @rafaelfranca).
* Raise `Encoding::CompatibilityError` with incompatible encodings on `Regexp` (#1775, @rafaelfranca).
* Fixed interactions between attributes and instance variables in `Struct` (#1776, @chrisseaton).
* Coercion fixes for `TCPServer.new` (#1780, @XrXr).
* Fix `Float#<=>` not calling `coerce` when `other` argument responds to it (#1783, @XrXr).
* Do not warn / crash when requiring a file that sets and trigger autoload on itself (#1779, @XrXr).
* Strip trailing whitespaces when creating a `BigDecimal` with a `String` (#1796, @XrXr).
* Default `close_others` in `Process.exec` to `false` like Ruby 2.6 (#1798, @XrXr).
* Don't clone methods when setting method to the same visibility (#1794, @XrXr).
* `BigDecimal()` deal with large rationals precisely (#1797, @XrXr).
* Make it possible to call `instance_exec` with `rb_block_call` (#1802, @XrXr).
* Check for duplicate members in `Struct.new` (#1803, @XrXr).
* `Process::Status#to_i` return raw `waitpid(2)` status (#1800, @XrXr).
* `Process#exec`: set close-on-exec to false for fd redirection (#1805, @XrXr, @rafaelfranca).
* Building C extensions should now work with frozen string literals (#1786).
* Keep the Truffle working directory in sync with the native working directory.
* Rename `to_native` to `polyglot_to_native` to match `polyglot_pointer?` and `polyglot_address` methods.
* Fixed missing partial evaluation boundary in `Array#{sort,sort!}` (#1727).
* Fixed the class of `self` and the wrapping `Module` for `Kernel#load(path, wrap=true)` (#1739).
* Fixed missing polyglot type declaration for `RSTRING_PTR` to help with native/managed interop.
* Fixed `Module#to_s` and `Module#inspect` to not return an extra `#<Class:` for singleton classes.
* Arrays backed by native storage now allocate the correct amount of memory (#1828).
* Fixed issue in `ConditionVariable#wait` that could lose a `ConditionVariable#signal`.
* Do not expose TruffleRuby-specific method `Array#swap` (#1816).
* Fixed `#inspect` on broken UTF-8 sequences (#1842, @chrisseaton).
* `Truffle::Interop.keys` should report methods of `String` and `Symbol` (#1817).
* `Kernel#sprintf` encoding validity has been fixed (#1852, @XrXr).
* Fixed `ArrayIndexOutOfBoundsException` in `File.fnmatch` (#1845).
* Make `String#concat` work with no or multiple arguments (#1519).
* Make `Array#concat` work with no or multiple arguments (#1519).
* Coerce `BigDecimal(arg)` using `to_str` (#1826).
* Fixed `NameError#dup`, `NoMethodError#dup`, and `SystemCallError#dup` to copy internal fields.
* Make `Enumerable#chunk` work without a block (#1518).
* Fixed issue with `SystemCallError.new` setting a backtrace too early.
* Fixed `BigDecimal#to_s` formatting issue (#1711).
* Run `END` keyword block only once at exit.
* Implement `Numeric#clone` to return `self`.
* Fixed `Symbol#to_proc` to create a `Proc` with `nil` `source_location` (#1663).
* Make `GC.start` work with keyword arguments.
* Fixed `Kernel#clone` for `nil`, `true`, `false`, `Integer`, and `Symbol`.
* Make top-level methods available in `Context#getBindings()` (#1838).
* Made `Kernel#caller_locations` accept a range argument, and return `nil` when appropriate.
* Made `rb_respond_to` work with primitives (#1869, @chrisseaton).
* Fixed issue with missing backtrace for `rescue $ERROR_INFO` (#1660).
* Fixed `Struct#hash` for `keyword_init: true` `Struct`.
* Fixed `String#{upcase!,downcase!,swapcase!}(:ascii)` for non-ASCII-compatible encodings like UTF-16.
* Fixed `String#capitalize!` for strings that weren't full ASCII.
* Fixed enumeration issue in `ENV.{select, filter}`.
* Fixed `Complex` and `Rational` should be frozen after initializing.
* Fixed `printf` should raise error when not enough arguments for positional argument.
* Removed "shadowing outer local variable" warning.
* Fixed parameter conversion to `String` in ENV methods.
* Fixed deprecation warning when `ENV.index` is called.
* Fixed issue with `ENV.each_key`.
* Fixed `ENV.replace` implementation.
* Fixed `ENV.udpate` implementation.
* Fixed argument handling in `Kernel.printf`.
* Fixed character length after conversion to binary from a non-US-ASCII String.
* Fixed issue with installing latest bundler (#1880).
* Fixed type conversion for `Numeric#step` `step` parameter.
* Fixed `Kernel#Integer` conversion.
* Fixed `IO.try_convert` parameter conversion.
* Fixed linking of always-inline C API functions with `-std=gnu90` (#1837, #1879).
* Avoid race conditions during `gem install` by using a single download thread.
* Do not use gems precompiled for MRI on TruffleRuby (#1837).
* Fixed printing foreign arrays that were also pointers (#1679).
* Fixed `nil#=~` to not warn.
* Fixed `Enumerable#collect` to give user block arity in the block passed to `Enumerable#each`.

Compatibility:

* Implemented `String#start_with?(Regexp)` (#1771, @zhublik).
* Various improvements to `SignalException` and signal handling (#1790, @XrXr).
* Implemented `rb_utf8_str_new`, `rb_utf8_str_new_cstr`, `rb_utf8_str_new_static` (#1788, @chrisseaton).
* Implemented the `unit` argument of `Time.at` (#1791, @XrXr).
* Implemented `keyword_init: true` for `Struct.new` (#1789, @XrXr).
* Implemented `MatchData#dup` (#1792, @XrXr).
* Implemented a native storage strategy for `Array` to allow better C extension compatibility.
* Implemented `rb_check_symbol_cstr` (#1814).
* Implemented `rb_hash_start` (#1841, @XrXr).
* JCodings has been updated from 1.0.42 to 1.0.45.
* Joni has been updated from 2.1.25 to 2.1.30.
* Implemented `Method#<<` and `Method#>>` (#1821).
* The `.bundle` file extension is now used for C extensions on macOS (#1819, #1837).
* Implemented `Comparable#clamp` (#1517).
* Implemented `rb_gc_register_mark_object` and `rb_enc_str_asciionly_p` (#1856, @chrisseaton).
* Implemented `rb_io_set_nonblock` (#1741).
* Include the major kernel version in `RUBY_PLATFORM` on macOS like MRI (#1860, @eightbitraptor).
* Implemented `Enumerator::Chain`, `Enumerator#+`, and `Enumerable#chain` (#1859, #1858).
* Implemented `Thread#backtrace_locations` and `Exception#backtrace_locations` (#1556).
* Implemented `rb_module_new`, `rb_define_class_id`, `rb_define_module_id`, (#1876, @XrXr, @chrisseaton).
* Implemented `-n` CLI option (#1532).
* Cache the `Symbol` of method names in call nodes only when needed (#1872).
* Implemented `rb_get_alloc_func` and related functions (#1874, @XrXr).
* Implemented `rb_module_new`, `rb_define_class_id`, `rb_define_module_id`, (#1876, @chrisseaton).
* Implemented `ENV.slice`.
* Support for the Darkfish theme for RDoc generation has been added back.
* Implemented `Kernel#system` `exception: true` option.
* Implemented `Random.bytes`.
* Implemented `Random.random_number`.
* Added the ability to parse endless ranges.
* Made `Range#{to_a, step, each, bsearch, step, last, max, min, to_s, ==}` compatible with endless ranges.
* Made `Array#{[], []=, values_at, fill, slice!}` compatible with endless ranges.
* Defined `Array#{min, max}` methods.

Performance:

* Use a smaller limit for identity-based inline caches to improve warmup by avoiding too many deoptimizations.
* `long[]` array storage now correctly declare that they accept `int` values, reducing deoptimisations and promotions to `Object[]` storage.
* Enable inline caching of `Symbol` conversion for `rb_iv_get` and `rb_iv_set`.
* `rb_type` information is now cached on classes as a hidden variable to improve performance.
* Change to using thread local buffers for socket calls to reduce allocations.
* Refactor `IO.select` to reduce copying and optimisation boundaries.
* Refactor various `String` and `Rope` nodes to avoid Truffle performance warnings.
* Reading caller frames should now work in more cases without deoptimisation.

# 19.3.0

New features:

* Compilation of C extensions is now done with an internal LLVM toolchain producing both native code and bitcode. This means more C extensions should compile out of the box and this should resolve most linker-related issues.
* It is no longer necessary to install LLVM for installing C extensions on TruffleRuby.
* It is no longer necessary to install libc++ and libc++abi for installing C++ extensions on TruffleRuby.
* On macOS, it is no longer necessary to install the system headers package (#1417).
* License updated to EPL 2.0/GPL 2.0/LGPL 2.1 like recent JRuby.

Bug fixes:

* `rb_undef_method` now works for private methods (#1731, @cky).
* Fixed several issues when requiring C extensions concurrently (#1565).
* `self.method ||= value` with a private method now works correctly (#1673).
* Fixed `RegexpError: invalid multibyte escape` for binary regexps with a non-binary String (#1433).
* Arrays now report their methods to other languages for interopability (#1768).
* Installing `sassc` now works due to using the LLVM toolchain (#1753).
* Renamed `Truffle::Interop.respond_to?` to avoid conflict with Ruby's `respond_to?` (#1491).
* Warn only if `$VERBOSE` is `true` when a magic comment is ignored (#1757, @nirvdrum).
* Make C extensions use the same libssl as the one used for the openssl C extension (#1770).

Compatibility:

* `GC.stat` can now take an optional argument (#1716, @kirs).
* `Kernel#load` with `wrap` has been implemented (#1739).
* Implemented `Kernel#spawn` with `:chdir` (#1492).
* Implemented `rb_str_drop_bytes`, notably used by OpenSSL (#1740, @cky).
* Include executables of default gems, needed for `rails new` in Rails 6.
* Use compilation flags similar to MRI for C extension compilation.
* Warn for `gem update --system` as it is not fully supported yet and is often not needed.
* Pass `-undefined dynamic_lookup` to the linker on macOS like MRI.

Performance:

* Core methods are no longer always cloned, which reduces memory footprint and should improve warmup.
* Inline cache calls to `rb_intern()` with a constant name in C extensions.
* Improve allocation speed of native handles for C extensions.
* Improve the performance of `NIL_P` and `INT2FIX` in C extensions.
* Various fixes to improve Rack performance.
* Optimize `String#gsub(String)` by not creating a `Regexp` and using `String#index` instead.
* Fixed "FrameWithoutBoxing should not be materialized" compilation issue in `TryNode`.

# 19.2.0, August 2019

New features:

* `Fiddle` has been implemented.

Bug fixes:

* Set `RbConfig::CONFIG['ruby_version']` to the same value as the TruffleRuby version. This fixes reusing C extensions between different versions of TruffleRuby with Bundler (#1715).
* Fixed `Symbol#match` returning `MatchData` (#1706, @zhublik).
* Allow `Time#strftime` to be called with binary format strings.
* Do not modify the argument passed to `IO#write` when the encoding does not match (#1714).
* Use the class where the method was defined to check if an `UnboundMethod` can be used for `#define_method` (#1710).
* Fixed setting `$~` for `Enumerable` and `Enumerator::Lazy`'s `#grep` and `#grep_v`.
* Improved errors when interacting with single-threaded languages (#1709).

Compatibility:

* Added `Kernel#then` (#1703, @zhublik).
* `FFI::Struct#[]=` is now supported for inline character arrays.
* `blocking: true` is now supported for `FFI::Library#attach_function`.
* Implemented `Proc#>>` and `#<<` (#1688).
* `Thread.report_on_exception` is now `true` by default like MRI 2.5+.
* `BigDecimal` compatibility has been generally improved in several ways.

Changes:

* An interop read message sent to a `Proc` will no longer call the `Proc`.

Performance:

* Several `String` methods have been made faster by the usage of vector instructions
  when searching for a single-byte character in a String.
* Methods needing the caller frame are now better optimized.

# 19.1.0, June 2019

*Ruby is an experimental language in the GraalVM 19.1.0 release*

Bug fixes:

* Sharing for thread-safety of objects is now triggered later as intended, e.g., when a second `Thread` is started.
* Fixed `Array#to_h` so it doesn't set a default value (#1698).
* Removed extra `public` methods on `IO` (#1702).
* Fixed `Process.kill(signal, Process.pid)` when the signal is trapped as `:IGNORE` (#1702).
* Fixed `Addrinfo.new(String)` to reliably find the address family (#1702).
* Fixed argument checks in `BasicSocket#setsockopt` (#1460).
* Fixed `ObjectSpace.trace_object_allocations` (#1456).
* Fixed `BigDecimal#{clone,dup}` so it now just returns the receiver, per Ruby 2.5+ semantics (#1680).
* Fixed creating `BigDecimal` instances from non-finite `Float` values (#1685).
* Fixed `BigDecimal#inspect` output for non-finite values (e.g, NaN or -Infinity) (#1683).
* Fixed `BigDecimal#hash` to return the same value for two `BigDecimal` objects that are equal (#1656).
* Added missing `BigDecimal` constant definitions (#1684).
* Implemented `rb_eval_string_protect`.
* Fixed `rb_get_kwargs` to correctly handle optional and rest arguments.
* Calling `Kernel#raise` with a raised exception will no longer set the cause of the exception to itself (#1682).
* Return a `FFI::Function` correctly for functions returning a callback.
* Convert to intuitive Ruby exceptions when INVOKE fails (#1690).
* Implemented `FFI::Pointer#clear` (#1687).
* Procs will now yield to the block in their declaration context even when called with a block argument (#1657).
* Fixed problems with calling POSIX methods if `Symbol#[]` is redefined (#1665).
* Fixed sharing of `Array` and `Hash` elements for thread-safety of objects (#1601).
* Fixed concurrent modifications of `Gem::Specification::LOAD_CACHE` (#1601).
* Fix `TCPServer#accept` to set `#do_not_reverse_lookup` correctly on the created `TCPSocket`.

Compatibility:

* Exceptions from `coerce` are no longer rescued, like MRI.
* Implemented `Integer#{allbits?,anybits?,nobits?}`.
* `Integer#{ceil,floor,truncate}` now accept a precision and `Integer#round` accepts a rounding mode.
* Added missing `Enumerable#filter` and `Enumerator::Lazy#filter` aliases to the respective `select` method (#1610).
* Implemented more `Ripper` methods as no-ops (#1694, @Mogztter).
* Implemented `rb_enc_sprintf` (#1702).
* Implemented `ENV#{filter,filter!}` aliases for `select` and `select!`.
* Non-blocking `StringIO` and `Socket` APIs now support `exception: false` like MRI (#1702).
* Increased compatibility of `BigDecimal`.
* `String#-@` now performs string deduplication (#1608).
* `Hash#merge` now preserves the key order from the original hash for merged values (#1650).
* Coerce values given to `FFI::Pointer` methods.
* `FrozenError` is now defined and is used for `can't modify frozen` object exceptions.
* `StringIO` is now available by default like in MRI, because it is required by RubyGems.

Changes:

* Interactive sources (like the GraalVM polyglot shell) now all share the same binding (#1695).
* Hash code calculation has been improved to reduce hash collisions for `Hash` and other cases.

Performance:

* `eval(code, binding)` for a fixed `code` containing blocks is now much faster. This improves the performance of rendering `ERB` templates containing loops.
* `rb_str_cat` is faster due to the C string now being concatenated without first being converted to a Ruby string or having its encoding checked. As a side effect the behaviour of `rb_str_cat` should now more closely match that of MRI.

# 19.0.0, May 2019

*Ruby is an experimental language in the GraalVM 19.0.0 release*

Bug fixes:

* The debugger now sees global variables as the global scope.
* Temporary variables are no longer visible in the debugger.
* Setting breakpoints on some lines has been fixed.
* The OpenSSL C extension is now always recompiled, fixing various bugs when using the extension (e.g., when using Bundler in TravisCI) (#1676, #1627, #1632).
* Initialize `$0` when not run from the 'ruby' launcher, which is needed to `require` gems (#1653).

Compatibility:

* `do...end` blocks can now have `rescue/else/ensure` clauses like MRI (#1618).

Changes:

* `TruffleRuby.sulong?` has been replaced by `TruffleRuby.cexts?`, and `TruffleRuby.graal?` has been replaced by `TruffleRuby.jit?`. The old methods will continue to work for now, but will produce warnings, and will be removed at a future release.

# 1.0 RC 16, 19 April 2019

Bug fixes:

* Fixed `Hash#merge` with no arguments to return a new copy of the receiver (#1645).
* Fixed yield with a splat and keyword arguments (#1613).
* Fixed `rb_scan_args` to correctly handle kwargs in combination with optional args.
* Many fixes for `FFI::Pointer` to be more compatible with the `ffi` gem.

New features:

* Rounding modes have been implemented or improved for `Float`, `Rational`, `BigDecimal` (#1509).
* Support Homebrew installed in other prefixes than `/usr/local` (#1583).
* Added a pure-Ruby implementation of FFI which passes almost all Ruby FFI specs (#1529, #1524).

Changes:

* Support for the Darkfish theme for RDoc generation has been removed.

Compatibility:

* The `KeyError` raised from `ENV#fetch` and `Hash#fetch` now matches MRI's message formatting (#1633).
* Add the missing `key` and `receiver` values to `KeyError` raised from `ENV#fetch`.
* `String#unicode_normalize` has been moved to the core library like in MRI.
* `StringScanner` will now match a regexp beginning with `^` even when not scanning from the start of the string.
* `Module#define_method` is now public like in MRI.
* `Kernel#warn` now supports the `uplevel:` keyword argument.

# 1.0 RC 15, 5 April 2019

Bug fixes:

* Improved compatibility with MRI's `Float#to_s` formatting (#1626).
* Fixed `String#inspect` when the string uses a non-UTF-8 ASCII-compatible encoding and has non-ASCII characters.
* Fixed `puts` for strings with non-ASCII-compatible encodings.
* `rb_protect` now returns `Qnil` when an error occurs.
* Fixed a race condition when using the interpolate-once (`/o`) modifier in regular expressions.
* Calling `StringIO#close` multiple times no longer raises an exception (#1640).
* Fixed a bug in include file resolution when compiling C extensions.

New features:

* `Process.clock_getres` has been implemented.

Changes:

* `debug`, `profile`, `profiler`, which were already marked as unsupported, have been removed.
* Our experimental JRuby-compatible Java interop has been removed - use `Polyglot` and `Java` instead.
* The Trufle handle patches applied to `psych` C extension have now been removed.
* The `rb_tr_handle_*` functions have been removed as they are no longer used in any C extension patches.
* Underscores and dots in options have become hyphens, so `--exceptions.print_uncaught_java` is now `--exceptions-print-uncaught-java`, for example.
* The `rb_tr_handle_*` functions have been removed as they are no longer used in any C extension patches.

Bug fixes:

* `autoload :C, "path"; require "path"` now correctly triggers the autoload.
* Fixed `UDPSocket#bind` to specify family and socktype when resolving address.
* The `shell` standard library can now be `require`-d.
* Fixed a bug where `for` could result in a `NullPointerException` when trying to assign the iteration variable.
* Existing global variables can now become aliases of other global variables (#1590).

Compatibility:

* ERB now uses StringScanner and not the fallback, like on MRI. As a result `strscan` is required by `require 'erb'` (#1615).
* Yield different number of arguments for `Hash#each` and `Hash#each_pair` based on the block arity like MRI (#1629).
* Add support for the `base` keyword argument to `Dir.{[], glob}`.

# 1.0 RC 14, 18 March 2019

Updated to Ruby 2.6.2.

Bug fixes:

* Implement `rb_io_wait_writable` (#1586).
* Fixed error when using arrows keys first within `irb` or `pry` (#1478, #1486).
* Coerce the right hand side for all `BigDecimal` operations (#1598).
* Combining multiple `**` arguments containing duplicate keys produced an incorrect hash. This has now been fixed (#1469).
* `IO#read_nonblock` now returns the passed buffer object, if one is supplied.
* Worked out autoloading issue (#1614).

New features:

* Implemented `String#delete_prefix`, `#delete_suffix`, and related methods.
* Implemented `Dir.children` and `Dir#children`.
* Implemented `Integer#sqrt`.

Changes:

* `-Xoptions` has been removed - use `--help:languages` instead.
* `-Xlog=` has been removed - use `--log.level=` instead.
* `-J` has been removed - use `--vm.` instead.
* `-J-cp lib.jar` and so on have removed - use `--vm.cp=lib.jar` or `--vm.classpath=lib.jar` instead.
* `--jvm.` and `--native.` have been deprecated, use `--vm.` instead to pass VM options.
* `-Xoption=value` has been removed - use `--option=value` instead.
* The `-X` option now works as in MRI.
* `--help:debug` is now `--help:internal`.
* `ripper` is still not implemented, but the module now exists and has some methods that are implemented as no-ops.

# 1.0 RC 13, 5 March 2019

Note that as TruffleRuby RC 13 is built on Ruby 2.4.4 it is still vulnerable to CVE-2018-16395. This will be fixed in the next release.

New features:

* Host interop with Java now works on SubstrateVM too.

Bug fixes:

* Fixed `Enumerator::Lazy` which wrongly rescued `StandardError` (#1557).
* Fixed several problems with `Numeric#step` related to default arguments, infinite sequences, and bad argument types (#1520).
* Fixed incorrect raising of `ArgumentError` with `Range#step` when at least one component of the `Range` is `Float::INFINITY` (#1503).
* Fixed the wrong encoding being associated with certain forms of heredoc strings (#1563).
* Call `#coerce` on right hand operator if `BigDecimal` is the left hand operator (#1533, @Quintasan).
* Fixed return type of division of `Integer.MIN_VALUE` and `Long.MIN_VALUE` by -1 (#1581).
* `Exception#cause` is now correctly set for internal exceptions (#1560).
* `rb_num2ull` is now implemented as well as being declared in the `ruby.h` header (#1573).
* `rb_sym_to_s` is now implemented (#1575).
* `R_TYPE_P` now returns the type number for a wider set of Ruby objects (#1574).
* `rb_fix2str` has now been implemented.
* `rb_protect` will now work even if `NilClass#==` has been redefined.
* `BigDecimal` has been moved out of the `Truffle` module to match MRI.
* `StringIO#puts` now correctly handles `to_s` methods which do not return strings (#1577).
* `Array#each` now behaves like MRI when the array is modified (#1580).
* Clarified that `$SAFE` can never be set to a non-zero value.
* Fix compatibility with RubyGems 3 (#1558).
* `Kernel#respond_to?` now returns false if a method is protected and the `include_all` argument is false (#1568).

Changes:

* `TRUFFLERUBY_CEXT_ENABLED` is no longer supported and C extensions are now always built, regardless of the value of this environment variable.
* Getting a substring of a string created by a C extension now uses less memory as only the requested portion will be copied to a managed string.
* `-Xoptions` has been deprecated and will be removed - use `--help:languages` instead.
* `-Xlog=` has been deprecated and will be removed - use `--log.level=` instead.
* `-J` has been deprecated and will be removed - use `--jvm.` instead.
* `-J-cp lib.jar` and so on have been deprecated and will be removed - use `--jvm.cp=lib.jar` or `--jvm.classpath=lib.jar` instead.
* `-J-cmd`, `--jvm.cmd`, `JAVA_HOME`, `JAVACMD`, and `JAVA_OPTS` do not work in any released configuration of TruffleRuby, so have been removed.
* `-Xoption=value` has been deprecated and will be removed - use `--option=value` instead.
* `TracePoint` now raises an `ArgumentError` for unsupported events.
* `TracePoint.trace` and `TracePoint#inspect` have been implemented.

Compatibility:

* Improved the exception when an `-S` file isn't found.
* Removed the message from exceptions raised by bare `raise` to better match MRI (#1487).
* `TracePoint` now handles the `:class` event.

Performance:

* Sped up `String` handling in native extensions, quite substantially in some cases, by reducing conversions between native and managed strings and allowing for mutable metadata in native strings.

# 1.0 RC 12, 4 February 2019

Bug fixes:

* Fixed a bug with `String#lines` and similar methods with multibyte characters (#1543).
* Fixed an issue with `String#{encode,encode!}` double-processing strings using XML conversion options and a new destination encoding (#1545).
* Fixed a bug where a raised cloned exception would be caught as the original exception (#1542).
* Fixed a bug with `StringScanner` and patterns starting with `^` (#1544).
* Fixed `Enumerable::Lazy#uniq` with infinite streams (#1516).

Compatibility:

* Change to a new system for handling Ruby objects in C extensions which greatly increases compatibility with MRI.
* Implemented `BigDecimal#to_r` (#1521).
* `Symbol#to_proc` now returns `-1` like on MRI (#1462).

# 1.0 RC 11, 15 January 2019

New features:

* macOS clocks `CLOCK_MONOTONIC_RAW`, `_MONOTONIC_RAW_APPROX`, `_UPTIME_RAW`, `_UPTIME_RAW_APPROX`, and `_PROCESS_CPUTIME_ID` have been implemented (#1480).
* TruffleRuby now automatically detects native access and threading permissions from the `Context` API, and can run code with no permissions given (`Context.create()`).

Bug fixes:

* FFI::Pointer now does the correct range checks for signed and unsigned values.
* Allow signal `0` to be used with `Process.kill` (#1474).
* `IO#dup` now properly sets the new `IO` instance to be close-on-exec.
* `IO#reopen` now properly resets the receiver to be close-on-exec.
* `StringIO#set_encoding` no longer raises an exception if the underlying `String` is frozen (#1473).
* Fix handling of `Symbol` encodings in `Marshal#dump` and `Marshal#load` (#1530).

Compatibility:

* Implemented `Dir.each_child`.
* Adding missing support for the `close_others` option to `exec` and `spawn`.
* Implemented the missing `MatchData#named_captures` method (#1512).

Changes:

* `Process::CLOCK_` constants have been given the same value as in standard Ruby.

Performance:

* Sped up accesses to native memory through FFI::Pointer.
* All core files now make use of frozen `String` literals, reducing the number of `String` allocations for core methods.
* New -Xclone.disable option to disable all manual cloning.

# 1.0 RC 10, 5 December 2018

New features:

* The `nkf` and `kconv` standard libraries were added (#1439).
* `Mutex` and `ConditionVariable` have a new fast path for acquiring locks that are unlocked.
* `Queue` and `SizedQueue`, `#close` and `#closed?`, have been implemented.
* `Kernel#clone(freeze)` has been implemented (#1454).
* `Warning.warn` has been implemented (#1470).
* `Thread.report_on_exception` has been implemented (#1476).
* The emulation symbols for `Process.clock_gettime` have been implemented.

Bug fixes:

* Added `rb_eEncodingError` for C extensions (#1437).
* Fixed race condition when creating threads (#1445).
* Handle `exception: false` for IO#write_nonblock (#1457, @ioquatix).
* Fixed `Socket#connect_nonblock` for the `EISCONN` case (#1465, @ioquatix).
* `File.expand_path` now raises an exception for a non-absolute user-home.
* `ArgumentError` messages now better match MRI (#1467).
* Added support for `:float_millisecond`, `:millisecond`, and `:second` time units to `Process.clock_gettime` (#1468).
* Fixed backtrace of re-raised exceptions (#1459).
* Updated an exception message in Psych related to loading a non-existing class so that it now matches MRI.
* Fixed a JRuby-style Java interop compatibility issue seen in `test-unit`.
* Fixed problem with calling `warn` if `$stderr` has been reassigned.
* Fixed definition of `RB_ENCODING_GET_INLINED` (#1440).

Changes:

* Timezone messages are now logged at `CONFIG` level, use `-Xlog=CONFIG` to debug if the timezone is incorrectly shown as `UTC`.

# 1.0 RC 9, 5 November 2018

Security:

* CVE-2018-16396, *tainted flags are not propagated in Array#pack and String#unpack with some directives* has been mitigated by adding additional taint operations.

New features:

* LLVM for Oracle Linux 7 can now be installed without building from source.

Bug fixes:

* Times can now be created with UTC offsets in `+/-HH:MM:SS` format.
* `Proc#to_s` now has `ASCII-8BIT` as its encoding instead of the incorrect `UTF-8`.
* `String#%` now has the correct encoding for `UTF-8` and `US-ASCII` format strings, instead of the incorrect `ASCII-8BIT`.
* Updated `BigDecimal#to_s` to use `e` instead of `E` for exponent notation.
* Fixed `BigDecimal#to_s` to allow `f` as a format flag to indicate conventional floating point notation. Previously only `F` was allowed.

Changes:

* The supported version of LLVM for Oracle Linux has been updated from 3.8 to 4.0.
* `mysql2` is now patched to avoid a bug in passing `NULL` to `rb_scan_args`, and now passes the majority of its test suite.
* The post-install script now automatically detects if recompiling the OpenSSL C extension is needed. The post-install script should always be run in TravisCI as well, see `doc/user/standalone-distribution.md`.
* Detect when the system libssl is incompatible more accurately and add instructions on how to recompile the extension.

# 1.0 RC 8, 19 October 2018

New features:

* `Java.synchronized(object) { }` and `TruffleRuby.synchronized(object) { }` methods have been added.
* Added a `TruffleRuby::AtomicReference` class.
* Ubuntu 18.04 LTS is now supported.
* macOS 10.14 (Mojave) is now supported.

Changes:

* Random seeds now use Java's `NativePRNGNonBlocking`.
* The supported version of Fedora is now 28, upgraded from 25.
* The FFI gem has been updated from 1.9.18 to 1.9.25.
* JCodings has been updated from 1.0.30 to 1.0.40.
* Joni has been updated from 2.1.16 to 2.1.25.

Performance:

* Performance of setting the last exception on a thread has now been improved.

# 1.0 RC 7, 3 October 2018

New features:

* Useful `inspect` strings have been added for more foreign objects.
* The C extension API now defines a preprocessor macro `TRUFFLERUBY`.
* Added the rbconfig/sizeof native extension for better MRI compatibility.
* Support for `pg` 1.1. The extension now compiles successfully, but may still have issues with some datatypes.

Bug fixes:

* `readline` can now be interrupted by the interrupt signal (Ctrl+C). This fixes Ctrl+C to work in IRB.
* Better compatibility with C extensions due to a new "managed struct" type.
* Fixed compilation warnings which produced confusing messages for end users (#1422).
* Improved compatibility with Truffle polyglot STDIO.
* Fixed version check preventing TruffleRuby from working with Bundler 2.0 and later (#1413).
* Fixed problem with `Kernel.public_send` not tracking its caller properly (#1425).
* `rb_thread_call_without_gvl()` no longer holds the C-extensions lock.
* Fixed `caller_locations` when called inside `method_added`.
* Fixed `mon_initialize` when called inside `initialize_copy` (#1428).
* `Mutex` correctly raises a `TypeError` when trying to serialize with `Marshal.dump`.

Performance:

* Reduced memory footprint for private/internal AST nodes.
* Increased the number of cases in which string equality checks will become compile-time constants.
* Major performance improvement for exceptional paths where the rescue body does not access the exception object (e.g., `x.size rescue 0`).

Changes:

* Many clean-ups to our internal patching mechanism used to make some native extensions run on TruffleRuby.
* Removed obsoleted patches for Bundler compatibility now that Bundler 1.16.5 has built-in support for TruffleRuby.
* Reimplemented exceptions and other APIs that can return a backtrace to use Truffle's lazy stacktraces API.

# 1.0 RC 6, 3 September 2018

New features:

* `Polyglot.export` can now be used with primitives, and will now convert strings to Java, and `.import` will convert them from Java.
* Implemented `--encoding`, `--external-encoding`, `--internal-encoding`.
* `rb_object_tainted` and similar C functions have been implemented.
* `rb_struct_define_under` has been implemented.
* `RbConfig::CONFIG['sysconfdir']` has been implemented.
* `Etc` has been implemented (#1403).
* The `-Xcexts=false` option disables C extensions.
* Instrumentation such as the CPUSampler reports methods in a clearer way like `Foo#bar`, `Gem::Specification.each_spec`, `block in Foo#bar` instead of just `bar`, `each_spec`, `block in bar` (which is what MRI displays in backtraces).
* TruffleRuby is now usable as a JSR 223 (`javax.script`) language.
* A migration guide from JRuby (`doc/user/jruby-migration.md`) is now included.
* `kind_of?` works as an alias for `is_a?` on foreign objects.
* Boxed foreign strings unbox on `to_s`, `to_str`, and `inspect`.

Bug fixes:

* Fix false-positive circular warning during autoload.
* Fix Truffle::AtomicReference for `concurrent-ruby`.
* Correctly look up `llvm-link` along `clang` and `opt` so it is no longer needed to add LLVM to `PATH` on macOS for Homebrew and MacPorts.
* Fix `alias` to work when in a refinement module (#1394).
* `Array#reject!` no longer truncates the array if the block raises an exception for an element.
* WeakRef now has the same inheritance and methods as MRI's version.
* Support `-Wl` linker argument for C extensions. Fixes compilation of`mysql2` and `pg`.
* Using `Module#const_get` with a scoped argument will now correctly autoload the constant if needed.
* Loaded files are read as raw bytes, rather than as a UTF-8 string and then converted back into bytes.
* Return 'DEFAULT' for `Signal.trap(:INT) {}`. Avoids a backtrace when quitting a Sinatra server with Ctrl+C.
* Support `Signal.trap('PIPE', 'SYSTEM_DEFAULT')`, used by the gem `rouge` (#1411).
* Fix arity checks and handling of arity `-2` for `rb_define_method()`.
* Setting `$SAFE` to a negative value now raises a `SecurityError`.
* The offset of `DATA` is now correct in the presence of heredocs.
* Fix double-loading of the `json` gem, which led to duplicate constant definition warnings.
* Fix definition of `RB_NIL_P` to be early enough. Fixes compilation of `msgpack`.
* Fix compilation of megamorphic interop calls.
* `Kernel#singleton_methods` now correctly ignores prepended modules of non-singleton classes. Fixes loading `sass` when `activesupport` is loaded.
* Object identity numbers should never be negative.

Performance:

* Optimize keyword rest arguments (`def foo(**kwrest)`).
* Optimize rejected (non-Symbol keys) keyword arguments.
* Source `SecureRandom.random_bytes` from `/dev/urandom` rather than OpenSSL.
* C extension bitcode is no longer encoded as Base64 to pass it to Sulong.
* Faster `String#==` using vectorization.

Changes:

* Clarified that all sources that come in from the Polyglot API `eval` method will be treated as UTF-8, and cannot be re-interpreted as another encoding using a magic comment.
* The `-Xembedded` option can now be set set on the launcher command line.
* The `-Xplatform.native=false` option can now load the core library, by enabling `-Xpolyglot.stdio`.
* `$SAFE` and `Thread#safe_level` now cannot be set to `1` - raising an error rather than warning as before. `-Xsafe` allows it to be set, but there are still no checks.
* Foreign objects are now printed as `#<Foreign:system-identity-hash-code>`, except for foreign arrays which are now printed as `#<Foreign [elements...]>`.
* Foreign objects `to_s` now calls `inspect` rather than Java's `toString`.
* The embedded configuration (`-Xembedded`) now warns about features which may not work well embedded, such as signals.
* The `-Xsync.stdio` option has been removed - use standard Ruby `STDOUT.sync = true` in your program instead.

# 1.0 RC 5, 3 August 2018

New features:

* It is no longer needed to add LLVM (`/usr/local/opt/llvm@4/bin`) to `PATH` on macOS.
* Improve error message when LLVM, `clang` or `opt` is missing.
* Automatically find LLVM and libssl with MacPorts on macOS (#1386).
* `--log.ruby.level=` can be used to set the log level from any launcher.
* Add documentation about installing with Ruby managers/installers and how to run TruffleRuby in CI such as TravisCI (#1062, #1070).
* `String#unpack1` has been implemented.

Bug fixes:

* Allow any name for constants with `rb_const_get()`/`rb_const_set()` (#1380).
* Fix `defined?` with an autoload constant to not raise but return `nil` if the autoload fails (#1377).
* Binary Ruby Strings can now only be converted to Java Strings if they only contain US-ASCII characters. Otherwise, they would produce garbled Java Strings (#1376).
* `#autoload` now correctly calls `main.require(path)` dynamically.
* Hide internal file from user-level backtraces (#1375).
* Show caller information in warnings from the core library (#1375).
* `#require` and `#require_relative` should keep symlinks in `$"` and `__FILE__` (#1383).
* Random seeds now always come directly from `/dev/urandom` for MRI compatibility.
* SIGINFO, SIGEMT and SIGPWR are now defined (#1382).
* Optional and operator assignment expressions now return the value assigned, not the value returned by an assignment method (#1391).
* `WeakRef.new` will now return the correct type of object, even if `WeakRef` is subclassed (#1391).
* Resolving constants in prepended modules failed, this has now been fixed (#1391).
* Send and `Symbol#to_proc` now take account of refinements at their call sites (#1391).
* Better warning when the timezone cannot be found on WSL (#1393).
* Allow special encoding names in `String#force_encoding` and raise an exception on bad encoding names (#1397).
* Fix `Socket.getifaddrs` which would wrongly return an empty array (#1375).
* `Binding` now remembers the file and line at which it was created for `#eval`. This is notably used by `pry`'s `binding.pry`.
* Resolve symlinks in `GEM_HOME` and `GEM_PATH` to avoid related problems (#1383).
* Refactor and fix `#autoload` so other threads see the constant defined while the autoload is in progress (#1332).
* Strings backed by `NativeRope`s now make a copy of the rope when `dup`ed.
* `String#unpack` now taints return strings if the format was tainted, and now does not taint the return array if the format was tainted.
* Lots of fixes to `Array#pack` and `String#unpack` tainting, and a better implementation of `P` and `p`.
* Array literals could evaluate an element twice under some circumstances. This has now been fixed.

Performance:

* Optimize required and optional keyword arguments.
* `rb_enc_to_index` is now faster by eliminating an expensive look-up.

Changes:

* `-Xlog=` now needs log level names to be upper case.
* `-Dtruffleruby.log` and `TRUFFLERUBY_LOG` have been removed - use `-Dpolyglot.log.ruby.level`.
* The log format, handlers, etc are now managed by the Truffle logging system.
* The custom log levels `PERFORMANCE` and `PATCH` have been removed.

# 1.0 RC 4, 18 July 2018

*TruffleRuby was not updated in RC 4*

# 1.0 RC 3, 2 July 2018

New features:

* `is_a?` can be called on foreign objects.

Bug fixes:

* It is no longer needed to have `ruby` in `$PATH` to run the post-install hook.
* `Qnil`/`Qtrue`/`Qfalse`/`Qundef` can now be used as initial value for global variables in C extensions.
* Fixed error message when the runtime libssl has no SSLv2 support (on Ubuntu 16.04 for instance).
* `RbConfig::CONFIG['extra_bindirs']` is now a String as other RbConfig values.
* `SIGPIPE` is correctly caught on SubstrateVM, and the corresponding write() raises `Errno::EPIPE` when the read end of a pipe or socket is closed.
* Use the magic encoding comment for determining the source encoding when using eval().
* Fixed a couple bugs where the encoding was not preserved correctly.

Performance:

* Faster stat()-related calls, by returning the relevant field directly and avoiding extra allocations.
* `rb_str_new()`/`rb_str_new_cstr()` are much faster by avoiding extra copying and allocations.
* `String#{sub,sub!}` are faster in the common case of an empty replacement string.
* Eliminated many unnecessary memory copy operations when reading from `IO` with a delimiter (e.g., `IO#each`), leading to overall improved `IO` reading for common use cases such as iterating through lines in a `File`.
* Use the byte[] of the given Ruby String when calling eval() directly for parsing.

# 1.0 RC 2, 6 June 2018

New features:

* We are now compatible with Ruby 2.4.4.
* `object.class` on a Java `Class` object will give you an object on which you can call instance methods, rather than static methods which is what you get by default.
* The log level can now also be set with `-Dtruffleruby.log=info` or `TRUFFLERUBY_LOG=info`.
* `-Xbacktraces.raise` will print Ruby backtraces whenever an exception is raised.
* `Java.import name` imports Java classes as top-level constants.
* Coercion of foreign numbers to Ruby numbers now works.
* `to_s` works on all foreign objects and calls the Java `toString`.
* `to_str` will try to `UNBOX` and then re-try `to_str`, in order to provoke the unboxing of foreign strings.

Changes:

* The version string now mentions if you're running GraalVM Community Edition (`GraalVM CE`) or GraalVM Enterprise Edition (`GraalVM EE`).
* The inline JavaScript functionality `-Xinline_js` has been removed.
* Line numbers `< 0`, in the various eval methods, are now warned about, because we don't support these at all. Line numbers `> 1` are warned about (at the fine level) but they are shimmed by adding blank lines in front to get to the correct offset. Line numbers starting at `0` are also warned about at the fine level and set to `1` instead.
* The `erb` standard library has been patched to stop using a -1 line number.
* `-Xbacktraces.interleave_java` now includes all the trailing Java frames.
* Objects with a `[]` method, except for `Hash`, now do not return anything for `KEYS`, to avoid the impression that you could `READ` them. `KEYINFO` also returns nothing for these objects, except for `Array` where it returns information on indices.
* `String` now returns `false` for `HAS_KEYS`.
* The supported additional functionality module has been renamed from `Truffle` to `TruffleRuby`. Anything not documented in `doc/user/truffleruby-additions.md` should not be used.
* Imprecise wrong gem directory detection was replaced. TruffleRuby newly marks its gem directories with a marker file, and warns if you try to use TruffleRuby with a gem directory which is lacking the marker.

Bug fixes:

* TruffleRuby on SubstrateVM now correctly determines the system timezone.
* `Kernel#require_relative` now coerces the feature argument to a path and canonicalizes it before requiring, and it now uses the current directory as the directory for a synthetic file name from `#instance_eval`.

# 1.0 RC 1, 17 April 2018

New features:

* The Ruby version has been updated to version 2.3.7.

Security:

* CVE-2018-6914, CVE-2018-8779, CVE-2018-8780, CVE-2018-8777, CVE-2017-17742 and CVE-2018-8778 have been mitigated.

Changes:

* `RubyTruffleError` has been removed and uses replaced with standard exceptions.
* C++ libraries like `libc++` are now not needed if you don't run C++ extensions. `libc++abi` is now never needed. Documentation updated to make it more clear what the minimum requirements for pure Ruby, C extensions, and C++ extensions separately.
* C extensions are now built by default - `TRUFFLERUBY_CEXT_ENABLED` is assumed `true` unless set to `false`.
* The `KEYS` interop message now returns an array of Java strings, rather than Ruby strings. `KEYS` on an array no longer returns indices.
* `HAS_SIZE` now only returns `true` for `Array`.
* A method call on a foreign object that looks like an operator (the method name does not begin with a letter) will call `IS_BOXED` on the object and based on that will possibly `UNBOX` and convert to Ruby.
* Now using the native version of Psych.
* The supported version of LLVM on Oracle Linux has been dropped to 3.8.
* The supported version of Fedora has been dropped to 25, and the supported version of LLVM to 3.8, due to LLVM incompatibilities. The instructions for installing `libssl` have changed to match.

# 0.33, April 2018

New features:

* The Ruby version has been updated to version 2.3.6.
* Context pre-initialization with TruffleRuby `--native`, which significantly improves startup time and loads the `did_you_mean` gem ahead of time.
* The default VM is changed to SubstrateVM, where the startup is significantly better. Use `--jvm` option for full JVM VM.
* The `Truffle::Interop` module has been replaced with a new `Polyglot` module which is designed to use more idiomatic Ruby syntax rather than explicit methods. A [new document](doc/user/polyglot.md) describes polyglot programming at a higher level.
* The `REMOVABLE`, `MODIFIABLE` and `INSERTABLE` Truffle interop key info flags have been implemented.
* `equal?` on foreign objects will check if the underlying objects are equal if both are Java interop objects.
* `delete` on foreign objects will send `REMOVE`, `size` will send `GET_SIZE`, and `keys` will send `KEYS`. `respond_to?(:size)` will send `HAS_SIZE`, `respond_to?(:keys)` will send `HAS_KEYS`.
* Added a new Java-interop API similar to the one in the Nashorn JavaScript implementation, as also implemented by Graal.js. The `Java.type` method returns a Java class object on which you can use normal interop methods. Needs the `--jvm` flag to be used.
* Supported and tested versions of LLVM for different platforms have been more precisely [documented](doc/user/installing-llvm.md).

Changes:

* Interop semantics of `INVOKE`, `READ`, `WRITE`, `KEYS` and `KEY_INFO` have changed significantly, so that `INVOKE` maps to Ruby method calls, `READ` calls `[]` or returns (bound) `Method` objects, and `WRITE` calls `[]=`.

Performance:

* `Dir.glob` is much faster and more memory efficient in cases that can reduce to direct filename lookups.
* `SecureRandom` now defers loading OpenSSL until it's needed, reducing time to load `SecureRandom`.
* `Array#dup` and `Array#shift` have been made constant-time operations by sharing the array storage and keeping a starting index.

Bug fixes:

* Interop key-info works with non-string-like names.

Internal changes:

* Changes to the lexer and translator to reduce regular expression calls.
* Some JRuby sources have been updated to 9.1.13.0.

# 0.32, March 2018

New features:

* A new embedded configuration is used when TruffleRuby is used from another language or application. This disables features like signals which may conflict with the embedding application, and threads which may conflict with other languages, and enables features such as the use of polyglot IO streams.

Performance:

* Conversion of ASCII-only Ruby strings to Java strings is now faster.
* Several operations on multi-byte character strings are now faster.
* Native I/O reads are about 22% faster.

Bug fixes:

* The launcher accepts `--native` and similar options in  the `TRUFFLERUBYOPT` environment variable.

Internal changes:

* The launcher is now part of the TruffleRuby repository, rather than part of the GraalVM repository.
* `ArrayBuilderNode` now uses `ArrayStrategies` and `ArrayMirrors` to remove direct knowledge of array storage.
* `RStringPtr` and `RStringPtrEnd` now report as pointers for interop purposes, fixing several issues with `char *` usage in C extensions.
