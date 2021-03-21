Based on https://github.com/benchmark-driver/sinatra/blob/master/driver.yml

Gems were vendored using:
```
bundle install --standalone
```

with this Gemfile:
```
source 'https://rubygems.org'

gem 'sinatra'
```

Then moving `bundle/ruby/version/gems` to just `bundle/gems`,
and keeping only `lib`, `bin` and licenses.
