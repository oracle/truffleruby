# frozen_string_literal: true
# truffleruby_primitives: true

# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Coverage

  def self.supported?(mode)
    mode == :lines
  end

  def self.start(*arguments, **options)
    # We have to track if the :lines option was provided, as that calls for a
    # different result format
    @lines = !!options[:lines]
    Truffle::Coverage.enable
  end

  def self.result(stop: true, clear: true)
    result = peek_result
    Truffle::Coverage.disable if stop || clear
    Truffle::Coverage.enable if !stop && clear
    # We have to wrap the coverage lines array in a hash with the :lines key if
    # the :lines option was given
    if @lines
      result.transform_values! do |_,*lines_array|
        # need to add nil to the beginning of each lines array, because the
        # first line has index 1 and not 0
        { lines: lines_array.unshift(nil) }
      end
    end
    result
  end

  def self.running?
    Truffle::Coverage.enabled?
  end

  def self.peek_result
    Truffle::Coverage.result_array.to_h
  end

end
