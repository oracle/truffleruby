require File.expand_path('../../spec_helper', __FILE__)

describe :resists_cve_2011_4815, shared: true do
  it "reists CVE-2011-4815 by having different hash codes in different processes" do
    expression = "print (#{@method}).hash"
    ruby_exe(expression).should_not == ruby_exe(expression)
  end
end

describe "Object#hash" do
  it_behaves_like :resists_cve_2011_4815, 'Object.new'
end

describe "Integer#hash" do
  it_behaves_like :resists_cve_2011_4815, '14'
end

describe "Float#hash" do
  it_behaves_like :resists_cve_2011_4815, '3.14'
end

describe "String#hash" do
  it_behaves_like :resists_cve_2011_4815, '"abc"'
end

describe "Symbol#hash" do
  it_behaves_like :resists_cve_2011_4815, ':a'
end

describe "Array#hash" do
  it_behaves_like :resists_cve_2011_4815, '[1, 2, 3]'
end

describe "Hash#hash" do
  it_behaves_like :resists_cve_2011_4815, '{a: 1, b: 2, c: 3}'
end
