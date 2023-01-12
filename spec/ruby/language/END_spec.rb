require_relative '../spec_helper'

describe "The END keyword" do
  it "runs only once for multiple calls" do
    ruby_exe("10.times { END { puts 'foo' }; } ").should == "foo\n"
  end

  it "runs last in a given code unit" do
    ruby_exe("END { puts 'bar' }; puts'foo'; ").should == "foo\nbar\n"
  end

  it "runs multiple ends in LIFO order" do
    ruby_exe("END { puts 'foo' }; END { puts 'bar' }").should == "bar\nfoo\n"
  end

  context "END blocks and at_exit callbacks are mixed" do
    it "runs them all in LIFO order" do
      ruby_exe(<<~RUBY).should == "at_exit#2\nEND#2\nat_exit#1\nEND#1\n"
        END { puts 'END#1' }
        at_exit { puts 'at_exit#1' }
        END { puts 'END#2' }
        at_exit { puts 'at_exit#2' }
      RUBY
    end
  end
end
