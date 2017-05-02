require 'spec_helper'

describe 'Slices' do
  describe "Handle file with slice properly" do
    it "should successfully parse a the PSD file of version 7 or above and has slices" do
      psd = PSD.new('spec/files/slices.psd')
      psd.parse!

      # File should parse
      expect(psd).to be_parsed

      # Slices should not be nil
      expect(psd.resources[:slices]).to_not be_nil

      # slices version should be above 6
      expect(psd.resources[:slices].data.version).to be > 6

      # Bounds of each slice should not be nil
      psd.resources[:slices].data.to_a.each do |slice|
        expect(slice).to_not be_nil
        expect(slice[:bounds]).to_not be_nil
      end
    end

    it "should successfully parse a the PSD file of version 6 and has slices" do
      psd = PSD.new('spec/files/pixel.psd')
      psd.parse!

      # File should parse
      expect(psd).to be_parsed

      # Slices should still be not nil
      expect(psd.resources[:slices]).to_not be_nil

      # But slices version should be equal to 6
      expect(psd.resources[:slices].data.version).to eq 6

      # Bounds should not be nil
      psd.resources[:slices].data.to_a.each do |slice|
        expect(slice).to_not be_nil
        expect(slice[:bounds]).to_not be_nil
      end
    end
  end

  describe "Handle file without slices properly" do
    it "should successfully parse a PSD file which does not have slices" do
      psd = PSD.new('spec/files/simplest.psd')
      psd.parse!

      expect(psd).to be_parsed

      expect(psd.resources[:slices].data.to_a.size).to be 1
      expect(psd.resources[:slices].data.to_a[0][:id]).to be 0
    end
  end
end