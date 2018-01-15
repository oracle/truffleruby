# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module KernelOperations
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
  end
end

Truffle::KernelOperations.define_read_only_global(:$LOAD_PATH, -> { Truffle::KernelOperations::LOAD_PATH })
Truffle::KernelOperations.define_read_only_global(:$LOADED_FEATURES, -> { Truffle::KernelOperations::LOADED_FEATURES })

alias $: $LOAD_PATH
alias $-I $LOAD_PATH
alias $" $LOADED_FEATURES

# The runtime needs to access these values, so we want them to be set in the variable storage.
Truffle::KernelOperations.global_variable_set(:$LOAD_PATH, Truffle::KernelOperations::LOAD_PATH)
Truffle::KernelOperations.global_variable_set(:$LOADED_FEATURES, Truffle::KernelOperations::LOADED_FEATURES)

Truffle::KernelOperations.define_read_only_global(:$*, -> { ARGV })

Truffle::KernelOperations.define_read_only_global(:$-a, -> { nil })
Truffle::KernelOperations.define_read_only_global(:$-l, -> { nil })
Truffle::KernelOperations.define_read_only_global(:$-p, -> { nil })

Truffle::KernelOperations.define_hooked_variable(
  :$/,
  -> { Truffle::KernelOperations.global_variable_get(:$/) },
  -> v { if v && !Truffle::Type.object_kind_of?(v, String)
           raise TypeError, '$/ must be a String'
         end
         Truffle::KernelOperations.global_variable_set(:$/, v) })

$/ = "\n".freeze

alias $-0 $/

Truffle::KernelOperations.define_hooked_variable(
  :$,,
  -> { Truffle::KernelOperations.global_variable_get(:$,) },
  -> v { if v && !Truffle::Type.object_kind_of?(v, String)
           raise TypeError, '$, must be a String'
         end
         Truffle::KernelOperations.global_variable_set(:$,, v) })

$, = nil # It should be defined by the time boot has finished.

Truffle::KernelOperations.define_hooked_variable(
  :$VERBOSE,
  -> { Truffle::KernelOperations.global_variable_get(:$VERBOSE) },
  -> v { v = if v.nil?
               nil
             else
               !!v
             end
         Truffle::KernelOperations.global_variable_set(:$VERBOSE, v) })

$VERBOSE = case Truffle::Boot.get_option 'verbosity'
           when :TRUE
             true
           when :FALSE
             false
           when :NIL
             nil
           end

alias $-v $VERBOSE
alias $-w $VERBOSE
