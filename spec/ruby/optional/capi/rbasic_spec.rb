require_relative 'spec_helper'
load_extension("rbasic")
load_extension("data")
load_extension("array")
require_relative 'shared/rbasic'

describe "RBasic support for regular objects" do
  specs = CApiRBasicSpecs.new
  def specs.data; [Object.new, Object.new] end
  it_behaves_like :rbasic, nil, specs
end

describe "RBasic support for RData" do
  specs = CApiRBasicRDataSpecs.new
  def specs.data
      data = CApiWrappedStructSpecs.new
      [data.wrap_struct(1024), data.wrap_struct(1025)]
  end
  it_behaves_like :rbasic, nil, specs
end