#
# This file is part of ruby-ffi.
# For licensing, see LICENSE.SPECS
#

require File.expand_path(File.join(File.dirname(__FILE__), "spec_helper"))

describe "FFI::FunctionType", skip: RUBY_ENGINE != "ruby" do
  it 'is initialized with return type and a list of parameter types' do
    function_type = FFI::FunctionType.new(:int, [ :char, :ulong ])
    expect(function_type.return_type).to be == FFI::Type::Builtin::INT
    expect(function_type.param_types).to be == [ FFI::Type::Builtin::CHAR, FFI::Type::Builtin::ULONG ]
  end

  it 'has a memsize function' do
    base_size = ObjectSpace.memsize_of(Object.new)

    function_type = FFI::FunctionType.new(:int, [])
    size = ObjectSpace.memsize_of(function_type)
    expect(size).to be > base_size

    base_size = size
    function_type = FFI::FunctionType.new(:int, [:char])
    size = ObjectSpace.memsize_of(function_type)
    expect(size).to be > base_size
  end
end
