# -*- encoding: us-ascii -*-

require_relative '../../../spec_helper'

describe "Enumerator::Lazy" do
  it "is a subclass of Enumerator" do
    Enumerator::Lazy.superclass.should equal(Enumerator)
  end

  it "defines lazy versions of a whitelist of Enumerator methods" do
    Enumerator::Lazy.instance_methods(false).should include(
      :grep, :grep_v, :find_all, :select, :reject, :collect,
      :map, :flat_map, :collect_concat, :zip, :take, :take_while,
      :drop, :drop_while, :chunk, :slice_before, :slice_after,
      :slice_when, :chunk_while, :uniq, :lazy, :force, :to_enum,
      :enum_for)
  end
end

describe "Enumerator::Lazy#lazy" do
  it "returns self" do
    lazy = (1..3).to_enum.lazy
    lazy.lazy.should equal(lazy)
  end
end
