require 'spec_helper'

describe 'PSD' do
  let(:filename) { 'spec/files/example.psd' }

  it 'should open a file without a block' do
    psd = PSD.new(filename)
    expect(psd).to_not be_parsed
    expect(psd).to be_an_instance_of(PSD)
  end

  it 'should raise an exception if using open without a block' do
    expect {
      PSD.open(filename)  
    }.to raise_error
  end

  it 'should refuse to open a bad filename' do
    expect { PSD.open('') }.to raise_error
  end

  it 'should open a file and feed it to a block' do
    PSD.open(filename) do |psd|
      expect(psd).to be_parsed
      expect(psd).to be_an_instance_of(PSD)
    end
  end

  # We have to use #should syntax here because the DSL binds
  # the block to the PSD instance.
  it 'should open a file and feed it to a block DSL style' do
    PSD.open(filename) do
      parsed?.should == true
      is_a?(PSD).should == true
    end
  end
end