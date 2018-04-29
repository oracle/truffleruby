# did_you_mean [![Gem Version](https://badge.fury.io/rb/did_you_mean.svg)](https://rubygems.org/gems/did_you_mean) [![Build Status](https://travis-ci.org/yuki24/did_you_mean.svg?branch=master)](https://travis-ci.org/yuki24/did_you_mean)

'Did you mean?' experience in Ruby. No, Really.

## Installation

This gem will automatically be activated when a Ruby process starts up. No special setup is required.

## Examples

### NameError

#### Correcting a Misspelled Method Name

```ruby
class User
  attr_accessor :first_name, :last_name

  def to_s
    "#{f1rst_name} #{last_name}" # f1rst_name ???
  end
end

user.to_s
# => NameError: undefined local variable or method `f1rst_name' for #<User:0x0000000928fad8>
#
#     Did you mean? #first_name
#
```

#### Correcting a Misspelled Class Name

```ruby
class Book
  class TableOfContents
    # ...
  end
end

Book::TableofContents # TableofContents ???
# => NameError: uninitialized constant Book::TableofContents
#
#     Did you mean? Book::TableOfContents
#
```

#### Suggesting an instance variable name

```ruby
@full_name = "Yuki Nishijima"
first_name, last_name = full_name.split(" ")
# => NameError: undefined local variable or method `full_name' for main:Object
#
#     Did you mean? @full_name
#
```

### NoMethodError

```ruby
# In a Rails controller:
params.with_inddiferent_access
# => NoMethodError: undefined method `with_inddiferent_access' for {}:Hash
#
#     Did you mean? #with_indifferent_access
#
```

## 'Did You Mean' Experience is Everywhere

_did\_you\_mean_ gem automagically puts method corrections into the error message. This means you'll have the "Did you mean?" experience almost everywhere:

![Did you mean? on BetterErrors](https://raw.githubusercontent.com/yuki24/did_you_mean/master/doc/did_you_mean_example.png)

## Contributing

1. Fork it (http://github.com/yuki24/did_you_mean/fork)
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

Copyright (c) 2015 Yuki Nishijima. See MIT-LICENSE for further details.
