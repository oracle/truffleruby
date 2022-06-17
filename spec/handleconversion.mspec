class NativeHandleChecker
  def register
    MSpec.register :before, self
    MSpec.register :after, self
  end

  def unregister
    MSpec.unregister :before, self
    MSpec.unregister :after, self
  end

  def before(state)
    @start_count = Truffle::Debug.cexts_to_native_count
#    $stdout.puts "Before #{state.description}."
  end

  def after(state)
    (Truffle::Debug.cexts_to_native_count - @start_count).should == 0
#    $stdout.puts "After #{state.description}."
  end
end

begin
  checker = NativeHandleChecker.new
  checker.register
  load File.expand_path('../truffleruby.mspec', __FILE__)
end
