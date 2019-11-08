require_relative '../../spec_helper'

describe 'mkmf' do
  it 'can be run with frozen string literals' do
    ruby_exe('puts "Success"', options: '-rmkmf --enable-frozen-string-literal', escape: true).should == "Success\n"
  end
end
