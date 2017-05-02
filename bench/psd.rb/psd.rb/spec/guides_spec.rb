require 'spec_helper'

describe 'Guides' do
  describe "Handle file with slice properly" do
    it "should successfully parse the PSD file that has guides" do
      psd = PSD.new('spec/files/guides.psd')
      psd.parse!

      # File should parse
      expect(psd).to be_parsed

      # Guides should not be nil
      expect(psd.resources[:guides]).to_not be_nil

      # Each guide should have a position and direction
      psd.resources[:guides].data.to_a.each do |guide|
        expect(guide).to_not be_nil
        expect(guide[:location]).to_not be_nil
        expect(guide[:direction]).to_not be_nil
      end
    end
  end

  describe "Handle file without guides properly" do
    it "should successfully parse a PSD file which does not have guides" do
      psd = PSD.new('spec/files/simplest.psd')
      psd.parse!

      expect(psd).to be_parsed

      expect(psd.resources[:guides].data.to_a.size).to be 0
    end
  end
end