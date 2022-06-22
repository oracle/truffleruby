
class NativeHandleChecker
  EXPECTED_FAILURES = {
    "C-API Debug function rb_debug_inspector_open creates a debug context and calls the given callback" => 2,
    "C-API Debug function rb_debug_inspector_frame_self_get returns self" => 2,
    "C-API Debug function rb_debug_inspector_frame_class_get returns the frame class" => 2,
    "C-API Debug function rb_debug_inspector_frame_binding_get returns the current binding" => 2,
    "C-API Debug function rb_debug_inspector_frame_binding_get matches the locations in rb_debug_inspector_backtrace_locations" => 2,
    "C-API Debug function rb_debug_inspector_frame_iseq_get returns an InstructionSequence" => 2,
    "C-API Debug function rb_debug_inspector_backtrace_locations returns an array of Thread::Backtrace::Location" => 2,
    "C-API IO function rb_io_printf calls #to_str to convert the format object to a String" => 2,
    "C-API IO function rb_io_printf calls #to_s to convert the object to a String" => 2,
    "C-API IO function rb_io_printf writes the Strings to the IO" => 4,
    "C-API IO function rb_io_print calls #to_s to convert the object to a String" => 1,
    "C-API IO function rb_io_print writes the Strings to the IO with no separator" => 3,
    "C-API IO function rb_io_puts calls #to_s to convert the object to a String" => 1,
    "C-API IO function rb_io_puts writes the Strings to the IO separated by newlines" => 3,
    "C-API Kernel function rb_rescue executes the passed 'rescue function' if a StandardError exception is raised" => 3,
    "C-API Kernel function rb_rescue passes the user supplied argument to the 'rescue function' if a StandardError exception is raised" => 4,
    "C-API Kernel function rb_rescue passes the raised exception to the 'rescue function' if a StandardError exception is raised" => 2,
    "C-API Kernel function rb_rescue raises an exception if any exception is raised inside the 'rescue function'" => 1,
    "C-API Kernel function rb_rescue makes $! available only during the 'rescue function' execution" => 1,
    "CApiObject rb_obj_call_init sends #initialize" => 2,
    "C-API String function rb_sprintf formats a string VALUE using to_s if sign not specified in format" => 1,
    "C-API String function rb_sprintf formats a string VALUE using inspect if sign specified in format" => 1,
    "C-API String function rb_sprintf formats a TrueClass VALUE as `TrueClass` if sign not specified in format" => 1,
    "C-API String function rb_sprintf truncates a VALUE string to a supplied precision if that is shorter than the VALUE string" => 1,
    "C-API String function rb_sprintf does not truncates a VALUE string to a supplied precision if that is longer than the VALUE string" => 1,
    "C-API String function rb_sprintf pads a VALUE string to a supplied width if that is longer than the VALUE string" => 1,
    "C-API String function rb_sprintf can format a string VALUE as a pointer and gives the same output as sprintf in C" => 1,
    "C-API String function rb_string_value_cstr returns a non-null pointer for a simple string" => 1,
    "C-API String function rb_string_value_cstr returns a non-null pointer for a UTF-16 string" => 1,
    "C-API String function rb_string_value_cstr raises an error if a string contains a null" => 1,
    "C-API String function rb_string_value_cstr raises an error if a UTF-16 string contains a null" => 1,
    "CApiWrappedTypedStruct throws an exception for a wrong type" => 1,
    "C-API Util function rb_scan_args assigns Hash arguments" => 1,
    "C-API Util function rb_scan_args assigns required and Hash arguments" => 1,
    "C-API Util function rb_scan_args assigns required, optional, splat, post-splat, Hash and block arguments" => 1,
    "C-API Util function rb_get_kwargs extracts required arguments in the order requested" => 2,
    "C-API Util function rb_get_kwargs extracts required and optional arguments in the order requested" => 1,
    "C-API Util function rb_get_kwargs raises an error if a required argument is not in the hash" => 1,
    "Native handle conversion converts all elements to native handles when memcpying an RARRAY_PTR" => 1000,
  }

  def register
    MSpec.register :before, self
    MSpec.register :passed, self
  end

  def unregister
    MSpec.unregister :before, self
    MSpec.unregister :passed, self
  end

  def before(state)
    @start_count = Truffle::Debug.cexts_to_native_count
  end

  def passed(state, example)
    (Truffle::Debug.cexts_to_native_count - @start_count).should == (EXPECTED_FAILURES[state.description] || 0)
  end
end

begin
  checker = NativeHandleChecker.new
  checker.register
  load File.expand_path('../truffleruby.mspec', __FILE__)
end
