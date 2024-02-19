#
# This file is part of ruby-ffi.
# For licensing, see LICENSE.SPECS
#

require File.expand_path(File.join(File.dirname(__FILE__), "spec_helper"))

describe FFI::Type do
  it 'has a memsize function', skip: RUBY_ENGINE != "ruby" do
    base_size = ObjectSpace.memsize_of(Object.new)

    size = ObjectSpace.memsize_of(FFI::Type.new(42))
    expect(size).to be > base_size
    base_size = size

    converter = Module.new do
      extend FFI::DataConverter

      def self.native_type
        @native_type_called = true
        FFI::Type::INT32
      end

      def self.to_native(val, ctx)
        @to_native_called = true
        ToNativeMap[val]
      end

      def self.from_native(val, ctx)
        @from_native_called = true
        FromNativeMap[val]
      end
    end

    size = ObjectSpace.memsize_of(FFI::Type::Mapped.new(converter))
    expect(size).to be > base_size
    base_size = size

    # Builtin types are larger as they also have a name and own ffi_type
    size = ObjectSpace.memsize_of(FFI::Type::Builtin::CHAR)
    expect(size).to be > base_size
  end

  it "should be shareable with Ractor", :ractor do
    expect(Ractor.shareable?(FFI::Type.new(5))).to eq(true)
  end

  describe :Builtin do
    it "should be shareable with Ractor", :ractor do
      expect(Ractor.shareable?(FFI::Type::INT32)).to eq(true)
    end
  end

  describe :Mapped do
    it "should be shareable with Ractor", :ractor do
      converter = Module.new do
        extend FFI::DataConverter

        def self.native_type
          FFI::Type::INT32
        end

        def self.to_native(val, ctx)
          ToNativeMap[val]
        end

        def self.from_native(val, ctx)
          FromNativeMap[val]
        end
      end
      expect(Ractor.shareable?(converter)).to eq(true)
      type = FFI::Type::Mapped.new(converter)
      expect(Ractor.shareable?(type)).to eq(true)
    end
  end
end
