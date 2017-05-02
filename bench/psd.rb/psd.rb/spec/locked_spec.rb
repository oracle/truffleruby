require 'spec_helper'

describe 'Locked' do
  it "should parse locked layer info" do
    psd = PSD.new('spec/files/locked.psd')
    psd.parse!

    expect(psd.tree.children[0].position_locked?).to eq(true)
    expect(psd.tree.children[0].all_locked?).to eq(true)
    expect(psd.tree.children[0].composite_locked?).to eq(true)
    expect(psd.tree.children[0].transparency_locked?).to eq(true)
    
    expect(psd.tree.children[1].position_locked?).to eq(true)
    expect(psd.tree.children[1].all_locked?).to eq(false)
    expect(psd.tree.children[1].composite_locked?).to eq(false)
    expect(psd.tree.children[1].transparency_locked?).to eq(false)
    
    expect(psd.tree.children[2].position_locked?).to eq(false)
    expect(psd.tree.children[2].all_locked?).to eq(false)
    expect(psd.tree.children[2].composite_locked?).to eq(true)
    expect(psd.tree.children[2].transparency_locked?).to eq(false)

    expect(psd.tree.children[3].position_locked?).to eq(false)
    expect(psd.tree.children[3].all_locked?).to eq(false)
    expect(psd.tree.children[3].composite_locked?).to eq(false)
    expect(psd.tree.children[3].transparency_locked?).to eq(true)

    expect(psd.tree.children[4].position_locked?).to eq(true)
    expect(psd.tree.children[4].all_locked?).to eq(false)
    expect(psd.tree.children[4].composite_locked?).to eq(true)
    expect(psd.tree.children[4].transparency_locked?).to eq(false)

    expect(psd.tree.children[5].position_locked?).to eq(true)
    expect(psd.tree.children[5].all_locked?).to eq(false)
    expect(psd.tree.children[5].composite_locked?).to eq(false)
    expect(psd.tree.children[5].transparency_locked?).to eq(true)

    expect(psd.tree.children[6].position_locked?).to eq(false)
    expect(psd.tree.children[6].all_locked?).to eq(false)
    expect(psd.tree.children[6].composite_locked?).to eq(false)
    expect(psd.tree.children[6].transparency_locked?).to eq(false)

    expect(psd.tree.children[7].position_locked?).to eq(true)
    expect(psd.tree.children[7].all_locked?).to eq(true)
    expect(psd.tree.children[7].composite_locked?).to eq(true)
    expect(psd.tree.children[7].transparency_locked?).to eq(true)

    expect(psd.tree.children[7].children[0].position_locked?).to eq(false)
    expect(psd.tree.children[7].children[0].all_locked?).to eq(false)
    expect(psd.tree.children[7].children[0].composite_locked?).to eq(false)
    expect(psd.tree.children[7].children[0].transparency_locked?).to eq(false)

    expect(psd.tree.children[7].children[1].position_locked?).to eq(true)
    expect(psd.tree.children[7].children[1].all_locked?).to eq(true)
    expect(psd.tree.children[7].children[1].composite_locked?).to eq(true)
    expect(psd.tree.children[7].children[1].transparency_locked?).to eq(true)

    expect(psd.tree.children[7].children[1].children[0].position_locked?).to eq(true)
    expect(psd.tree.children[7].children[1].children[0].all_locked?).to eq(false)
    expect(psd.tree.children[7].children[1].children[0].composite_locked?).to eq(false)
    expect(psd.tree.children[7].children[1].children[0].transparency_locked?).to eq(false)

    expect(psd.tree.children[8].position_locked?).to eq(true)
    expect(psd.tree.children[8].all_locked?).to eq(false)
    expect(psd.tree.children[8].composite_locked?).to eq(false)
    expect(psd.tree.children[8].transparency_locked?).to eq(true)

    expect(psd.tree.children[9].position_locked?).to eq(true)
    expect(psd.tree.children[9].all_locked?).to eq(true)
    expect(psd.tree.children[9].composite_locked?).to eq(true)
    expect(psd.tree.children[9].transparency_locked?).to eq(true)

    expect(psd.tree.children[12].position_locked?).to eq(false)
    expect(psd.tree.children[12].all_locked?).to eq(false)
    expect(psd.tree.children[12].composite_locked?).to eq(false)
    expect(psd.tree.children[12].transparency_locked?).to eq(false)
  end
end