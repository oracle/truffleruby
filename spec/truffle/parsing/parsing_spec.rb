# truffleruby_primitives: true

# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

# Test cases are stored in YAML files of the following structure:
#
#   ``` yaml
#   subject: "short description"
#   description: "long description"
#   notes: >
#     "some additional details to explain what this case actually tests (optional)"
#   focused_on_node: "a node class name"
#   index: "integer, position of a node to focus on in AST when there are several such nodes and we need not the first one (optional)"
#   ruby: |
#     <Ruby source code>
#   ast: |
#     <Truffle AST>
#   ```
#
# The following attributes might be a multiline string,
# so leading and terminating blank characters should be removed:
# - description
# - source_code
# - expected_ast
#
# index is an optional attribute. Missing it means we need a node with index 0.
#
# Don't run specs in multi-context mode and with JIT because they affect AST:
# - in multi-context mode some nodes are replaced with dynamic-lexical-scope related ones
# - with JIT some nodes are replaced with optimized ones (e.g. OptimizedCallTarget)
#
# To regenerate fixture YAML files with actual AST run the following command:
#
#   OVERWRITE_PARSING_RESULTS=true jt -q test spec/truffle/parsing/parsing_spec.rb
#
# An approach with YAML.dump has some downsides:
# - it adds a line `---` at a file beginning
# - it removes unnecessary "" for string literals
# - it looses the folded style (with > indicator) for multiline blocks
# - it looses comments inside YAML document
# So just replace AST in a YAML file using a regexp.

overwrite = ENV['OVERWRITE_PARSING_RESULTS'] == 'true'

describe "Parsing" do
  require 'yaml'

  filenames = Dir.glob(File.dirname(__FILE__) + "/fixtures/**/*.yaml")

  filenames.each do |filename|
    yaml = YAML.safe_load_file(filename)
    subject, description, focused_on_node, index, source_code, expected_ast = yaml.values_at("subject", "description", "focused_on_node", "index", "ruby", "ast")

    guard -> { Primitive.vm_single_context? && !TruffleRuby.jit? } do
      it "a #{subject} (#{description.strip}) case is parsed correctly" do
        actual_ast = Truffle::Debug.parse_with_yarp_and_dump_truffle_ast(source_code.strip, focused_on_node, index.to_i).strip

        if overwrite
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
          # actual test check
          actual_ast.should == expected_ast.strip
        end
      end
    end
  end
end
