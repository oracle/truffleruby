require_relative '../../ruby/spec_helper'

describe "The -Xfinalizers.run_on_exit=true option" do
  it "runs finalizers on exit" do
    code = <<~RUBY
    OBJ = Object.new
    ObjectSpace.define_finalizer(OBJ, -> {
      puts :finalized
      STDOUT.flush
    })
    p :end
    RUBY
    ruby_exe(code, options: "-Xfinalizers.run_on_exit=true").should == ":end\nfinalized\n"
  end
end
