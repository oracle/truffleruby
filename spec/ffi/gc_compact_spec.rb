# -*- rspec -*-
# encoding: utf-8
#
# Tests to verify correct implementation of compaction callbacks in rb_data_type_t definitions.
#
# Compaction callbacks update moved VALUEs.
# In ruby-2.7 they are invoked only while GC.compact or GC.verify_compaction_references.
# Ruby constants are usually moved, but local variables are not.
#
# Effectiveness of the tests below should be verified by commenting the compact callback out like so:
#
#   const rb_data_type_t rbffi_struct_layout_data_type = {
#     .wrap_struct_name = "FFI::StructLayout",
#     .function = {
#         .dmark = struct_layout_mark,
#         .dfree = struct_layout_free,
#         .dsize = struct_layout_memsize,
#        # ffi_compact_callback( struct_layout_compact )
#     },
#     .parent = &rbffi_type_data_type,
#     .flags = RUBY_TYPED_FREE_IMMEDIATELY | RUBY_TYPED_WB_PROTECTED
#   };
#
# This should result in a segmentation fault aborting the whole process.
# Therefore the effectiveness of only one test can be verified per rspec run.

require File.expand_path(File.join(File.dirname(__FILE__), "spec_helper"))

describe "GC.compact", if: GC.respond_to?(:compact) do
	before :all do

		class St1 < FFI::Struct
			layout  :i, :int
		end
		ST1 = St1.new

		class St2 < FFI::Struct
			layout  :i, :int
		end
		ST2 = St2.new
		ST2[:i] = 6789

		begin
			# Use GC.verify_compaction_references instead of GC.compact .
			# This has the advantage that all movable objects are actually moved.
			# The downside is that it doubles the heap space of the Ruby process.
			# Therefore we call it only once and do several tests afterwards.
			GC.verify_compaction_references(toward: :empty, double_heap: true)
		rescue NotImplementedError, NoMethodError => err
			skip("GC.compact skipped: #{err}")
		end
	end

	it "should compact FFI::StructLayout without field cache" do
		expect( ST1[:i] ).to eq( 0 )
	end

	it "should compact FFI::StructLayout with field cache" do
		expect( ST2[:i] ).to eq( 6789 )
	end

	it "should compact FFI::StructLayout::Field" do
		l = St1.layout
		expect( l.fields.first.type ).to eq( FFI::Type::Builtin::INT32 )
	end
end
