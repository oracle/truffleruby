# Interop Sample

GraalVM includes multiple languages, including Ruby and JavaScript. We can use
it to run programs written in a combination of languages. For example, we can
use the Ruby `openweather` gem for getting the weather in a city, from
JavaScript.

http://www.oracle.com/technetwork/oracle-labs/program-languages/

First, clone `openweather`.

```
$ git clone https://github.com/lucasocon/openweather.git
$ pushd openweather
$ git checkout d5f49d3c567bd1ac3e055a65189661d8d3851c7f
$ popd
```

And then run the JavaScript interpreter. We need to put the `openweather/lib`
directory, and the current directory on the load path, but we don't have a Ruby
command line. Instead, we use the `load_paths` option, which as a Java system
property is written `truffleruby.load_paths`.

```
.../graalvm/bin/js -J-Dtruffleruby.load_paths=openweather/lib,. weather.js
```

Note that this sample uses the API key from the `openweather` gem tests, and you
shouldn't re-use this in production.
