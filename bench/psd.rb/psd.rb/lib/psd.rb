require "bindata"
require "psd/enginedata"
require "chunky_png"

require_relative 'psd/section'

dir_root = File.dirname(File.absolute_path(__FILE__)) + '/psd'
[
  '/image_exports/*',
  '/image_formats/*',
  '/image_modes/*',
  '/nodes/*',
  '/layer_info/*',
  '/layer/*',
  '/**/*'
].each do |path|
  Dir.glob(dir_root + path) { |file| require file if File.file?(file) }
end

# A general purpose parser for Photoshop files. PSDs are broken up in to 4 logical sections:
# the header, resources, the layer mask (including layers), and the preview image. We parse
# each of these sections in order.
class PSD
  include Logger
  include Helpers
  include NodeExporting

  attr_reader :file, :opts
  alias :options :opts

  # Opens the named file, parses it, and makes it available for reading. Then, closes it after you're finished.
  # @param filename [String]  the name of the file to open
  # @return [PSD] the {PSD} object if no block was given, otherwise the value of the block
  def self.open(filename, opts={}, &block)
    raise "Must supply a block. Otherwise, use PSD.new." unless block_given?

    psd = PSD.new(filename, opts)
    psd.parse!

    if 0 == block.arity
      psd.instance_eval(&block)
    else
      yield psd
    end
  ensure
    psd.close if psd
  end

  # Create and store a reference to our PSD file
  def initialize(file, opts={})
    @file = PSD::File.new(file, 'rb')
    @file.seek 0 # If the file was previously used and not closed

    @opts = opts
    @header = nil
    @resources = nil
    @layer_mask = nil
    @parsed = false
  end

  # Close the PSD file
  def close
    file.close unless file.closed?
  end

  # There is a specific order that must be followed when parsing
  # the PSD. Sections can be skipped if needed. This method will
  # parse all sections of the PSD.
  def parse!
    header
    resources
    layer_mask
    image
    
    @parsed = true

    return true
  end

  # Has our PSD been parsed yet?
  def parsed?
    @parsed
  end

  # Get the Header, parsing it if needed.
  def header
    return @header if @header

    @header = Header.read(@file)
    PSD.logger.debug @header.inspect
  end

  # Get the Resources section, parsing if needed.
  def resources
    return @resources unless @resources.nil?

    ensure_header

    @resources = Resources.new(@file)
    @resources.parse

    return @resources
  end

  # Get the LayerMask section. Ensures the header and resources
  # have been parsed first since they are required.
  def layer_mask
    ensure_header
    ensure_resources

    @layer_mask ||= LayerMask.new(@file, @header, @opts).parse
  end

  # Get the full size flattened preview Image.
  def image
    ensure_header
    ensure_resources
    ensure_layer_mask

    @image ||= (
      # The image is the last section in the file, so we don't have to
      # bother with skipping over the bytes to read more data.
      image = Image.new(@file, @header)
      LazyExecute.new(image, @file)
        .later(:parse)
        .ignore(:width, :height)
    )
  end

  # Export the current file to a new PSD. This may or may not work.
  def export(file)
    parse! unless parsed?

    # Create our file for writing
    outfile = File.open(file, 'w')

    # Reset the file pointer
    @file.seek 0
    @header.write outfile
    @file.seek @header.num_bytes, IO::SEEK_CUR

    # Nothing in the header or resources we want to bother with changing
    # right now. Write it straight to file.
    outfile.write @file.read(@resources.end_of_section - @file.tell)

    # Now, the changeable part. Layers and masks.
    layer_mask.export(outfile)

    # And the rest of the file (merged image data)
    outfile.write @file.read
    outfile.flush
  end

  private

  def ensure_header
    header # Header is always required
  end

  def ensure_resources
    return unless @resources.nil?
    
    @resources = Resources.new(@file)
    @resources.skip
  end

  def ensure_layer_mask
    return unless @layer_mask.nil?

    @layer_mask = LayerMask.new(@file, @header, @opts)
    @layer_mask.skip
  end
end