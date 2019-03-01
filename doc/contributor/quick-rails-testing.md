# Quick Rails testing

This repository contains a small Rails application for testing  in the
`test/truffle/ecosystem/rails-app` directory. The test can be executed with `jt
test ecosystem rails-app` in TruffleRuby's home directory. When executed it
prepares its environment, starts the Rails server, runs a few tests against JSON
API, and shut downs the rails server. It requires Redis as a database to be
running in its default configuration on `localhost:6379`.

The `jt test ecosystem rails-app` should be executed in TruffleRuby's home. The
output will be similar to following:

```txt
jt test ecosystem rails-app
path/to/truffleruby/truffleruby-gem-test-pack-5
$ path/to/truffleruby/test/truffle/ecosystem/rails-app.sh
++ PORT=14873
+ jt gem-test-pack
+ ruby tool/jt.rb gem-test-pack
path/to/truffleruby/truffleruby-gem-test-pack-5
++ pwd
+ truffle_ruby=path/to/truffleruby
+ JTR='path/to/truffleruby/bin/truffleruby path/to/truffleruby/test/truffleruby-tool/bin/truffleruby-tool'
+ rails_app=path/to/truffleruby/test/truffle/ecosystem/rails-app
+ cd path/to/truffleruby/test/truffle/ecosystem/rails-app
+ '[' -n '' -a -z '' ']'
+ '[' -f tmp/pids/server.pid ']'
++ cat tmp/pids/server.pid
+ kill 7043
path/to/truffleruby/test/truffle/ecosystem/rails-app.sh: line 21: kill: (7043) - No such process
+ true
+ rm tmp/pids/server.pid
+ path/to/truffleruby/bin/truffleruby path/to/truffleruby/test/truffleruby-tool/bin/truffleruby-tool setup --offline
jtt: loading YAML configuration path/to/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool.yaml
jtt: executing "setup" command
jtt: $ TRUFFLERUBY_RESILIENT_GEM_HOME="" GEM_HOME="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" GEM_PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" path/to/truffleruby/bin/truffleruby path/to/truffleruby/truffleruby-gem-test-pack-5/gems/bin/bundle install --local --no-prune
[ruby] WARNING rubygems.rb:11 Could not find 'did_you_mean' (>= 0) among 148 total gem(s)
Checked in 'GEM_PATH=path/to/truffleruby/truffleruby-gem-test-pack-5/gems', execute `gem env` for more information
Using rake 11.2.2
Using i18n 0.7.0
Using json 1.8.3
Using minitest 5.9.0
Using thread_safe 0.3.5
Using builder 3.2.2
Using erubis 2.7.0
Using mini_portile2 2.1.0
Using pkg-config 1.1.7
Using rack 1.6.4
Using mime-types-data 3.2016.0521
Using arel 6.0.3
Using concurrent-ruby 1.0.2
Using multi_json 1.12.1
Using systemu 2.6.5
Using bundler 1.16.1
Using thor 0.19.1
Using rdoc 4.3.0
Using redis 3.3.1
Using tzinfo 1.2.2
Using nokogiri 1.6.8
Using rack-test 0.6.3
Using mime-types 3.1
Using sprockets 3.6.3
Using macaddr 1.7.1
Using sdoc 0.4.2
Using activesupport 4.2.6
Using loofah 2.0.3
Using mail 2.6.4
Using uuid 2.3.8
Using rails-deprecated_sanitizer 1.0.3
Using globalid 0.3.6
Using activemodel 4.2.6
Using jbuilder 2.5.0
Using rails-html-sanitizer 1.0.3
Using rails-dom-testing 1.0.7
Using activejob 4.2.6
Using activerecord 4.2.6
Using redis_orm 0.7
Using actionview 4.2.6
Using actionpack 4.2.6
Using actionmailer 4.2.6
Using railties 4.2.6
Using sprockets-rails 3.1.1
Using rails 4.2.6
Bundle complete! 6 Gemfile dependencies, 45 gems now installed.
Use `bundle show [gemname]` to see where a bundled gem is installed.
+ url=http://localhost:3000
+ set +x
+ path/to/truffleruby/bin/truffleruby path/to/truffleruby/test/truffleruby-tool/bin/truffleruby-tool run --offline -- -S bundle exec bin/rails server
....jtt: loading YAML configuration path/to/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool.yaml
jtt: executing "run" command
jtt: $ TRUFFLERUBY_RESILIENT_GEM_HOME="" GEM_HOME="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" GEM_PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems/bin:<snipped>" NO_FORK="true" path/to/truffleruby/bin/truffleruby -J-Xmx2G -J-ea -J-esa -Xcore.load_path\=path/to/truffleruby/src/main/ruby -I path/to/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool_bundle/mocks -S bundle exec bin/rails server
....[ruby] WARNING rubygems.rb:11 Could not find 'did_you_mean' (>= 0) among 148 total gem(s)
Checked in 'GEM_PATH=path/to/truffleruby/truffleruby-gem-test-pack-5/gems', execute `gem env` for more information
.HTML sanitization stubbed
..WARN: Unresolved specs during Gem::Specification.reset:
      minitest (~> 5.1)
      nokogiri (>= 1.5.9, ~> 1.6.0)
WARN: Clearing out unresolved specs.
Please report a bug if this causes problems.
........=> Booting WEBrick
=> Rails 4.2.6 application starting in development on http://localhost:3000
=> Run `rails server -h` for more startup options
=> Ctrl-C to shutdown server
.[2018-01-17 09:02:49] INFO  WEBrick 1.3.1
[2018-01-17 09:02:49] INFO  ruby 2.3.5 (2018-01-16) [x86_64-darwin]
[2018-01-17 09:02:49] INFO  WEBrick::HTTPServer#start: pid=9617 port=3000


Started GET "/people.json" for ::1 at 2018-01-17 09:02:50 +0100
Processing by PeopleController#index as JSON
Completed 200 OK in 73ms (Views: 1.7ms)
[]+ echo Server is up
Server is up
+ curl -s -X DELETE http://localhost:3000/people/destroy_all.json


Started DELETE "/people/destroy_all.json" for ::1 at 2018-01-17 09:02:50 +0100
Processing by PeopleController#destroy_all as JSON
Completed 200 OK in 5ms (Views: 0.9ms)
true++ curl -s http://localhost:3000/people.json


Started GET "/people.json" for ::1 at 2018-01-17 09:02:51 +0100
Processing by PeopleController#index as JSON
Completed 200 OK in 3ms (Views: 0.7ms)
+ test '[]' = '[]'
+ curl -s --data 'name=Anybody&email=ab@example.com' http://localhost:3000/people.json


Started POST "/people.json" for ::1 at 2018-01-17 09:02:51 +0100
Processing by PeopleController#create as JSON
  Parameters: {"name"=>"Anybody", "email"=>"ab@example.com"}
<Redis_person id: 18, name: "Anybody", email: "ab@example.com", created_at: 2018-01-17 09:02:51 +0100, modified_at: 2018-01-17 09:02:51 +0100> created
Completed 200 OK in 58ms (Views: 2.0ms)
{"id":18,"name":"Anybody","email":"ab@example.com"}+ curl -s http://localhost:3000/people.json
+ grep '"name":"Anybody","email":"ab@example.com"'


Started GET "/people.json" for ::1 at 2018-01-17 09:02:51 +0100
Processing by PeopleController#index as JSON
Completed 200 OK in 8ms (Views: 2.0ms)
[{"id":18,"name":"Anybody","email":"ab@example.com"}]
+ curl -s -X DELETE http://localhost:3000/people/destroy_all.json


Started DELETE "/people/destroy_all.json" for ::1 at 2018-01-17 09:02:51 +0100
Processing by PeopleController#destroy_all as JSON
Completed 200 OK in 16ms (Views: 1.4ms)
true+ kill %1
++ cat tmp/pids/server.pid
+ kill 9617
```

There are several levels of indirection but it always prints what command is actually
executed. It's prefixed with `+` if it's executed by bash or by `$` if it's our tool.

If the test finishes without problem then the rails server can be executed from
the `path/to/truffleruby/test/truffle/ecosystem/rails-app` directory for further investigation.
The server can be started with following command which can be copied out from the output.

```bash
TRUFFLERUBY_RESILIENT_GEM_HOME="" GEM_HOME="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" GEM_PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems" PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems/bin:<snipped>" NO_FORK="true" path/to/truffleruby/bin/truffleruby -J-Xmx2G -J-ea -J-esa -Xcore.load_path\=path/to/truffleruby/src/main/ruby -I path/to/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool_bundle/mocks -S bundle exec bin/rails server
```

The command can be further modified for inspection.
First it's useful to use jt not to run TruffleRuby directly.
(`-Xcore.load_path` option can be then omitted.)

```bash
env TRUFFLERUBY_RESILIENT_GEM_HOME="" GEM_HOME="/Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/truffleruby-gem-test-pack-5/gems" GEM_PATH="/Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/truffleruby-gem-test-pack-5/gems" PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems/bin:<snipped>" NO_FORK="true" ../../../../tool/jt.rb ruby -J-Xmx2G -J-ea -J-esa -I /Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool_bundle/mocks -S bundle exec ./bin/rails server
```

To run on Graal pass `--graal` to `jt ruby`.

```bash
env TRUFFLERUBY_RESILIENT_GEM_HOME="" GEM_HOME="/Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/truffleruby-gem-test-pack-5/gems" GEM_PATH="/Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/truffleruby-gem-test-pack-5/gems" PATH="path/to/truffleruby/truffleruby-gem-test-pack-5/gems/bin:<snipped>" NO_FORK="true" ../../../../tool/jt.rb ruby --graal -J-Xmx2G -J-ea -J-esa -I /Users/pitr/Workspace/labs/truffleruby-ws/truffleruby/test/truffle/ecosystem/rails-app/.truffleruby-tool_bundle/mocks -S bundle exec ./bin/rails server
```

The command can be further modified as needed to investigate.
