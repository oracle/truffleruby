# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module KernelOperations
    def self.to_enum_with_size(enum, method, size_method)
      enum.to_enum(method) { enum.send(size_method) }
    end

    def self.define_hooked_variable(name, getter, setter, defined = proc { 'global-variable' })
      getter = Truffle::Graal.always_split(getter) if getter.arity == 1
      setter = Truffle::Graal.always_split(setter) if setter.arity == 2
      defined = Truffle::Graal.always_split(defined) if defined.arity == 1
      define_hooked_variable_with_is_defined(name, getter, setter, defined)
    end

    def self.define_read_only_global(name, getter)
      setter = -> _ { raise NameError, "#{name} is a read-only variable." }
      define_hooked_variable(name, getter, setter)
    end

    LOAD_PATH = []
    LOADED_FEATURES = []

    define_read_only_global(:$LOAD_PATH, -> { LOAD_PATH })
    define_read_only_global(:$LOADED_FEATURES, -> { LOADED_FEATURES })

    alias $: $LOAD_PATH
    alias $-I $LOAD_PATH
    alias $" $LOADED_FEATURES

    # The runtime needs to access these values, so we want them to be set in the variable storage.
    global_variable_set(:$LOAD_PATH, LOAD_PATH)
    global_variable_set(:$LOADED_FEATURES, LOADED_FEATURES)

    define_read_only_global(:$*, -> { ARGV })

    define_read_only_global(:$-a, -> { nil })
    define_read_only_global(:$-l, -> { nil })
    define_read_only_global(:$-p, -> { nil })

    define_hooked_variable(
      :$/,
      -> { global_variable_get(:$/) },
      -> v { if v && !Truffle::Type.object_kind_of?(v, String)
               raise TypeError, '$/ must be a String'
             end
             global_variable_set(:$/, v) })

    $/ = "\n".freeze

    alias $-0 $/

    define_hooked_variable(
      :'$,',
      -> { global_variable_get(:$,) },
      -> v { if v && !Truffle::Type.object_kind_of?(v, String)
               raise TypeError, '$, must be a String'
             end
             global_variable_set(:$,, v) })

    $, = nil # It should be defined by the time boot has finished.

    $= = false

    define_hooked_variable(
      :$VERBOSE,
      -> { global_variable_get(:$VERBOSE) },
      -> v { v = if v.nil?
                   nil
                 else
                   !!v
                 end
             global_variable_set(:$VERBOSE, v) })

    Truffle::Boot.redo do
      $DEBUG = Truffle::Boot.get_option 'debug'
      $VERBOSE = case Truffle::Boot.get_option 'verbosity'
                 when :TRUE
                   true
                 when :FALSE
                   false
                 when :NIL
                   nil
                 end
    end

    alias $-d $DEBUG
    alias $-v $VERBOSE
    alias $-w $VERBOSE

    define_hooked_variable(
      :$stdout,
      -> { global_variable_get(:$stdout) },
      -> v { raise TypeError, "$stdout must have a write method #{v.class} given." unless v.respond_to?(:write)
             global_variable_set(:$stdout, v) })

    alias $> $stdout

    define_hooked_variable(
      :$stderr,
      -> { global_variable_get(:$stderr) },
      -> v { raise TypeError, "$stderr must have a write method #{v.class} given." unless v.respond_to?(:write)
             global_variable_set(:$stderr, v) })
  end
end
