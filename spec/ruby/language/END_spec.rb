require_relative '../spec_helper'
require_relative '../shared/kernel/at_exit'

describe "The END keyword" do
  it_behaves_like :kernel_at_exit, :END

  it "runs only once for multiple calls" do
    ruby_exe("10.times { END { puts 'foo' }; } ").should == "foo\n"
  end

  context "END blocks and at_exit callbacks are mixed" do
    it "runs them all in reverse order of registration" do
      ruby_exe(<<~RUBY).should == "at_exit#2\nEND#2\nat_exit#1\nEND#1\n"
        END { puts 'END#1' }
        at_exit { puts 'at_exit#1' }
        END { puts 'END#2' }
        at_exit { puts 'at_exit#2' }
      RUBY
    end
  end
end
