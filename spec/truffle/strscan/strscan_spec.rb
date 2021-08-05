require_relative '../../ruby/spec_helper'

require 'strscan'

describe 'StringScanner' do
  it 'has a `scan_internal` method of under 100 AST nodes' do
    ruby_exe('require "strscan"; print Truffle::Debug.ast_size(StringScanner.instance_method(:scan_internal))').to_i.should < 100
  end
end
