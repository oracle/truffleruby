require 'spec_helper'

describe 'Image Exporting' do
  let!(:psd) { PSD.new('spec/files/pixel.psd') }

  describe "the full preview image" do
    it "should successfully parse the image data" do
      psd.parse!
      expect(psd).to be_parsed
      expect(psd.image).to_not be_nil
      expect(psd.image.width).to eq(1)
      expect(psd.image.height).to eq(1)
      expect(psd.image.pixel_data).to eq([ChunkyPNG::Color.rgba(0, 100, 200, 255)])
    end

    it "should be able to skip to the image" do
      expect(psd).to_not be_parsed
      expect(psd.image.width).to eq(1)
      expect(psd.image.height).to eq(1)
      expect(psd.image.pixel_data).to eq([ChunkyPNG::Color.rgba(0, 100, 200, 255)])
    end

    describe "as PNG" do
      it "should produce a valid PNG object" do
        expect(psd.image.to_png).to be_an_instance_of(ChunkyPNG::Canvas)

        expect(psd.image.to_png.width).to eq(1)
        expect(psd.image.to_png.height).to eq(1)
        expect(
          ChunkyPNG::Color.to_truecolor_alpha_bytes(psd.image.to_png[0,0])
        ).to eq([0, 100, 200, 255])
      end
    end
  end

  describe "Renderer" do
    before(:each) do
      psd.parse!
    end

    it "should be available via any tree node" do
      [psd.tree, psd.tree.children.first].each do |node|
        expect(node).to respond_to(:renderer)
        expect(node).to respond_to(:to_png)
        expect(node).to respond_to(:save_as_png)
      end
    end

    it "returns a Renderer object" do
      [psd.tree, psd.tree.children.first].each do |node|
        expect(node.renderer).to be_an_instance_of(PSD::Renderer)
      end
    end

    it "produces a correct PNG" do
      expect(psd.tree.to_png).to be_an_instance_of(ChunkyPNG::Canvas)
      expect(psd.tree.to_png.pixels).to eq([ChunkyPNG::Color.rgba(0, 100, 200, 255)])
      expect(psd.tree.to_png[0, 0]).to eq(ChunkyPNG::Color.rgba(0, 100, 200, 255))
    end

    describe "Canvas" do
      before do
        @node = psd.tree.children.first
        @canvas = PSD::Renderer::Canvas.new(@node)
      end

      it 'is initialized properly' do
        expect(@canvas.node).to be @node
        expect(@canvas.width).to eq @node.width
        expect(@canvas.height).to eq @node.height

        expect(@canvas.opacity).to eq @node.opacity
        expect(@canvas.fill_opacity).to eq @node.fill_opacity

        expect(@canvas.canvas).to be_an_instance_of(ChunkyPNG::Canvas)
        expect(@canvas.instance_variable_get(:@pixel_data)).to be_nil
        expect(@canvas.canvas.pixels.length).to be 1
      end

      it 'delegates array methods to internal canvas' do
        expect(@canvas[0, 0]).to eq ChunkyPNG::Color.rgba(0, 100, 200, 255)
        expect(@canvas).to respond_to(:[]=)
      end
    end
  end
end
