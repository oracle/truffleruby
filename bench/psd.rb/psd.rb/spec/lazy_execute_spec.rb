require 'spec_helper'

describe PSD::LazyExecute do
  before(:each) do
    @psd = PSD.new('spec/files/pixel.psd')
  end

  it 'initializes correctly' do
    expect(@psd.image).to be_an_instance_of(PSD::LazyExecute)
    expect(@psd.image).to_not be_loaded
    expect(@psd.image.instance_variable_get(:@obj)).to be_an_instance_of(PSD::Image)
    expect(@psd.image.instance_variable_get(:@start_pos)).to eq @psd.file.tell
    expect(@psd.image.instance_variable_get(:@load_method)).to eq :parse
  end

  it 'loads when accessed the first time' do
    @psd.image.to_png
    expect(@psd.image).to be_loaded
  end
end