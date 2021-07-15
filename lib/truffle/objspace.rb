# truffleruby_primitives: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::ObjSpace
  def self.count_nodes_method(method, nodes)
    node_stack = [Truffle.ast(method)]

    until node_stack.empty?
      node = node_stack.pop
      next if node.nil?

      name = node.first
      children = node.drop(1)
      nodes[name] ||= 0
      nodes[name] += 1
      node_stack.push(*children)
    end
  end
end

module ObjectSpace
  module_function

  def count_nodes(nodes = {})
    ObjectSpace.each_object(Module) do |mod|
      mod.methods(false).each do |name|
        Truffle::ObjSpace.count_nodes_method mod.method(name), nodes
      end

      mod.private_methods(false).each do |name|
        Truffle::ObjSpace.count_nodes_method mod.method(name), nodes
      end
    end

    ObjectSpace.each_object(Proc) do |proc|
      Truffle::ObjSpace.count_nodes_method proc, nodes
    end

    ObjectSpace.each_object(Method) do |method|
      Truffle::ObjSpace.count_nodes_method method, nodes
    end

    ObjectSpace.each_object(UnboundMethod) do |umethod|
      Truffle::ObjSpace.count_nodes_method umethod, nodes
    end

    nodes
  end

  def count_objects_size(hash = {})
    total = 0
    ObjectSpace.each_object(Class) do |klass|
      per_klass = memsize_of_all(klass)
      hash[klass.name.to_sym] = per_klass unless klass.name.nil?
      total += per_klass
    end
    hash[:TOTAL] = total
    hash
  end

  def count_tdata_objects(hash = {})
    ObjectSpace.each_object do |object|
      klass = object.class
      hash[klass] ||= 0
      hash[klass] += 1
    end
    hash
  end

  def dump(object, output: :string)
    case output
    when :string
      require 'json'
      json = {
        address: '0x' + object.object_id.to_s(16),
        class: '0x' + object.class.object_id.to_s(16),
        memsize: memsize_of(object),
        flags: { }
      }
      case object
      when String
        json.merge!({
          type: 'STRING',
          bytesize: object.bytesize,
          value: object,
          encoding: object.encoding.name
        })
      when Array
        json.merge!({
          type: 'ARRAY',
          length: object.size
        })
      when Hash
        json.merge!({
          type: 'HASH',
          size: object.size
        })
      else
        json.merge!({
          type: 'OBJECT',
          length: object.instance_variables.size
        })
      end
      JSON.generate(json)
    when :file
      require 'tempfile'
      f = Tempfile.new(['rubyobj', '.json'])
      f.write dump(object, output: :string)
      f.close
      f.path
    when :stdout
      puts dump(object, output: :string)
      nil
    end
  end

  def dump_all(output: :file)
    case output
    when :string
      objects = []
      ObjectSpace.each_object do |object|
        objects.push dump(object)
      end
      objects.join("\n")
    when :file
      require 'tempfile'
      f = Tempfile.new(['ruby', '.json'])
      f.write dump_all(output: :string)
      f.close
      f.path
    when :stdout
      puts dump_all(output: :string)
      nil
    when IO
      output.write dump_all(output: :string)
      nil
    end
  end

  def memsize_of(object)
    size = Truffle::ObjSpace.memsize_of(object)

    memsizer = Primitive.object_hidden_var_get object, Truffle::CExt::DATA_MEMSIZER
    if memsizer
      size + memsizer.call
    else
      size
    end
  end

  def memsize_of_all(klass = BasicObject)
    total = 0
    ObjectSpace.each_object(klass) do |object|
      total += ObjectSpace.memsize_of(object)
    end
    total
  end

  def reachable_objects_from(object)
    Truffle::ObjSpace.adjacent_objects(object)
  end

  def reachable_objects_from_root
    { 'roots' => Truffle::ObjSpace.root_objects }
  end

  def trace_object_allocations
    trace_object_allocations_start
    begin
      yield
    ensure
      trace_object_allocations_stop
    end
  end

  def trace_object_allocations_debug_start
    trace_object_allocations_start
  end

  def trace_object_allocations_start
    Truffle::ObjSpace.trace_allocations_start
  end

  def trace_object_allocations_stop
    Truffle::ObjSpace.trace_allocations_stop
  end

  def trace_object_allocations_clear
    Truffle::ObjSpace.trace_allocations_clear
  end

  def allocation_class_path(object)
    Primitive.allocation_class_path(object)
  end

  def allocation_generation(object)
    Primitive.allocation_generation(object)
  end

  def allocation_method_id(object)
    Primitive.allocation_method_id(object)
  end

  def allocation_sourcefile(object)
    Primitive.allocation_sourcefile(object)
  end

  def allocation_sourceline(object)
    Primitive.allocation_sourceline(object)
  end
end
