module Optcarrot
  # config manager and logger
  class Config
    OPTIONS = {
      optimization: {
        opt_ppu: {
          type: :opts,
          desc: "select PPU optimizations",
          candidates: PPU::OptimizedCodeBuilder::OPTIONS,
          default: nil,
        },
        opt_cpu: {
          type: :opts,
          desc: "select CPU optimizations",
          candidates: CPU::OptimizedCodeBuilder::OPTIONS,
          default: nil,
        },
        opt: { shortcut: %w(--opt-ppu=all --opt-cpu=all) },
        list_opts: { type: :info, desc: "list available optimizations" },
        dump_ppu: { type: :info, desc: "print generated PPU source code" },
        dump_cpu: { type: :info, desc: "print generated CPU source code" },
        load_ppu: { type: "FILE", desc: "use generated PPU source code" },
        load_cpu: { type: "FILE", desc: "use generated CPU source code" },
      },
      emulation: {
        sprite_limit:      { type: :switch, desc: "enable/disable sprite limit", default: false },
        frames:            { type: :int, desc: "execute N frames (0 = no limit)", default: 0, aliases: [:f, :frame] },
        audio_sample_rate: { type: :int, desc: "set audio sample rate", default: 44100 },
        audio_bit_depth:   { type: :int, desc: "set audio bit depth", default: 16 },
        nestopia_palette:  { type: :switch, desc: "use Nestopia palette instead of de facto", default: false },
      },
      driver: {
        video:  { type: :driver, desc: "select video driver", candidates: Driver::DRIVER_DB[:video].keys },
        audio:  { type: :driver, desc: "select audio driver", candidates: Driver::DRIVER_DB[:audio].keys },
        input:  { type: :driver, desc: "select input driver", candidates: Driver::DRIVER_DB[:input].keys },
        list_drivers: { type: :info, desc: "print available drivers" },
        sdl2:      { shortcut: %w(--video=sdl2 --audio=sdl2 --input=sdl2) },
        sfml:      { shortcut: %w(--video=sfml --audio=sfml --input=sfml) },
        headless:  { shortcut: %w(--video=none --audio=none --input=none) },
        video_output: { type: "FILE", desc: "save video to file", default: "video.EXT" },
        audio_output: { type: "FILE", desc: "save audio to file", default: "audio.wav" },
        show_fps: { type: :switch, desc: "show fps in the right-bottom corner", default: true },
        key_log: { type: "FILE", desc: "use recorded input file" },
        # key_config: { type: "KEY", desc: "key configuration" },
      },
      profiling: {
        print_fps: { type: :switch, desc: "print fps of last 10 frames", default: false },
        print_video_checksum: { type: :switch, desc: "print checksum of the last video output", default: false },
        stackprof: { shortcut: "--stackprof-mode=cpu", aliases: :p },
        stackprof_mode: { type: "MODE", desc: "run under stackprof", default: nil },
        stackprof_output: { type: "FILE", desc: "stackprof output file", default: "stackprof-MODE.dump" }
      },
      misc: {
        benchmark: { shortcut: %w(--headless --print-fps --print-video-checksum --frames 180), aliases: :b },
        loglevel: { type: :int, desc: "set loglevel", default: 1 },
        quiet:    { shortcut: "--loglevel=0", aliases: :q },
        verbose:  { shortcut: "--loglevel=2", aliases: :v },
        debug:    { shortcut: "--loglevel=3", aliases: :d },
        version: { type: :info, desc: "print version" },
        help:    { type: :info, desc: "print this message", aliases: :h },
      },
    }

    DEFAULT_OPTIONS = {}
    OPTIONS.each do |_kind, opts|
      opts.each do |id, opt|
        next if opt[:shortcut]
        DEFAULT_OPTIONS[id] = opt[:default] if opt.key?(:default)
        attr_reader id
      end
    end
    attr_reader :romfile

    def initialize(opt)
      opt = Parser.new(opt).options if opt.is_a?(Array)
      DEFAULT_OPTIONS.merge(opt).each {|id, val| instance_variable_set(:"@#{ id }", val) }
    end

    def debug(msg)
      puts "[DEBUG] " + msg if @loglevel >= 3
    end

    def info(msg)
      puts "[INFO] " + msg if @loglevel >= 2
    end

    def warn(msg)
      puts "[WARN] " + msg if @loglevel >= 1
    end

    def error(msg)
      puts "[ERROR] " + msg
    end

    def fatal(msg)
      puts "[FATAL] " + msg
      abort
    end

    # command-line option parser
    class Parser
      def initialize(argv)
        @argv = argv
        @options = DEFAULT_OPTIONS.dup
        parse_option until @argv.empty?
        error "ROM file is not given" unless @options[:romfile]
      rescue Invalid => err
        puts "[FATAL] #{ err }"
        exit 1
      end

      attr_reader :options

      class Invalid < RuntimeError; end

      def error(msg)
        raise Invalid, msg
      end

      def find_option(arg)
        OPTIONS.each do |_kind, opts|
          opts.each do |id_base, opt|
            [id_base, *opt[:aliases]].each do |id|
              id = id.to_s.tr("_", "-")
              return opt, id_base if id.size == 1 && arg == "-#{ id }"
              return opt, id_base if arg == "--#{ id }"
              return opt, id_base, true if opt[:type] == :switch && arg == "--no-#{ id }"
            end
          end
        end
        return nil
      end

      def parse_option
        arg, operand = @argv.shift.split("=", 2)
        if arg =~ /\A-(\w{2,})\z/
          args = $1.chars.map {|a| "-#{ a }" }
          args.last << "=" << operand if operand
          @argv.unshift(*args)
          return
        end
        opt, id, no = find_option(arg)
        if opt
          if opt[:shortcut]
            @argv.unshift(*opt[:shortcut])
            return
          elsif opt[:type] == :info
            send(id)
            exit
          elsif opt[:type] == :switch
            error "option `#{ arg }' doesn't allow an operand" if operand
            @options[id] = !no
          else
            @options[id] = parse_operand(operand, arg, opt)
          end
        else
          arg = @argv.shift if arg == "--"
          error "invalid option: `#{ arg }'" if arg && arg.start_with?("-")
          if arg
            error "extra argument: `#{ arg }'" if @options[:romfile]
            @options[:romfile] = arg
          end
        end
      end

      def parse_operand(operand, arg, opt)
        type = opt[:type]
        operand ||= @argv.shift
        case type
        when :opts
          operand = operand.split(",").map {|s| s.to_sym }
        when :driver
          operand = operand.to_sym
          error "unknown driver: `#{ operand }'" unless opt[:candidates].include?(operand)
        when :int
          begin
            operand = Integer(operand)
          rescue
            error "option `#{ arg }' requires numerical operand"
          end
        end
        operand
      end

      def help
        tbl = ["Usage: #{ $PROGRAM_NAME } [OPTION]... FILE"]
        long_name_width = 0
        OPTIONS.each do |kind, opts|
          tbl << "" << "#{ kind } options:"
          opts.each do |id_base, opt|
            short_name = [*opt[:aliases]][0]
            switch = args = ""
            case opt[:type]
            when :switch then switch = "[no-]"
            when :opts   then args = "=OPTS,..."
            when :driver then args = "=DRIVER"
            when :int    then args = "=N"
            when String  then args = "=" + opt[:type]
            end
            short_name = "-#{ switch }#{ short_name }, " if short_name && short_name.size == 1
            long_name = "--" + switch + id_base.to_s.tr("_", "-") + args
            if opt[:shortcut]
              desc = "same as `#{ [*opt[:shortcut]].join(" ") }'"
            else
              desc = opt[:desc]
              desc += " (default: #{ opt[:default] || "none" })" if opt.key?(:default)
            end
            long_name_width = [long_name_width, long_name.size].max
            tbl << [short_name, long_name, desc]
          end
        end
        tbl.each do |arg|
          if arg.is_a?(String)
            puts arg
          else
            short_name, long_name, desc = arg
            puts "    %4s%-*s %s" % [short_name, long_name_width, long_name, desc]
          end
        end
      end

      def version
        puts "optcarrot #{ VERSION }"
      end

      def list_drivers
        Driver::DRIVER_DB.each do |kind, drivers|
          puts "#{ kind } drivers: #{ drivers.keys * " " }"
        end
      end

      def list_opts
        puts "CPU core optimizations:"
        CPU::OptimizedCodeBuilder::OPTIONS.each do |opt|
          puts "  * #{ opt }"
        end
        puts
        puts "PPU core optimizations:"
        PPU::OptimizedCodeBuilder::OPTIONS.each do |opt|
          puts "  * #{ opt }"
        end
        puts
        puts "(See `doc/internal.md' in detail.)"
      end

      def dump_ppu
        puts PPU::OptimizedCodeBuilder.new(@options[:loglevel], @options[:opt_ppu] || []).build
      end

      def dump_cpu
        puts CPU::OptimizedCodeBuilder.new(@options[:loglevel], @options[:opt_cpu] || []).build
      end
    end
  end
end
