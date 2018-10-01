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
* `bin/setup`
* `bundle exec rails server`
* Go to <http://localhost:3000>
