# TruffleRuby Tool

`truffleruby-tool` is a small command line utility designed to run and
test Ruby gems and applications on the TruffleRuby runtime. It manages
workarounds and wraps bundler install and execute.

## Setup

There is a `setup` subcommand to install dependencies and prepare workarounds.

-   Go to a directory of a gem/application you would like to test.
-   Run `truffleruby-tool setup`

It uses the default configuration (part of the tool) if one is available for a
given gem (it looks for a `gemspec` in current directory). It installs all
required gems (based on the `Gemfile`) and executes other steps defined in the 
configuration files or as command line options (see `truffleruby-tool
setup --help` to learn what additional setup steps are available, or see
the default configuration `config.rb`). After it finishes, 
the `run` subcommand can be used.

## Running

After the environment is set the gem can be used to execute code, files, or
gem's executables on TruffleRuby in the prepared environment. Examples follows
(options after `--` are interpreted by Ruby, options before `--` are options
for this tool):

-   `truffleruby-tool run -- file.rb` - executes file.rb
-   `truffleruby-tool run -- -e '1+1'` - evaluates 1+1 expression
-   `truffleruby-tool run -- -I test test/a_test_file_test.rb` - runs a test file
-   `truffleruby-tool run -S rspec -- spec/a_spec_file_spec.rb` - runs a spec file
    using the `rspec` executable of the rspec gem
-   `truffleruby-tool run --require mocks -- file.rb` - executes file.rb, but
    requires mocks first. (mocks can be made to load always by putting the
    option to configuration file (`.truffleruby-tool.yaml`) instead)

See `truffleruby-tool run --help` to see all available options.

## Clean up

To remove all files added during the setup phase, run `truffleruby-tool clean`,
it will only keep the `.truffleruby-tool.yaml` configuration file for future re-setup.

## Pre-configuration

Options which are always required or are part of the setup step can
pre-configured in the default configuration (included in the tool) or in the local 
`.truffleruby-tool.yaml` configuration file to avoid repeating options on the command
line. The configuration file has a 2-level deep tree structure. The first level is
the name of the command (or `:global`) and the second level is the name of the option
which is same as its long variant with `-` replaced by `_`. 

Configuration values are deep-merged in following order (potentially 
overriding): default values, default gem configuration, local configuration, 
command-line options. This tool contains default configurations for some gems 
in the `config.rb` file, which are used if available. An example of
activesupport's configuration follows:


```yaml
---
:global:
  # default ../jruby/bin/ruby won't work since activesupport is one more dir deeper
  :jruby_truffle_path: '../../truffleruby/bin/truffleruby'
  :graal_path: '../../graalvm-jdk1.8.0/bin/java'
:setup:
  :file:
    shims.rb: |
              require 'minitest'
              # mock load_plugins as it loads rubygems
              def Minitest.load_plugins
              end
    bundler.rb: "module Bundler; def self.setup; end; end"
    # mock method_source gem
    method_source.rb: nil
  # do not let bundler to install db gem group
  :without:
    - db
:run:
  :require:
    - shims
```

## Using the tool in CI

CI execution is defined in config.rb 
then `truffleruby-tool ci activemodel` can be used to run tests of the given gem in CI.

## Example step-by-step

```sh
git clone git@github.com:ruby-concurrency/concurrent-ruby.git
cd concurrent-ruby
git checkout v0.9.1 # latest release
rbenv shell jruby-local # use your compiled JRuby
truffleruby-tool setup
truffleruby-tool run -S rspec -- spec --format progress # run all tests
# you should see only a few errors
```
