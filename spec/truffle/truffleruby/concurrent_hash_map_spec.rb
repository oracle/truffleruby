require_relative '../../ruby/spec_helper'

describe "TruffleRuby::ConcurrentHashMap" do
  before do
    @h = TruffleRuby::ConcurrentHashMap.new
  end

  it "#[] of a new instance is empty" do
    @h[:empty].should equal nil
  end

  it "#[]= creates a new key value pair" do
    new_value = "bar"
    @h[:foo] = new_value
    @h[:foo].should equal new_value
  end

  it "#compute_if_absent computes and stores new value for key if key is absent" do
    expected_value = "value for foobar"
    @h.compute_if_absent(:foobar) { expected_value }.should equal expected_value

    @h[:foobar].should equal expected_value
  end

  it "#compute_if_present computes and stores new value for key if key is present" do
    expected_value = "new value"
    @h[:foobar] = "old value"
    @h.compute_if_present(:foobar) { expected_value }.should equal expected_value

    @h[:foobar].should equal expected_value
  end

  it "#compute computes and stores new value" do
    expected_value = "new value"
    @h[:foobar] = "old value"
    @h.compute(:foobar) { expected_value }.should equal expected_value

    @h[:foobar].should equal expected_value
  end

  it "#merge_pair stores value if key is absent" do
    new_value = "bloop"
    @h.merge_pair(:foobar, new_value).should equal new_value
    @h[:foobar].should equal new_value
  end

  it "#merge_pair stores computed value if key is present" do
    old_value, new_value = "bleep", "bloop"
    expected_value = old_value + new_value
    @h[:foobar] = old_value

    @h.merge_pair(:foobar, new_value) do |value|
      value + new_value
    end.should == expected_value
    @h[:foobar].should == expected_value
  end

  it "#replace_pair replaces old value with new value if key exists and current value matches old value" do
    old_value, new_value = "bleep", "bloop"
    @h[:foobar] = old_value

    @h.replace_pair(:foobar, old_value, new_value).should be_true
    @h[:foobar].should equal new_value
  end

  it "#replace_pair doesn't replace old value if current value doesn't match old value" do
    expected_old_value = "BLOOP"
    @h[:foobar] = expected_old_value

    @h.replace_pair(:foobar, "bleep", "bloop").should be_false
    @h[:foobar].should equal expected_old_value
  end

  it "#replace_if_exists replaces value if key exists" do
    @h[:foobar] = "bloop"
    expected_value = "bleep"

    @h.replace_if_exists(:foobar, expected_value).should == "bloop"
    @h[:foobar].should equal expected_value
  end

  it "#get_and_set gets current value and set new value" do
    @h[:foobar] = "bloop"
    expected_value = "bleep"

    @h.get_and_set(:foobar, expected_value).should == "bloop"
    @h[:foobar].should equal expected_value
  end

  it "#key? returns true if key is present" do
    @h[:foobar] = "bloop"
    @h.key?(:foobar).should be_true
  end

  it "#key? returns false if key is absent" do
    @h.key?(:foobar).should be_false
  end

  it "#delete deletes key and value pair" do
    value = "bloop"
    @h[:foobar] = value
    @h.delete(:foobar).should equal value
    @h[:foobar].should be_nil
  end

  it "#delete_pair deletes pair if value equals provided value" do
    value = "bloop"
    @h[:foobar] = value
    @h.delete_pair(:foobar, value).should be_true
    @h[:foobar].should be_nil
  end

  it "#delete_pair doesn't delete pair if value equals provided value" do
    value = "bloop"
    @h[:foobar] = value
    @h.delete_pair(:foobar, "BLOOP").should be_false
    @h[:foobar].should equal value
  end

  it "#clear returns an empty hash" do
    @h[:foobar] = "bleep"
    @h.clear
    @h.key?(:foobar).should be_false
    @h.size.should == 0
  end

  it "#size returns the size of hash" do
    @h[:foobar], @h[:barfoo] = "bleep", "bloop"
    @h.size.should == 2
  end

  it "#get_or_default returns value of key if key mapped" do
    @h[:foobar] = "bleep"
    @h.get_or_default(:foobar, "BLEEP").should == "bleep"
    @h.key?(:foobar).should be_true
  end

  it "#get_or_default returns default if key isn't mapped" do
    @h.get_or_default(:foobar, "BLEEP").should == "BLEEP"
    @h.key?(:foobar).should be_false
  end

  it "#each_pair passes each key value pair to given block" do
    @h[:foobar], @h[:barfoo] = "bleep", "bloop"
    @h.each_pair do |key, value|
      value.should == @h[key]
    end
  end

  it "#each_pair returns self" do
    @h.each_pair { }.should equal(@h)
  end
end
