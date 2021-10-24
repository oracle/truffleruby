require_relative '../../spec_helper'

describe "Random::DEFAULT" do

  it "returns a random number generator" do
    suppress_warning do
      Random::DEFAULT.should respond_to(:rand)
    end
  end

  it "refers to the Random class" do
    suppress_warning do
      Random::DEFAULT.should.equal?(Random)
    end
  end

  it "is deprecated" do
    -> {
      Random::DEFAULT.should.equal?(Random)
    }.should complain(/constant Random::DEFAULT is deprecated/)
  end

  it "changes seed on reboot" do
    seed1 = ruby_exe('p Random::DEFAULT.new.seed', options: '--disable-gems')
    seed2 = ruby_exe('p Random::DEFAULT.new.seed', options: '--disable-gems')
    seed1.should != seed2
  end
end
