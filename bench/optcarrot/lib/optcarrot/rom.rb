module Optcarrot
  # Cartridge class (with NROM mapper implemented)
  class ROM
    MAPPER_DB = { 0x00 => self }

    # These are optional
    require_relative "mapper/mmc1"
    require_relative "mapper/uxrom"
    require_relative "mapper/cnrom"
    require_relative "mapper/mmc3"

    def self.zip_extract(filename)
      require "zlib"
      bin = File.binread(filename)
      loop do
        sig, _, flags, comp, _, _, _, data_len, _, fn_len, ext_len = bin.slice!(0, 30).unpack("a4v5V3v2")
        break if sig != "PK\3\4".b
        fn = bin.slice!(0, fn_len)
        bin.slice!(0, ext_len)
        data = bin.slice!(0, data_len)
        next if File.extname(fn).downcase != ".nes"
        next if flags & 0x11 != 0
        next if comp != 0 && comp != 8
        if comp == 8
          zs = Zlib::Inflate.new(-15)
          data = zs.inflate(data)
          zs.finish
          zs.close
        end
        return data
      end
      raise "failed to extract ROM file from `#{ filename }'"
    end

    def self.load(conf, cpu, ppu)
      filename = conf.romfile
      basename = File.basename(filename)

      blob = (File.extname(filename) == ".zip" ? zip_extract(filename) : File.binread(filename)).bytes

      # parse mapper
      mapper = (blob[6] >> 4) | (blob[7] & 0xf0)

      klass = MAPPER_DB[mapper]
      raise NotImplementedError, "Unsupported mapper type 0x%02x" % mapper unless klass
      klass.new(conf, cpu, ppu, basename, blob)
    end

    class InvalidROM < StandardError
    end

    def parse_header(buf)
      raise InvalidROM, "Missing 16-byte header" if buf.size < 16
      raise InvalidROM, "Missing 'NES' constant in header" if buf[0, 4] != "NES\x1a".bytes
      raise NotImplementedError, "trainer not supported" if buf[6][2] == 1
      raise NotImplementedError, "VS cart not supported" if buf[7][0] == 1
      raise NotImplementedError, "PAL not supported" unless buf[9][0] == 0

      prg_banks = buf[4]
      chr_banks = buf[5]
      @mirroring = buf[6][0] == 0 ? :horizontal : :vertical
      @mirroring = :four_screen if buf[6][3] == 1
      @battery = buf[6][1] == 1
      @mapper = (buf[6] >> 4) | (buf[7] & 0xf0)
      ram_banks = [1, buf[8]].max

      return prg_banks, chr_banks, ram_banks
    end

    def initialize(conf, cpu, ppu, basename, buf)
      @conf = conf
      @cpu = cpu
      @ppu = ppu
      @basename = basename

      prg_count, chr_count, wrk_count = parse_header(buf.slice!(0, 16))

      raise InvalidROM, "EOF in ROM bank data" if buf.size < 0x4000 * prg_count
      @prg_banks = (0...prg_count).map { buf.slice!(0, 0x4000) }

      raise InvalidROM, "EOF in CHR bank data" if buf.size < 0x2000 * chr_count
      @chr_banks = (0...chr_count).map { buf.slice!(0, 0x2000) }

      @prg_ref = [nil] * 0x10000
      @prg_ref[0x8000, 0x4000] = @prg_banks.first
      @prg_ref[0xc000, 0x4000] = @prg_banks.last

      @chr_ram = chr_count == 0 # No CHR bank implies CHR-RAM (writable CHR bank)
      @chr_ref = @chr_ram ? [0] * 0x2000 : @chr_banks[0].dup

      @wrk_readable = wrk_count > 0
      @wrk_writable = false
      @wrk = wrk_count > 0 ? (0x6000..0x7fff).map {|addr| addr >> 8 } : nil

      init

      @ppu.nametables = @mirroring
      @ppu.set_chr_mem(@chr_ref, @chr_ram)
    end

    def init
    end

    def reset
      @cpu.add_mappings(0x8000..0xffff, @prg_ref, nil)
    end

    def inspect
      [
        "Mapper: #{ @mapper } (#{ self.class.to_s.split("::").last })",
        "PRG Banks: #{ @prg_banks.size }",
        "CHR Banks: #{ @chr_banks.size }",
        "Mirroring: #{ @mirroring }",
      ].join("\n")
    end

    def peek_6000(addr)
      @wrk_readable ? @wrk[addr - 0x6000] : (addr >> 8)
    end

    def poke_6000(addr, data)
      @wrk[addr - 0x6000] = data if @wrk_writable
    end

    def vsync
    end

    def load_battery
      return unless @battery
      sav = @basename + ".sav"
      return unless File.readable?(sav)
      sav = File.binread(sav)
      @wrk.replace(sav.bytes)
    end

    def save_battery
      return unless @battery
      sav = @basename + ".sav"
      puts "Saving: " + sav
      File.binwrite(sav, @wrk.pack("C*"))
    end
  end
end
