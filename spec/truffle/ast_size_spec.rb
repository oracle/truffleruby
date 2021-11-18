require_relative '../ruby/spec_helper'

describe 'Regexp' do
  it 'has critical methods of under 100 AST nodes' do
    cmd = <<-EOF
puts Truffle::Debug.ast_size(Regexp.instance_method(:match)) < 100
puts Truffle::Debug.ast_size(Regexp.instance_method(:match?)) < 100
puts Truffle::Debug.ast_size(Regexp.instance_method(:===)) < 100
puts Truffle::Debug.ast_size(Truffle::RegexpOperations.method(:match)) < 100
puts Truffle::Debug.ast_size(Truffle::RegexpOperations.method(:match?)) < 100
puts Truffle::Debug.ast_size(Truffle::RegexpOperations.method(:match_from)) < 100
puts Truffle::Debug.ast_size(Truffle::RegexpOperations.method(:search_region)) < 100
EOF
    ruby_exe(cmd).should == "true\n" * 7;
  end
end


describe 'String' do
  it 'has critical methods of under 100 AST nodes' do
    cmd = <<-EOF
require 'strscan'
puts Truffle::Debug.ast_size(String.instance_method(:sub)) < 100
puts Truffle::Debug.ast_size(String.instance_method(:sub!)) < 100
puts Truffle::Debug.ast_size(String.instance_method(:gsub)) < 100
puts Truffle::Debug.ast_size(String.instance_method(:gsub!)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_match_and_replace)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_internal_hash)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_internal_replacement)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_internal_core_check_encoding)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_internal_matches)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_new_offset)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_regexp_matches)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_string_matches)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_other_matches)) < 100
puts Truffle::Debug.ast_size(Truffle::StringOperations.method(:gsub_internal_yield_matches)) < 100
puts Truffle::Debug.ast_size(StringScanner.instance_method(:scan_internal)) < 100
EOF
    ruby_exe(cmd).should == "true\n" * 15;
  end
end
