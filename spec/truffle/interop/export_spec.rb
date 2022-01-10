# Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

guard -> { Truffle::Interop.polyglot_bindings_access? } do
  describe "Truffle::Interop.export" do
    it "exports an object" do
      object = Object.new
      Truffle::Interop.export :exports_an_object, object
      Truffle::Interop.import(:exports_an_object).should == object
    end

    it "exports a primitive boolean" do
      Truffle::Interop.export :exports_a_primitive_number, true
      Truffle::Interop.import(:exports_a_primitive_number).should be_true
    end

    it "exports a primitive number" do
      Truffle::Interop.export :exports_a_primitive_number, 14
      Truffle::Interop.import(:exports_a_primitive_number).should == 14
    end

    it "exports a string" do
      Truffle::Interop.export :exports_a_string, 'hello'
      (Truffle::Interop.import(:exports_a_string) == 'hello').should be_true
    end

    it "exports a symbol, getting back a string" do
      Truffle::Interop.export :exports_a_symbol, :hello
      (Truffle::Interop.import(:exports_a_symbol) == 'hello').should be_true
    end

    it "exports a foreign object" do
      foreign_object = Truffle::Debug.foreign_object
      Truffle::Interop.export :exports_a_foreign_object, foreign_object
      Truffle::Interop.import(:exports_a_foreign_object).equal?(foreign_object).should be_true
    end

    it "exports a Java string" do
      Truffle::Interop.export :exports_a_java_string, Truffle::Interop.to_java_string('hello')
      Truffle::Interop.import(:exports_a_java_string).should == 'hello'
    end

    it "converts to Java when exporting a string" do
      Truffle::Interop.export :exports_a_string_with_conversion, 'hello'
      imported = Truffle::Interop.import_without_conversion(:exports_a_string_with_conversion)
      Truffle::Interop.java_string?(imported).should be_true
      Truffle::Interop.from_java_string(imported).should == 'hello'
    end

    it "can export a string without conversion to Java" do
      Truffle::Interop.export_without_conversion :exports_a_string_without_conversion, 'hello'
      imported = Truffle::Interop.import_without_conversion(:exports_a_string_without_conversion)
      Truffle::Interop.java_string?(imported).should be_false
      imported.should == 'hello'
    end

    it "can import a string without conversion from Java" do
      Truffle::Interop.export :imports_a_string_without_conversion, 'hello'
      imported = Truffle::Interop.import_without_conversion(:imports_a_string_without_conversion)
      Truffle::Interop.java_string?(imported).should be_true
      Truffle::Interop.from_java_string(imported).should == 'hello'
    end

    it "can be used with a string name" do
      Truffle::Interop.export 'string_name', 'hello'
      Truffle::Interop.import('string_name').should == 'hello'
    end

    it "can be used with a symbol name" do
      Truffle::Interop.export :symbol_name, 'hello'
      Truffle::Interop.import(:symbol_name).should == 'hello'
    end

    it "can be used with a mix of string and symbol names" do
      Truffle::Interop.export :mixed_name, 'hello'
      Truffle::Interop.import('mixed_name').should == 'hello'
    end

    it "returns the original exported value" do
      string = 'hello'
      Truffle::Interop.export(:returns_original_value, string).equal?(string).should be_true
    end
  end
end
