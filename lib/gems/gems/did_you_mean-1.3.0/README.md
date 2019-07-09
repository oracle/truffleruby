# did_you_mean [![Gem Version](https://badge.fury.io/rb/did_you_mean.svg)](https://rubygems.org/gems/did_you_mean) [![Build Status](https://travis-ci.org/yuki24/did_you_mean.svg?branch=master)](https://travis-ci.org/yuki24/did_you_mean)

## Installation

Ruby 2.3 and later ships with this gem and it will automatically be `require`d when a Ruby process starts up. No special setup is required.

## Examples

### NameError

#### Correcting a Misspelled Method Name

```ruby
methosd
# => NameError: undefined local variable or method `methosd' for main:Object
#    Did you mean?  methods
#                   method
```

#### Correcting a Misspelled Class Name

```ruby
OBject
# => NameError: uninitialized constant OBject
#    Did you mean?  Object
```

#### Suggesting an Instance Variable Name

```ruby
@full_name = "Yuki Nishijima"
first_name, last_name = full_name.split(" ")
# => NameError: undefined local variable or method `full_name' for main:Object
#    Did you mean?  @full_name
```

#### Correcting a Class Variable Name

```ruby
@@full_name = "Yuki Nishijima"
@@full_anme
# => NameError: uninitialized class variable @@full_anme in Object
#    Did you mean?  @@full_name
```

### NoMethodError

```ruby
full_name = "Yuki Nishijima"
full_name.starts_with?("Y")
# => NoMethodError: undefined method `starts_with?' for "Yuki Nishijima":String
#    Did you mean?  start_with?
```

### KeyError

```ruby
hash = {foo: 1, bar: 2, baz: 3}
hash.fetch(:fooo)
# => KeyError: key not found: :fooo
#    Did you mean?  :foo
```

## Experimental Features

Aside from the basic features above, the `did_you_mean` gem comes with experimental features. They can be enabled by calling `require 'did_you_mean/experimental'`.

Note that **these experimental features should never be enabled in production as they would impact Ruby's performance and use some unstable Ruby APIs.**

### Correcting an Instance Variable When It's Incorrectly Spelled

```ruby
require 'did_you_mean/experimental'

@full_name = "Yuki Nishijima"
@full_anme.split(" ")
# => NoMethodError: undefined method `split' for nil:NilClass
#    Did you mean?  @full_name
```

### Displaying a Warning When `initialize` is Incorrectly Spelled

```ruby
require 'did_you_mean/experimental'

class Person
  def intialize
    ...
  end
end
# => warning: intialize might be misspelled, perhaps you meant initialize?
```

## Verbose Formatter

This verbose formatter changes the error message format to take more lines/spaces so it'll be slightly easier to read the suggestions. This formatter can totally be used in any environment including production.

```ruby
OBject
# => NameError: uninitialized constant OBject
#    Did you mean?  Object

require 'did_you_mean/verbose'
OBject
# => NameError: uninitialized constant OBject
#
#        Did you mean? Object
#
```

## Disabling `did_you_mean`

Occasionally, you may want to disable the `did_you_mean` gem for e.g. debugging issues in the error object itself. You
can disable it entirely by specifying `--disable-did_you_mean` option to the `ruby` command:

```bash
$ ruby --disable-did_you_mean -e "1.zeor?"
-e:1:in `<main>': undefined method `zeor?' for 1:Integer (NameError)
```

When you do not have direct access to the `ruby` command (e.g. `rails console`, `irb`), you could applyoptions using the
`RUBYOPT` environment variable:

```bash
$ RUBYOPT='--disable-did_you_mean' irb
irb:0> 1.zeor?
# => NoMethodError (undefined method `zeor?' for 1:Integer)
```

### Getting the original error message

Sometimes, you do not want to disable the gem entirely, but need to get the original error message without suggestions
(e.g. testing). In this case, you could use the `#original_message` method on the error object:

```ruby
no_method_error = begin
                    1.zeor?
                  rescue NoMethodError => error
                    error
                  end

no_method_error.message
# => NoMethodError (undefined method `zeor?' for 1:Integer)
#    Did you mean?  zero?

no_method_error.original_message
# => NoMethodError (undefined method `zeor?' for 1:Integer)
```

## Contributing

1. Fork it (http://github.com/yuki24/did_you_mean/fork)
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Make sure all tests pass (`bundle exec rake`)
5. Push to the branch (`git push origin my-new-feature`)
6. Create new Pull Request

## License

Copyright (c) 2014-16 Yuki Nishijima. See MIT-LICENSE for further details.
