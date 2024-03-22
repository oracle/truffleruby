# truffleruby_primitives: true

# Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

guard -> { TruffleRuby.jit? } do # this test needs splitting
  describe "Inline caching for dynamically-created Regexp works for" do
    before :each do
      @performance_warnings, Warning[:performance] = Warning[:performance], true
    end

    after :each do
      Warning[:performance] = @performance_warnings
    end

    it "Regexp.union with 1 argument" do
      # Check that separate call sites with fixed input does not warn
      -> {
        Regexp.union("a")
        Regexp.union("b")
        Regexp.union("c")
        Regexp.union("d")
        Regexp.union("e")
        Regexp.union("f")
        Regexp.union("g")
        Regexp.union("h")
        Regexp.union("i")
        Regexp.union("j")
      }.should_not complain

      # Check that calling it with many different inputs has the warning.
      -> {
        ("a".."z").each do |pattern|
          Regexp.union(pattern)
        end
      }.should complain(/unbounded creation of regexps/)
    end

    it "Regexp.union with multiple arguments" do
      # Check that separate call sites with fixed input does not warn
      -> {
        Regexp.union("h", "a")
        Regexp.union("h", "b")
        Regexp.union("h", "c")
        Regexp.union("h", "d")
        Regexp.union("h", "e")
        Regexp.union("h", "f")
        Regexp.union("h", "g")
        Regexp.union("h", "h")
        Regexp.union("h", "i")
        Regexp.union("h", "j")
      }.should_not complain

      # Check that calling it with many different inputs has the warning.
      -> {
        ("a".."z").each do |pattern|
          Regexp.union("h", pattern)
        end
      }.should complain(/unbounded creation of regexps/)
    end

    it "String#scan" do
      # Check that separate call sites with fixed input does not warn
      -> {
        "zzz".scan("a")
        "zzz".scan("b")
        "zzz".scan("c")
        "zzz".scan("d")
        "zzz".scan("e")
        "zzz".scan("f")
        "zzz".scan("g")
        "zzz".scan("h")
        "zzz".scan("i")
        "zzz".scan("j")
      }.should_not complain

      # Check that calling it with many different inputs has the warning.
      -> {
        # "a".."z" and not just "a".."j" because there can be some late heuristic megamorphic splitting by TRegex (ExecCompiledRegexNode)
        ("a".."z").each do |pattern|
          "zzz".scan(pattern)
        end
      }.should complain(/unbounded creation of regexps/)
    end
  end
end
