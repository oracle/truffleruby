exclude :test_timeout_attribute, "spurious; NoMethodError: private method `timeout' called for #<UNIXSocket:(closed)>"
exclude :test_timeout_gets_exception, "spurious: NoMethodError: undefined method `timeout=' for #<UNIXSocket:(closed)>"
exclude :test_timeout_puts, "spurious; NoMethodError: undefined method `timeout=' for #<UNIXSocket:(closed)>"
exclude :test_timeout_read_exception, "spurious; NoMethodError: undefined method `timeout=' for #<UNIXSocket:(closed)>"