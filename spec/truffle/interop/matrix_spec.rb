# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'bigdecimal'

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

class PolyglotMatcher
  def initialize(expected)
    @expected = expected
  end

  def matches?(actual)
    @actual = actual
    @actual == @expected || (@expected.is_a?(Symbol) && @actual == @expected.to_s)
  end

  def failure_message
    ["Expected #{MSpec.format(@actual)}",
     "to have same value and type as #{MSpec.format(@expected)}"]
  end

  def negative_failure_message
    ["Expected #{MSpec.format(@actual)}",
     "not to have same value or type as #{MSpec.format(@expected)}"]
  end
end

module MSpecMatchers
  private def polyglot_match(expected)
    PolyglotMatcher.new(expected)
  end
end

describe 'Interop:' do

  # TODO (pitr-ch 13-Feb-2020): should test all InteropLibrary messages against all possible Ruby types we care about.
  # TODO (pitr-ch 25-Feb-2020): Truffle::Interop.* test with different objects as indexes and names, test their conversion to long/String
  #
  # TODO (pitr-ch 25-Feb-2020): documentation
  # TODO (pitr-ch 02-Mar-2020): - translation of InteropExceptions to RubyExceptions
  # TODO (pitr-ch 25-Feb-2020): - exceptions accepted in dynamic Ruby API
  # TODO (pitr-ch 02-Mar-2020): - generate documentation for explicit API (Truffle::Interop.*)
  #
  # TODO (pitr-ch 27-Feb-2020): deal with Arity exception when calling a ruby method from different language
  #   Ruby ArgumentError should be translated properly to ArityException
  # TODO (pitr-ch 27-Feb-2020): Over time dynamic ruby api under TrufflerRuby, explicit API under Polyglot
  #
  # == Dynamic Ruby API   ->
  # * Use custom exceptions to never confuse implementation bug with
  #   what is suppose to be thrown on the Java side.
  # * Low level internal API, fine to be explicit and more complicated.
  # * no polyglot dynamic API raising it arity exception, therefore omitted
  #
  # Interop::UnsupportedMessageException -> UnsupportedMessageException
  # Interop::InvalidArrayIndexException  -> InvalidArrayIndexException
  # Interop::UnknownIdentifierException  -> UnknownIdentifierException
  # Interop::UnsupportedTypeException    -> UnsupportedTypeException
  # e.is_a?(StandardError)               -> is alllowed to bubble up, it is a TruffleException
  #
  # * All Interop::* exceptions above inherit from Interop::InteropException < Exception
  #   Stands apart from normal Ruby exceptions so it cannot be rescued accidentally.
  # * Interop.invoke_member should check arity explicitly and raise ArgumentError (<- ArityException).
  #   Other exceptions are allowed to bubble up, they are TruffleExceptions
  #
  # == Exception risen by Interop methods <-
  # Polyglot::UnsupportedMessageError     <- UnsupportedMessageException
  #   inherits from StandardError
  # IndexError                            <- InvalidArrayIndexException
  # NameError                             <- UnknownIdentifierException
  #   NoMethodError if the message was invokeMember, NoMethodError inherits from NameError so it is fine
  # ArgumentError                         <- ArityException
  # TypeError                             <- UnsupportedTypeException
  #
  # # TODO (pitr-ch 02-Mar-2020): following
  # * same translation will be also applied in the any special forms applied to foreign objects like
  #   `foreign[-1] = :v` => IndexError if index is invalid
  #   `foreign.call()`   => Polyglot::UnsupportedMessageError if not executable
  #   `foreign.a_method` => NoMethodError if the member is not invocable
  #                         UnknownIdentifierException translated to NoMethodError on invokeMember message

  # Other notes: marking modules and Interop::* exceptions belong under TruffleRuby namespace
  # and explicit API belongs under Polyglot namespace. Since the first one is for internal
  # TruffleRuby implementation of polyglot behaviour the other one is outward, to talk to other languages.


  def read_array_elemet(index)
    raise Interop::InvalidArrayIndexException if index < 0
    @data.fetch index
  rescue IndexError
    42
  end


  INSPECTION  = -> v { code v.inspect }
  AN_INSTANCE = -> v do
    class_name = v.class.name
    an         = class_name.start_with?('A', 'E', 'I', 'O', 'U')
    (an ? 'an ' : 'a ') + code(class_name)
  end

  def code(string)
    '`' + string + '`'
  end

  Subject = Struct.new(:value_constructor, :key, :name, :doc, :frozen, :explanation) do
    def initialize(value_constructor, name: INSPECTION, doc: false, frozen: nil, explanation: nil)
      super value_constructor, nil, name, doc,
            frozen.nil? ? !value_constructor.is_a?(Proc) : frozen,
            explanation

      if name.is_a? Proc
        self.name = self.name.call value
      else
        self.name ||= value.inspect
      end
    end

    def value
      self.frozen ? value_constructor : value_constructor.call
    end
  end

  Message = Struct.new(:name, :tests, :rest) do
    def initialize(name, *tests, rest)
      raise ArgumentError, name.inspect unless Symbol === name
      raise ArgumentError, tests.inspect unless tests.all? { |v| Test === v }
      raise ArgumentError, rest.inspect unless rest.nil? || (Test === rest && rest.subjects.empty?)
      super name, tests, rest
    end
  end

  Test = Struct.new(:description, :subjects, :test, :mode) do
    def initialize(description, *subjects, mode: :single, &test)
      raise ArgumentError, description.inspect unless String === description
      raise ArgumentError, test.inspect unless Proc === test
      super description, subjects.map { |k| SUBJECTS.fetch(k) { EXTRA_SUBJECTS.fetch(k) } }, test, mode
    end

    def spec_describe(subject)
      this = self
      describe("#{subject.name} (#{subject.key.inspect}),") { this.spec_it subject }
    end

    def spec_it(subject)
      description = self.description
      test        = self.test

      case mode
      when :single
        it "it #{description}" do
          test.call subject.value
        end
      when :values
        SUBJECTS.each do |_, subject_as_value|
          it "it #{description} where value is #{subject_as_value.name} (#{subject_as_value.key.inspect})" do
            test.call subject.value, subject_as_value.value
          end
        end
      else
        raise ArgumentError, "unknown mode: #{mode.inspect}"
      end
    end
  end

  Delimiter = Struct.new(:text)

  array_polyglot_methods = %w[
      polyglot_has_array_elements?
      polyglot_array_size
      polyglot_read_array_element
      polyglot_write_array_element
      polyglot_remove_array_element
      polyglot_array_element_readable?
      polyglot_array_element_modifiable?
      polyglot_array_element_insertable?
      polyglot_array_element_removable?]

  member_polyglot_methods = %w[
      polyglot_has_members?
      polyglot_members
      polyglot_read_member
      polyglot_write_member
      polyglot_remove_member
      polyglot_invoke_member
      polyglot_member_readable?
      polyglot_member_modifiable?
      polyglot_member_removable?
      polyglot_member_insertable?
      polyglot_member_invocable?
      polyglot_member_internal?
      polyglot_has_member_read_side_effects?
      polyglot_has_member_write_side_effects?]

  pointer_polyglot_methods = %w[
      polyglot_pointer?
      polyglot_as_pointer
      polyglot_to_native]

  interop_library_reference = "  The methods correspond to messages defined in\n" +
      "  [InteropLibrary](https://www.graalvm.org/truffle/javadoc/com/oracle/truffle/api/interop/InteropLibrary.html)."


  # TODO (pitr-ch 20-Feb-2020): test objects from CExt ?
  # TODO (pitr-ch 13-Feb-2020): add number corner cases

  SUBJECTS = {
      nil:            Subject[nil, doc: true],
      false:          Subject[false, doc: true],
      true:           Subject[true, doc: true],

      symbol:         Subject[:symbol, doc: true],
      strange_symbol: Subject[:"strange -=@\0x2397"],
      empty_string:   Subject[-> { "" }],
      string:         Subject[-> { "string" }, name: AN_INSTANCE, doc: true],

      # TODO (pitr-ch 24-Feb-2020): has array interface?, test it
      # java_string:     Subject[Truffle::Interop.to_java_string("Java-string"),
      #                          # TODO (pitr-ch 21-Feb-2020): where do we test foreign objects? (just example here for now)
      #                          # TODO (pitr-ch 24-Feb-2020): mark this as foreign object and test it only in tests of Interop.* methods
      #                          doc: true, name: 'a ' + code('java.lang.String')],

      zero:          Subject[0],
      small_integer: Subject[1, name: AN_INSTANCE, doc: true],
      zero_float:    Subject[0.0],
      small_float:   Subject[1.0, name: AN_INSTANCE, doc: true],
      big_decimal:   Subject[BigDecimal('1e99'), name: AN_INSTANCE, doc: true],

      object:        Subject[-> { Object.new }, name: AN_INSTANCE, doc: true],
      class:         Subject[-> { Class.new }, name: AN_INSTANCE, doc: true],
      module:        Subject[-> { Module.new }, name: AN_INSTANCE],
      hash:          Subject[-> { {} }, name: AN_INSTANCE, doc: true],
      array:         Subject[-> { [] }, name: AN_INSTANCE, doc: true],

      proc:          Subject[proc { |v| v }, name: code("proc {...}"), doc: true, frozen: true],
      lambda:        Subject[-> v { v }, name: code("lambda {...}"), doc: true, frozen: true],
      method:        Subject[
                         Object.new.tap do |o|
                           def o.foo(v)
                             v
                           end
                         end.method(:foo),
                         name: AN_INSTANCE, doc: true, frozen: true],

      # TODO (pitr-ch 02-Mar-2020): better pointer for doc
      pointer:          Subject[-> { Truffle::FFI::Pointer.new(0) },
                                name:        AN_INSTANCE,
                                doc:         true,
                                explanation: "An Object implementing Polyglot pointer API."],
      polyglot_pointer: Subject[-> { TruffleInteropSpecs::PolyglotPointer.new },
                                name:        AN_INSTANCE,
                                doc:         true,
                                explanation: "An Object which implements `polyglot_*` methods for pointer, that are:\n  " +
                                                 pointer_polyglot_methods.map { |m| code(m) }.join(",\n  ") + ".\n" +
                                                 interop_library_reference],

      polyglot_object:  Subject[-> { TruffleInteropSpecs::PolyglotMember.new },
                                doc:         true,
                                name:        "polyglot object",
                                explanation: "An Object which implements `polyglot_*` methods for members, that are:\n  " +
                                                 member_polyglot_methods.map { |m| code(m) }.join(",\n  ") + ".\n" +
                                                 interop_library_reference],
      polyglot_array:   Subject[-> { TruffleInteropSpecs::PolyglotArray.new },
                                doc:         true,
                                name:        "polyglot array",
                                explanation: "An Object which implements `polyglot_*` methods for Array, that are:\n  " +
                                                 array_polyglot_methods.map { |m| code(m) }.join(",\n  ") + ".\n" +
                                                 interop_library_reference]
  }.each { |key, subject| subject.key = key }

  # not part of the standard matrix, not considered in last rest case
  EXTRA_SUBJECTS = {
      polyglot_int_array: Subject[-> { TruffleInteropSpecs::PolyglotArray.new { |v| Integer === v } },
                                  doc:         true,
                                  name:        "polyglot int array",
                                  explanation: "An Object which implements `polyglot_*` methods for Array allowing only Integers to be stored"]
  }.each { |key, subject| subject.key = key }

  def predicate(name, is, *message_args, &setup)
    -> subject do
      setup.call subject if setup
      Truffle::Interop.send(name, subject, *message_args).send(is ? :should : :should_not, be_true)
    end
  end

  def unsupported_test(precise = true, &action)
    Test.new("fails with UnsupportedError") do |subject|
      error_matcher = precise ? Polyglot::UnsupportedMessageError : -> v { Polyglot::UnsupportedMessageError === v || RuntimeError === v || TypeError === v }
      -> { action.call(subject) }.should raise_error(error_matcher, /Message not supported|unsupported message/)
    end
  end

  def array_element_predicate(message, predicate, insert_on_true_case)
    insert = -> subject { Truffle::Interop.write_array_element(subject, 0, Object.new) }
    Message[message,
            Test.new("returns true if there is #{insert_on_true_case ? 'a' : 'no'} value on the given index",
                     :array, :polyglot_array,
                     &predicate(predicate, true, 0, &(insert if insert_on_true_case))),
            Test.new("returns false if there is #{!insert_on_true_case ? 'a' : 'no'} value on the given index",
                     :array, :polyglot_array,
                     &predicate(predicate, false, 0, &(insert unless insert_on_true_case))),
            Test.new("returns false", &predicate(predicate, false, 0))]
  end

  MESSAGES = [
      Delimiter["`null` related messages"],
      Message[:isNUll,
              Test.new("returns true", :nil, &predicate(:null?, true)),
              Test.new("returns false", &predicate(:null?, false))],


      Delimiter["`boolean` related messages"],
      Message[:isBoolean,
              Test.new("returns true", :true, :false, &predicate(:boolean?, true)),
              Test.new("returns false", &predicate(:boolean?, false))],
      Message[:asBoolean,
              Test.new("returns the receiver", :true, :false) do |subject|
                Truffle::Interop.as_boolean(subject).should == subject
              end,
              unsupported_test { |subject| Truffle::Interop.as_boolean(subject) }],


      Delimiter["Messages related to executable objects"],
      Message[:isExecutable,
              Test.new("returns true", :proc, :lambda, :method, &predicate(:executable?, true)),
              Test.new("returns false", &predicate(:executable?, false))],
      Message[:execute,
              Test.new("returns the result of the execution", :proc, :lambda, :method) do |subject|
                value = Object.new
                Truffle::Interop.execute(subject, value).should == value
              end,
              Test.new("fails with `ArgumentError` an error when the number of arguments is wrong", :lambda, :method) do |subject|
                value = Object.new
                -> { Truffle::Interop.execute(subject, value, value) }.should raise_error(ArgumentError)
              end,
              Test.new("returns the result of the execution even though the number of arguments is wrong", :proc) do |subject|
                value = Object.new
                Truffle::Interop.execute(subject, value, value).should == value
              end,
              unsupported_test { |subject| Truffle::Interop.execute(subject) }],

      Delimiter["Messages related to pointers"],
      Message[:isPointer,
              Test.new("returns true", :pointer, &predicate(:pointer?, true)),
              Test.new("returns false", &predicate(:pointer?, false))],
      Message[:asPointer,
              Test.new("returns pointer address", :pointer) do |subject|
                Truffle::Interop.as_pointer(subject).should == 0
              end,
              unsupported_test { |subject| Truffle::Interop.as_pointer(subject) }],
      Message[:toNative,
              Test.new('converts receiver from object returning isPointer => false to object returning isPointer => true if possible',
                       :pointer, :polyglot_pointer) do |subject|
                case subject
                when Truffle::FFI::Pointer
                  Truffle::Interop.pointer?(subject).should be_true
                when TruffleInteropSpecs::PolyglotPointer
                  Truffle::Interop.pointer?(subject).should_not be_true
                else
                  raise "unsupported subject"
                end
                Truffle::Interop.to_native(subject)
                Truffle::Interop.pointer?(subject).should be_true
              end,
              unsupported_test { |subject| Truffle::Interop.as_pointer(subject) }],

      Delimiter["Array related messages (incomplete)"],
      Message[:hasArrayElements,
              Test.new("returns true", :array, :polyglot_array, :polyglot_int_array, &predicate(:size?, true)),
              Test.new("returns false", &predicate(:size?, false))],
      Message[:getArraySize,
              Test.new("returns size of the array", :array, :polyglot_array) do |subject|
                Truffle::Interop.array_size(subject).should == 0
                value = Object.new
                Truffle::Interop.write_array_element(subject, 0, value)
                Truffle::Interop.array_size(subject).should == 1
                Truffle::Interop.remove_array_element(subject, 0)
                Truffle::Interop.array_size(subject).should == 0
              end,
              unsupported_test { |subject| Truffle::Interop.array_size(subject) }],
      Message[:readArrayElement,
              Test.new("returns stored value when it is present on the given valid index", :array, :polyglot_array, mode: :values) do |subject, value|
                Truffle::Interop.write_array_element(subject, 0, value)
                Truffle::Interop.read_array_element(subject, 0).should polyglot_match value
              end,
              Test.new("fails with `IndexError` when a value is not present on the index or the index is invalid", :array, :polyglot_array) do |subject|
                -> { Truffle::Interop.read_array_element(subject, 0) }.should raise_error(IndexError)
                -> { Truffle::Interop.read_array_element(subject, -1) }.should raise_error(IndexError)
              end,
              unsupported_test { |subject| Truffle::Interop.read_array_element(subject, 0) }],
      Message[:writeArrayElement,
              Test.new("stores a value at a given index", :array, :polyglot_array, mode: :values) do |subject, value|
                Truffle::Interop.write_array_element(subject, 0, value)
                Truffle::Interop.read_array_element(subject, 0).should polyglot_match value
                Truffle::Interop.write_array_element(subject, 10, value)
                Truffle::Interop.read_array_element(subject, 10).should polyglot_match value
              end,
              Test.new("fails with `IndexError` when a index is invalid", :array, :polyglot_array) do |subject|
                -> { Truffle::Interop.write_array_element(subject, -1, Object.new) }.should raise_error(IndexError)
              end,
              Test.new("fails with `TypeError` when the value is invalid", :polyglot_int_array) do |subject|
                Truffle::Interop.write_array_element(subject, 0, 42)
                Truffle::Interop.read_array_element(subject, 0).should == 42
                -> { Truffle::Interop.write_array_element(subject, 1, Object.new) }.should raise_error(TypeError)
              end,
              unsupported_test { |subject| Truffle::Interop.write_array_element(subject, 0, Object.new) }],
      Message[:removeArrayElement,
              Test.new("removes a value when the value is present on a valid index", :array, :polyglot_array) do |subject|
                value = Object.new
                Truffle::Interop.write_array_element(subject, 0, value)
                Truffle::Interop.remove_array_element(subject, 0)
                Truffle::Interop.array_element_readable?(subject, 0).should be_false
              end,
              Test.new("fails with IndexError when the value is not present on a valid index", :array, :polyglot_array) do |subject|
                -> { Truffle::Interop.remove_array_element(subject, 0) }.should raise_error(IndexError)
              end,
              unsupported_test { |subject| Truffle::Interop.remove_array_element(subject, 0) }],
      array_element_predicate(:isArrayElementReadable, :array_element_readable?, true),
      array_element_predicate(:isArrayElementModifiable, :array_element_modifiable?, true),
      array_element_predicate(:isArrayElementInsertable, :array_element_insertable?, false),
      array_element_predicate(:isArrayElementRemovable, :array_element_removable?, true)
  ]

  at_exit do
    # TODO (pitr-ch 25-Feb-2020): Can we write the md file always in all the environments?
    File.open(File.join(__dir__, '..', '..', '..', 'doc', 'contributor', 'interop_details.md'), 'w') do |out|

      out.print "<!-- Generated by spec/truffle/interop/matrix_spec.rb -->\n\n"
      out.print "### Detailed definition of polyglot behaviour is given for\n\n"

      { **SUBJECTS, **EXTRA_SUBJECTS }.each do |_, subject|
        next unless subject.doc
        out.print '- ' + subject.name
        out.puts subject.explanation ? (" - " + subject.explanation) : nil
      end

      out.puts

      MESSAGES.each do |message|
        if message.is_a? Delimiter
          out.puts "\n### #{message.text}"
          next
        end
        out.puts "\nWhen interop message `#{message.name}` is send"

        format_subjects = -> subjects { subjects.select(&:doc).map(&:name).join(', ') }
        message.tests.each do |test|
          out.puts "- to: " + format_subjects.call(test.subjects) + "\n  it " + test.description + "."
        end

        # remaining_keys = SUBJECTS.keys - message.tests.map(&:subjects).flatten
        # remaining_subjects = SUBJECTS.values_at(*remaining_keys)

        if message.rest
          out.puts "- to all other objects not mentioned above\n  it " + message.rest.description + "."
        end
      end
    end
  end

  def spec_describe(description, subject, test)
    describe "#{subject.name} (#{subject.key.inspect})," do
      it "it #{description}" do
        test.call subject.value
      end
    end
  end

  MESSAGES.each do |message|
    next if message.is_a? Delimiter

    tests = message.tests
    describe "When interop message #{message.name} is send to" do
      subjects_tested = {}

      tests.each do |test|
        test.subjects.each do |subject|
          subjects_tested[subject.key] = subject

          test.spec_describe subject
        end
      end

      if message.rest
        SUBJECTS.each do |key, subject|
          next if subjects_tested.key? key
          message.rest.spec_describe subject
        end
      end
    end
  end

end
