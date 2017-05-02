require 'spec_helper'

describe 'Util' do
  it "pad2's correctly" do
    expect(PSD::Util.pad2(0)).to eq 0
    expect(PSD::Util.pad2(1)).to eq 2
  end
  
  # Note, this pad4 is weird but this is expected results
  it "pad4's correctly" do
    expect(PSD::Util.pad4(0)).to eq 3
    expect(PSD::Util.pad4(1)).to eq 3
    expect(PSD::Util.pad4(2)).to eq 3
  end
end