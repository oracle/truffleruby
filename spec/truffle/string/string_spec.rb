require_relative '../../ruby/spec_helper'

describe 'String' do
  it 'has a `sub` method of under 100 AST nodes' do
    ruby_exe('print Truffle::Debug.ast_size(String.instance_method(:sub))').to_i.should < 100
  end

  it 'has a `sub!` method of under 100 AST nodes' do
    ruby_exe('print Truffle::Debug.ast_size(String.instance_method(:sub!))').to_i.should < 100
  end

  it 'has a `gsub` method of under 100 AST nodes' do
    ruby_exe('print Truffle::Debug.ast_size(String.instance_method(:gsub))').to_i.should < 100
  end

  it 'has a `gsub!` method of under 100 AST nodes' do
    ruby_exe('print Truffle::Debug.ast_size(String.instance_method(:gsub!))').to_i.should < 100
  end
end
