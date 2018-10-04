# A simple blog

The base of the application was generated with:
```bash
rails new blog \ 
  --database=sqlite3 \
  --skip-git \
  --skip-bundle \
  --skip-action-cable \
  --skip-active-storage \
  --skip-action-mailer \
  --skip-action-cable \
  --skip-puma \
  --skip-turbolinks \
  --skip-coffee \
  --skip-javascript \
  --skip-spring \
  --skip-sprockets \
  --skip-yarn \
  --skip-bootsnap \
  --skip-listen
```
Then the source code of the old blog app running on rails 4.2 was ported over. 

## Setup

List of steps to get the web server running follow. 
If a step is required specially just for TruffleRuby it is explicitly mentioned, 
otherwise it is a standard step for all Ruby implementations.

* `gem install bundler`
* `bundle config --local build.nokogiri --use-system-libraries`
  * required by nokogiri to work on TruffleRuby
  * can be left enabled for MRI as well when testing, it works
* `bundle config --local without postgresql mysql`
  * disable gem groups which would install pg and mysql drivers
* `bin/setup`
* `bundle exec rails server`
* Go to <http://localhost:3000>

## Using different DB drivers

Rails by default have 3 environments: `development`, `test`, `production`.
Those are configured to use sqlite database. 

This application has extra environments to run with different database driver.
It has triplet of environments for postgresql database: 
`development-postgresql`, `test-postgresql`, `production-postgresql`. 
And another triplet for mysql: `development-mysql`, `test-mysql`, `production-mysql`.

To enable the e.g. mysql driver run `bundle config --local without postgresql` 
which removes mysql driver group from the exclusion list. 
Then rerun `bin/setup`, which installs the driver gem and creates the database. 

Rails commands have options to specify the environment they should be executed in.
Therefore, to run the web sever with mysql execute `bundle exec rails server -e development-mysql`,
or to run the rails console execute `bundle exec rails console development-mysql`. 
 
## Workarounds

* nokogiri has to install with system libraries
* the application was generated with lots of skipped standard parts
  * needs to be explored, it's expected to mostly work
* concurrent-ruby has to be specified to version '>= 1.1.0.pre2' in Gemfile
  * can be removed once 1.1.0 is released


`bundle config --local cache_path ...`
