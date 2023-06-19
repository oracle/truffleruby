# truffleruby_primitives: true

# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

# Run the following command to regenerate fixture YAML files:
#   OVERWRITE_PARSING_RESULTS=true jt -q test spec/truffle/parsing/parsing_spec.rb
overwrite = ENV['OVERWRITE_PARSING_RESULTS'] == 'true'

describe "Parsing" do
  require 'yaml'

  filenames = Dir.glob(File.dirname(__FILE__) + "/fixtures/**/*.yaml")

  filenames.each do |filename|
    yaml = YAML.safe_load_file(filename)

    # The following attributes might be a multiline string,
    # so leading and terminating blank characters should be removed:
    # - description
    # - source_code
    # - expected_ast
    #
    # index is an optional attribute. Missing it means we need a node with index 0.
    subject, description, focused_on_node, index, source_code, expected_ast = yaml.values_at("subject", "description", "focused_on_node", "index", "ruby", "ast")

    it "a #{subject} (#{description.strip}) case is parsed correctly" do
      actual_ast = Truffle::Debug.parse_and_dump_truffle_ast(source_code.strip, focused_on_node, index.to_i).strip

      # debugging
      #
      # File.write("actual_ast_bytes.log", actual_ast.strip.bytes.map(&:to_s).join("\n"))
      # File.write("expected_ast_bytes.log", expected_ast.strip.bytes.map(&:to_s).join("\n"))

      # File.write("actual_ast.log", actual_ast)
      # File.write("expected_ast.log", expected_ast)

      if overwrite
        # Regenerate YAML files with actual AST
        #
        # An approach with YAML.dump has some downsides:
        # - it adds a line `---` at a file beginning
        # - it removes unnecessary "" for string literals
        # - it looses the folded style (with > indicator) for multiline blocks
        # - it looses comments inside YAML document
        #
        # So just replace AST with Regexp:
        example = File.read(filename)
        actual_ast_with_indentation = actual_ast.lines.map { |line| "  " + line }.join
        replaced = example.sub!(/^ast: \|.+\Z/m, "ast: |\n" + actual_ast_with_indentation)

        File.write filename, example

        # ensure it's still a valid YAML document
        YAML.safe_load_file(filename)

        unless replaced
          raise "The file #{filename} wasn't updated with actual AST"
        end
      else
        # the multi-context mode introduces some changes in the AST:
        # - static lexical scopes aren't set
        # - ReadConstantWithLexicalScopeNode is used instead of ReadConstantWithLexicalScopeNode
        # - ReadClassVariableNode.lexicalScopeNode is GetDynamicLexicalScopeNode instead of (ObjectLiteralNode object =  :: Object)
        # - WriteConstantNode.moduleNode uses DynamicLexicalScopeNode instead of (LexicalScopeNode lexicalScope =  :: Object)
        # - WriteClassVariableNode.lexicalScopeNode uses GetDynamicLexicalScopeNode instead of (LexicalScopeNode lexicalScope =  :: Object)
        if !Primitive.vm_single_context? &&
          expected_ast.include?("staticLexicalScope") ||
          expected_ast.include?("ReadConstantWithLexicalScopeNode") ||
          expected_ast.include?("ReadClassVariableNode") ||
          expected_ast.include?("WriteConstantNode") ||
          expected_ast.include?("WriteClassVariableNode")
          skip "Static lexical scopes are never set in multi context mode"
        end

        if TruffleRuby.jit?
          skip "Don't run parsing specs when JIT is enabled because it affects AST"
        end

        # actual test check
        actual_ast.should == expected_ast.strip
      end
    end
  end
end
