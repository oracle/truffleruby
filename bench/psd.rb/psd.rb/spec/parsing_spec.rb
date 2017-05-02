require 'spec_helper'

describe 'Parsing' do
  before(:each) do
    @psd = PSD.new('spec/files/example.psd')
  end

  it "should parse without error" do
    @psd.parse!
    expect(@psd).to be_parsed
  end

  describe 'Header' do
    before(:each) do
      @psd.parse!
    end

    it "should contain data" do
      expect(@psd.header).not_to be_nil
    end

    it "should be the proper version" do
      expect(@psd.header.version).to eq(1)
    end

    it "should have the proper number of channels" do
      expect(@psd.header.channels).to eq(3)
    end

    it "should parse the proper document dimensions" do
      expect(@psd.header.width).to eq(900)
      expect(@psd.header.height).to eq(600)
    end

    it "should correctly parse the color mode" do
      expect(@psd.header.mode).to eq(3)
      expect(@psd.header.mode_name).to eq('RGBColor')
    end
  end

  describe 'Resources' do
    before(:each) do
      @psd.parse!
    end

    it "should contain data" do
      expect(@psd.resources).not_to be_nil
      expect(@psd.resources.data).to be_an_instance_of(Hash)
      expect(@psd.resources.data.size).to be >= 1
    end

    it "should be of type 8BIM" do
      @psd.resources.data.each { |id, r| expect(r.type).to eq('8BIM') }
    end

    it "should have an ID" do
      @psd.resources.data.each do |id, r|
        expect(r.id).to_not be_nil
      end
    end
  end

  describe 'Layer Mask' do
    before(:each) do
      @psd.parse!
    end

    it "should contain data" do
      expect(@psd.layer_mask).to_not be_nil
      expect(@psd.layer_mask).to be_an_instance_of(PSD::LayerMask)
    end

    it "should contain layers" do
      expect(@psd.layer_mask.layers.size).to be > 0
    end

    # The test file is actually missing the global mask. Need a new test file
    # with it present.
    # it "should contain the global layer mask data" do
    #   expect(@psd.layer_mask.global_mask).to_not be_nil
    #   expect(@psd.layer_mask.global_mask).to include :overlay_color_space
    #   expect(@psd.layer_mask.global_mask).to include :color_components
    #   expect(@psd.layer_mask.global_mask).to include opacity: 1.0
    # end
  end

  describe 'Layers' do
    before(:each) do
      @psd.parse!
    end

    it "should contain each layer" do
      expect(@psd.layer_mask.layers.size).to eq(15)
      expect(@psd.layers).to be @psd.layer_mask.layers
      @psd.layers.each { |l| expect(l).to be_an_instance_of(PSD::Layer) }
    end

    it "should have a name" do
      expect(@psd.layers.first.name).to eq('Version C')
    end

    it "should properly identify folders" do
      expect(@psd.layers.first).to be_folder
      expect(@psd.layers.select { |l| l.name == 'Matte' }.first).not_to be_folder
    end

    it "should properly detect visibility" do
      expect(@psd.layers.first).not_to be_visible
      expect(
        @psd
          .layers
          .select { |l| l.name == 'Version A' }.first
      ).to be_visible
    end

    it "should properly calculate dimensions" do
      layer = @psd.layers.select { |l| l.name == 'Logo_Glyph' }.last
      expect(layer.width).to eq(142)
      expect(layer.height).to eq(179)
    end

    it "should properly calculate coordinates" do
      layer = @psd.layers.select { |l| l.name == 'Logo_Glyph' }.last
      expect(layer.left).to eq(379)
      expect(layer.top).to eq(210)
    end

    it "should have a blend mode" do
      blend_mode = @psd.layers.select { |l| l.name == 'Version A' }.last.blend_mode
      expect(blend_mode).to_not be_nil
      expect(blend_mode.mode).to eq('normal')
      expect(blend_mode.opacity).to eq(255)
      expect(blend_mode.opacity_percentage).to eq(100)
      expect(blend_mode.visible).to be true
    end

    it "should parse all layer comps" do
      expect(@psd.layer_comps.size).to eq(3)
      expect(@psd.layer_comps.map { |c| c[:name] }).to eq([
        'Version A',
        'Version B',
        'Version C'
      ])

      @psd.layer_comps.each do |c|
        expect(c[:id]).to be > 0
      end
    end
  end
  
  describe 'Blend Modes' do
    before(:each) do
      @psd = PSD.new('spec/files/blendmodes.psd')
      @psd.parse!
    end

    it "should parse all blend modes" do
      @psd.layers.each do |layer|
        expect(layer.blend_mode.mode).to eq layer.name
      end      
    end
  end  
end