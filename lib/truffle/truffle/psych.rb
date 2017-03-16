# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

Psych = Truffle::Psych

require 'psych/visitors'

module Psych

  def self.libyaml_version
    # TODO CS 23-Sep-15 hardcoded this for now - uses resources to read
    [1, 14, 0]
  end


  class ClassLoader

    def path2class(path)
      eval("::#{path}")
    end

  end

  module Visitors
    class ToRuby

      def build_exception(klass, mesg)
        klass.new(mesg)
      end

    end

    class YAMLTree

      def private_iv_get(target, prop)
        target.instance_variable_get("@#{prop}")
      end

    end
  end

end

require 'psych/syntax_error.rb'
