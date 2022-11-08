# frozen_string_literal: true

require 'objspace'
require 'pp'

require_relative 'color'

module DEBUGGER__
  module SkipPathHelper
    def skip_path?(path)
      (skip_paths = CONFIG[:skip_path]) && skip_paths.any?{|skip_path| path.match?(skip_path)}
    end

    def skip_location?(loc)
      loc_path = loc.absolute_path || "!eval:#{loc.path}"
      skip_path?(loc_path)
    end
  end

  class ThreadClient
    def self.current
      if thc = Thread.current[:DEBUGGER__ThreadClient]
        thc
      else
        thc = SESSION.get_thread_client
        Thread.current[:DEBUGGER__ThreadClient] = thc
      end
    end

    include Color
    include SkipPathHelper

    attr_reader :location, :thread, :id, :recorder

    def assemble_arguments(args)
      args.map do |arg|
        "#{colorize_cyan(arg[:name])}=#{arg[:value]}"
      end.join(", ")
    end

    def default_frame_formatter frame
      call_identifier_str =
        case frame.frame_type
        when :block
          level, block_loc, args = frame.block_identifier

          if !args.empty?
            args_str = " {|#{assemble_arguments(args)}|}"
          end

          "#{colorize_blue("block")}#{args_str} in #{colorize_blue(block_loc + level)}"
        when :method
          ci, args = frame.method_identifier

          if !args.empty?
            args_str = "(#{assemble_arguments(args)})"
          end

          "#{colorize_blue(ci)}#{args_str}"
        when :c
          colorize_blue(frame.c_identifier)
        when :other
          colorize_blue(frame.other_identifier)
        end

      location_str = colorize(frame.location_str, [:GREEN])
      result = "#{call_identifier_str} at #{location_str}"

      if return_str = frame.return_str
        result += " #=> #{colorize_magenta(return_str)}"
      end

      result
    end

    def initialize id, q_evt, q_cmd, thr = Thread.current
      @is_management = false
      @id = id
      @thread = thr
      @target_frames = nil
      @q_evt = q_evt
      @q_cmd = q_cmd
      @step_tp = nil
      @output = []
      @frame_formatter = method(:default_frame_formatter)
      @var_map = {} # { thread_local_var_id => obj } for DAP
      @obj_map = {} # { object_id => obj } for CDP
      @recorder = nil
      @mode = :waiting
      set_mode :running
      thr.instance_variable_set(:@__thread_client_id, id)

      ::DEBUGGER__.info("Thread \##{@id} is created.")
    end

    def deactivate
      @step_tp.disable if @step_tp
    end

    def management?
      @is_management
    end

    def mark_as_management
      @is_management = true
    end

    def set_mode mode
      # STDERR.puts "#{@mode} => #{mode} @ #{caller.inspect}"
      # pp caller

      # mode transition check
      case mode
      when :running
        raise "#{mode} is given, but #{mode}" unless self.waiting?
      when :waiting
        # TODO: there is waiting -> waiting
        # raise "#{mode} is given, but #{mode}" unless self.running?
      else
        raise "unknown mode: #{mode}"
      end

      # DEBUGGER__.warn "#{@mode} => #{mode} @ #{self.inspect}"
      @mode = mode
    end

    def running?
      @mode == :running
    end

    def waiting?
      @mode == :waiting
    end

    def name
      "##{@id} #{@thread.name || @thread.backtrace.last}"
    end

    def close
      @q_cmd.close
    end

    def inspect
      if bt = @thread.backtrace
        "#<DBG:TC #{self.id}:#{@mode}@#{bt[-1]}>"
      else # bt can be nil
        "#<DBG:TC #{self.id}:#{@mode}>"
      end
    end

    def to_s
      loc = current_frame&.location

      if loc
        str = "(#{@thread.name || @thread.status})@#{loc}"
      else
        str = "(#{@thread.name || @thread.status})@#{@thread.to_s}"
      end

      str += " (not under control)" unless self.waiting?
      str
    end

    def puts str = ''
      if @recorder&.replaying?
        prefix = colorize_dim("[replay] ")
      end
      case str
      when nil
        @output << "\n"
      when Array
        str.each{|s| puts s}
      else
        @output << "#{prefix}#{str.chomp}\n"
      end
    end

    def << req
      @q_cmd << req
    end

    def generate_info
      return unless current_frame

      { location: current_frame.location_str, line: current_frame.location.lineno }
    end

    def event! ev, *args
      @q_evt << [self, @output, ev, generate_info, *args]
      @output = []
    end

    ## events

    def wait_reply event_arg
      return if management?

      set_mode :waiting

      event!(*event_arg)
      wait_next_action
    end

    def on_load iseq, eval_src
      wait_reply [:load, iseq, eval_src]
    end

    def on_init name
      wait_reply [:init, name]
    end

    def on_trace trace_id, msg
      wait_reply [:trace, trace_id, msg]
    end

    def on_breakpoint tp, bp
      suspend tp.event, tp, bp: bp
    end

    def on_trap sig
      if waiting?
        # raise Interrupt
      else
        suspend :trap, sig: sig
      end
    end

    def on_pause
      suspend :pause
    end

    def suspend event, tp = nil, bp: nil, sig: nil, postmortem_frames: nil, replay_frames: nil, postmortem_exc: nil
      return if management?

      @current_frame_index = 0

      case
      when postmortem_frames
        @target_frames = postmortem_frames
        @postmortem = true
      when replay_frames
        @target_frames = replay_frames
      else
        @target_frames = DEBUGGER__.capture_frames(__dir__)
      end

      cf = @target_frames.first
      if cf
        @location = cf.location
        case event
        when :return, :b_return, :c_return
          cf.has_return_value = true
          cf.return_value = tp.return_value
        end

        if CatchBreakpoint === bp
          cf.has_raised_exception = true
          cf.raised_exception = bp.last_exc
        end

        if postmortem_exc
          cf.has_raised_exception = true
          cf.raised_exception = postmortem_exc
        end
      end

      if event != :pause
        show_src
        show_frames CONFIG[:show_frames] || 2

        set_mode :waiting

        if bp
          event! :suspend, :breakpoint, bp.key
        elsif sig
          event! :suspend, :trap, sig
        else
          event! :suspend, event
        end
      else
        set_mode :waiting
      end

      wait_next_action
    end

    def replay_suspend
      # @recorder.current_position
      suspend :replay, replay_frames: @recorder.current_frame
    end

    ## control all

    begin
      TracePoint.new(:raise){}.enable(target_thread: Thread.current)
      SUPPORT_TARGET_THREAD = true
    rescue ArgumentError
      SUPPORT_TARGET_THREAD = false
    end

    def step_tp iter, events = [:line, :b_return, :return]
      @step_tp.disable if @step_tp

      thread = Thread.current

      if SUPPORT_TARGET_THREAD
        @step_tp = TracePoint.new(*events){|tp|
          next if SESSION.break_at? tp.path, tp.lineno
          next if !yield(tp.event)
          next if tp.path.start_with?(__dir__)
          next if tp.path.start_with?('<internal:trace_point>')
          next unless File.exist?(tp.path) if CONFIG[:skip_nosrc]
          loc = caller_locations(1, 1).first
          next if skip_location?(loc)
          next if iter && (iter -= 1) > 0

          tp.disable
          suspend tp.event, tp
        }
        @step_tp.enable(target_thread: thread)
      else
        @step_tp = TracePoint.new(*events){|tp|
          next if thread != Thread.current
          next if SESSION.break_at? tp.path, tp.lineno
          next if !yield(tp.event)
          next if tp.path.start_with?(__dir__)
          next if tp.path.start_with?('<internal:trace_point>')
          next unless File.exist?(tp.path) if CONFIG[:skip_nosrc]
          loc = caller_locations(1, 1).first
          next if skip_location?(loc)
          next if iter && (iter -= 1) > 0

          tp.disable
          suspend tp.event, tp
        }
        @step_tp.enable
      end
    end

    ## cmd helpers

    # this method is extracted to hide frame_eval's local variables from C method eval's binding
    def instance_eval_for_cmethod frame_self, src
      frame_self.instance_eval(src)
    end

    SPECIAL_LOCAL_VARS = [
      [:raised_exception, "_raised"],
      [:return_value,     "_return"],
    ]

    def frame_eval src, re_raise: false
      @success_last_eval = false

      b = current_frame.eval_binding

      special_local_variables current_frame do |name, var|
        b.local_variable_set(name, var) if /\%/ !~ name
      end

      result = if b
                  f, _l = b.source_location
                  b.eval(src, "(rdbg)/#{f}")
                else
                  frame_self = current_frame.self
                  instance_eval_for_cmethod(frame_self, src)
                end
      @success_last_eval = true
      result

    rescue Exception => e
      return yield(e) if block_given?

      puts "eval error: #{e}"

      e.backtrace_locations&.each do |loc|
        break if loc.path == __FILE__
        puts "  #{loc}"
      end
      raise if re_raise
    end

    def show_src(frame_index: @current_frame_index,
                 update_line: false,
                 max_lines: CONFIG[:show_src_lines] || 10,
                 start_line: nil,
                 end_line: nil,
                 dir: +1)
      if @target_frames && frame = @target_frames[frame_index]
        if file_lines = frame.file_lines
          frame_line = frame.location.lineno - 1

          lines = file_lines.map.with_index do |e, i|
            cur = i == frame_line ? '=>' : '  '
            line = colorize_dim('%4d|' % (i+1))
            "#{cur}#{line} #{e}"
          end

          unless start_line
            if frame.show_line
              if dir > 0
                start_line = frame.show_line
              else
                end_line = frame.show_line - max_lines
                start_line = [end_line - max_lines, 0].max
              end
            else
              start_line = [frame_line - max_lines/2, 0].max
            end
          end

          unless end_line
            end_line = [start_line + max_lines, lines.size].min
          end

          if update_line
            frame.show_line = end_line
          end

          if start_line != end_line && max_lines
            puts "[#{start_line+1}, #{end_line}] in #{frame.pretty_path}" if !update_line && max_lines != 1
            puts lines[start_line ... end_line]
          end
        else # no file lines
          puts "# No sourcefile available for #{frame.path}"
        end
      end
    rescue Exception => e
      p e
      pp e.backtrace
      exit!
    end

    def current_frame
      if @target_frames
        @target_frames[@current_frame_index]
      else
        nil
      end
    end

    ## cmd: show

    def special_local_variables frame
      SPECIAL_LOCAL_VARS.each do |mid, name|
        next unless frame&.send("has_#{mid}")
        name = name.sub('_', '%') if frame.eval_binding.local_variable_defined?(name)
        yield name, frame.send(mid)
      end
    end

    def show_locals pat
      if s = current_frame&.self
        puts_variable_info '%self', s, pat
      end
      special_local_variables current_frame do |name, val|
        puts_variable_info name, val, pat
      end

      if vars = current_frame&.local_variables
        vars.each{|var, val|
          puts_variable_info var, val, pat
        }
      end
    end

    def show_ivars pat
      if s = current_frame&.self
        s.instance_variables.sort.each{|iv|
          value = s.instance_variable_get(iv)
          puts_variable_info iv, value, pat
        }
      end
    end

    def show_consts pat, only_self: false
      if s = current_frame&.self
        cs = {}
        if s.kind_of? Module
          cs[s] = :self
        else
          s = s.class
          cs[s] = :self unless only_self
        end

        unless only_self
          s.ancestors.each{|c| break if c == Object; cs[c] = :ancestors}
          if b = current_frame&.binding
            b.eval('Module.nesting').each{|c| cs[c] = :nesting unless cs.has_key? c}
          end
        end

        names = {}

        cs.each{|c, _|
          c.constants(false).sort.each{|name|
            next if names.has_key? name
            names[name] = nil
            value = c.const_get(name)
            puts_variable_info name, value, pat
          }
        }
      end
    end

    SKIP_GLOBAL_LIST = %i[$= $KCODE $-K $SAFE].freeze
    def show_globals pat
      global_variables.sort.each{|name|
        next if SKIP_GLOBAL_LIST.include? name

        value = eval(name.to_s)
        puts_variable_info name, value, pat
      }
    end

    def puts_variable_info label, obj, pat
      return if pat && pat !~ label

      begin
        inspected = obj.inspect
      rescue Exception => e
        inspected = e.inspect
      end
      mono_info = "#{label} = #{inspected}"

      w = SESSION::width

      if mono_info.length >= w
        info = truncate(mono_info, width: w)
      else
        valstr = colored_inspect(obj, width: 2 ** 30)
        valstr = inspected if valstr.lines.size > 1
        info = "#{colorize_cyan(label)} = #{valstr}"
      end

      puts info
    end

    def truncate(string, width:)
      str = string[0 .. (width-4)] + '...'
      str += ">" if str.start_with?("#<")
      str
    end

    ### cmd: show edit

    def show_by_editor path = nil
      unless path
        if @target_frames && frame = @target_frames[@current_frame_index]
          path = frame.path
        else
          return # can't get path
        end
      end

      if File.exist?(path)
        if editor = (ENV['RUBY_DEBUG_EDITOR'] || ENV['EDITOR'])
          puts "command: #{editor}"
          puts "   path: #{path}"
          system(editor, path)
        else
          puts "can not find editor setting: ENV['RUBY_DEBUG_EDITOR'] or ENV['EDITOR']"
        end
      else
        puts "Can not find file: #{path}"
      end
    end

    ### cmd: show frames

    def show_frames max = nil, pattern = nil
      if @target_frames && (max ||= @target_frames.size) > 0
        frames = []
        @target_frames.each_with_index{|f, i|
          next if pattern && !(f.name.match?(pattern) || f.location_str.match?(pattern))
          next if CONFIG[:skip_path] && CONFIG[:skip_path].any?{|pat|
            case pat
            when String
              f.location_str.start_with?(pat)
            when Regexp
              f.location_str.match?(pat)
            end
          }

          frames << [i, f]
        }

        size = frames.size
        max.times{|i|
          break unless frames[i]
          index, frame = frames[i]
          puts frame_str(index, frame: frame)
        }
        puts "  # and #{size - max} frames (use `bt' command for all frames)" if max < size
      end
    end

    def show_frame i=0
      puts frame_str(i)
    end

    def frame_str(i, frame: @target_frames[i])
      cur_str = (@current_frame_index == i ? '=>' : '  ')
      prefix = "#{cur_str}##{i}"
      frame_string = @frame_formatter.call(frame)
      "#{prefix}\t#{frame_string}"
    end

    ### cmd: show outline

    def show_outline expr
      begin
        obj = frame_eval(expr, re_raise: true)
      rescue Exception
        # ignore
      else
        o = Output.new(@output)

        locals = current_frame&.local_variables
        klass  = (obj.class == Class || obj.class == Module ? obj : obj.class)

        o.dump("constants", obj.constants) if obj.respond_to?(:constants)
        outline_method(o, klass, obj)
        o.dump("instance variables", obj.instance_variables)
        o.dump("class variables", klass.class_variables)
        o.dump("locals", locals.keys) if locals
      end
    end

    def outline_method(o, klass, obj)
      singleton_class = begin obj.singleton_class; rescue TypeError; nil end
      maps = class_method_map((singleton_class || klass).ancestors)
      maps.each do |mod, methods|
        name = mod == singleton_class ? "#{klass}.methods" : "#{mod}#methods"
        o.dump(name, methods)
      end
    end

    def class_method_map(classes)
      dumped = Array.new
      classes.reject { |mod| mod >= Object }.map do |mod|
        methods = mod.public_instance_methods(false).select do |m|
          dumped.push(m) unless dumped.include?(m)
        end
        [mod, methods]
      end.reverse
    end

    ## cmd: breakpoint

    def make_breakpoint args
      case args.first
      when :method
        klass_name, op, method_name, cond, cmd, path = args[1..]
        bp = MethodBreakpoint.new(current_frame.eval_binding, klass_name, op, method_name, cond: cond, command: cmd, path: path)
        begin
          bp.enable
        rescue Exception => e
          puts e.message
          ::DEBUGGER__::METHOD_ADDED_TRACKER.enable
        end

        bp
      when :watch
        ivar, object, result, cond, command, path = args[1..]
        WatchIVarBreakpoint.new(ivar, object, result, cond: cond, command: command, path: path)
      else
        raise "unknown breakpoint: #{args}"
      end
    end

    class SuspendReplay < Exception
    end

    def wait_next_action
      wait_next_action_
    rescue SuspendReplay
      replay_suspend
    end

    def wait_next_action_
      # assertions
      raise "@mode is #{@mode}" if !waiting?

      unless SESSION.active?
        pp caller
        set_mode :running
        return
      end

      while true
        begin
          set_mode :waiting if !waiting?
          cmds = @q_cmd.pop
          # pp [self, cmds: cmds]
          break unless cmds
        ensure
          set_mode :running
        end

        cmd, *args = *cmds

        case cmd
        when :continue
          break

        when :step
          step_type = args[0]
          iter = args[1]

          case step_type
          when :in
            if @recorder&.replaying?
              @recorder.step_forward
              raise SuspendReplay
            else
              step_tp iter do
                true
              end
              break
            end

          when :next
            frame = @target_frames.first
            path = frame.location.absolute_path || "!eval:#{frame.path}"
            line = frame.location.lineno

            if frame.iseq
              frame.iseq.traceable_lines_norec(lines = {})
              next_line = lines.keys.bsearch{|e| e > line}
              if !next_line && (last_line = frame.iseq.last_line) > line
                next_line = last_line
              end
            end

            depth = @target_frames.first.frame_depth

            step_tp iter do
              loc = caller_locations(2, 1).first
              loc_path = loc.absolute_path || "!eval:#{loc.path}"

              # same stack depth
              (DEBUGGER__.frame_depth - 3 <= depth) ||

              # different frame
              (next_line && loc_path == path &&
               (loc_lineno = loc.lineno) > line &&
               loc_lineno <= next_line)
            end
            break

          when :finish
            finish_frames = (iter || 1) - 1
            goal_depth = @target_frames.first.frame_depth - finish_frames

            step_tp nil, [:return, :b_return] do
              DEBUGGER__.frame_depth - 3 <= goal_depth ? true : false
            end
            break

          when :back
            if @recorder&.can_step_back?
              unless @recorder.backup_frames
                @recorder.backup_frames = @target_frames
              end
              @recorder.step_back
              raise SuspendReplay
            else
              puts "Can not step back more."
              event! :result, nil
            end

          when :reset
            if @recorder&.replaying?
              @recorder.step_reset
              raise SuspendReplay
            end

          else
            raise "unknown: #{type}"
          end

        when :eval
          eval_type, eval_src = *args

          result_type = nil

          case eval_type
          when :p
            result = frame_eval(eval_src)
            puts "=> " + color_pp(result, 2 ** 30)
            if alloc_path = ObjectSpace.allocation_sourcefile(result)
              puts "allocated at #{alloc_path}:#{ObjectSpace.allocation_sourceline(result)}"
            end
          when :pp
            result = frame_eval(eval_src)
            puts color_pp(result, SESSION.width)
            if alloc_path = ObjectSpace.allocation_sourcefile(result)
              puts "allocated at #{alloc_path}:#{ObjectSpace.allocation_sourceline(result)}"
            end
          when :call
            result = frame_eval(eval_src)
          when :irb
            begin
              result = frame_eval('binding.irb')
            ensure
              # workaround: https://github.com/ruby/debug/issues/308
              Reline.prompt_proc = nil if defined? Reline
            end
          when :display, :try_display
            failed_results = []
            eval_src.each_with_index{|src, i|
              result = frame_eval(src){|e|
                failed_results << [i, e.message]
                "<error: #{e.message}>"
              }
              puts "#{i}: #{src} = #{result}"
            }

            result_type = eval_type
            result = failed_results
          else
            raise "unknown error option: #{args.inspect}"
          end

          event! :result, result_type, result
        when :frame
          type, arg = *args
          case type
          when :up
            if @current_frame_index + 1 < @target_frames.size
              @current_frame_index += 1
              show_src max_lines: 1
              show_frame(@current_frame_index)
            end
          when :down
            if @current_frame_index > 0
              @current_frame_index -= 1
              show_src max_lines: 1
              show_frame(@current_frame_index)
            end
          when :set
            if arg
              index = arg.to_i
              if index >= 0 && index < @target_frames.size
                @current_frame_index = index
              else
                puts "out of frame index: #{index}"
              end
            end
            show_src max_lines: 1
            show_frame(@current_frame_index)
          else
            raise "unsupported frame operation: #{arg.inspect}"
          end
          event! :result, nil

        when :show
          type = args.shift

          case type
          when :backtrace
            max_lines, pattern = *args
            show_frames max_lines, pattern

          when :list
            show_src(update_line: true, **(args.first || {}))

          when :edit
            show_by_editor(args.first)

          when :default
            pat = args.shift
            show_locals pat
            show_ivars  pat
            show_consts pat, only_self: true

          when :locals
            pat = args.shift
            show_locals pat

          when :ivars
            pat = args.shift
            show_ivars pat

          when :consts
            pat = args.shift
            show_consts pat

          when :globals
            pat = args.shift
            show_globals pat

          when :outline
            show_outline args.first || 'self'

          else
            raise "unknown show param: " + [type, *args].inspect
          end

          event! :result, nil

        when :breakpoint
          case args[0]
          when :method
            bp = make_breakpoint args
            event! :result, :method_breakpoint, bp
          when :watch
            ivar, cond, command, path = args[1..]
            result = frame_eval(ivar)

            if @success_last_eval
              object =
                if b = current_frame.binding
                  b.receiver
                else
                  current_frame.self
                end
              bp = make_breakpoint [:watch, ivar, object, result, cond, command, path]
              event! :result, :watch_breakpoint, bp
            else
              event! :result, nil
            end
          end

        when :trace
          case args.shift
          when :object
            begin
              obj = frame_eval args.shift, re_raise: true
              opt = args.shift
              obj_inspect = obj.inspect

              width = 50

              if obj_inspect.length >= width
                obj_inspect = truncate(obj_inspect, width: width)
              end

              event! :result, :trace_pass, obj.object_id, obj_inspect, opt
            rescue => e
              puts e.message
              event! :result, nil
            end
          else
            raise "unreachable"
          end

        when :record
          case args[0]
          when nil
            # ok
          when :on
            # enable recording
            if !@recorder
              @recorder = Recorder.new
              @recorder.enable
            end
          when :off
            if @recorder&.enabled?
              @recorder.disable
            end
          else
            raise "unknown: #{args.inspect}"
          end

          if @recorder&.enabled?
            puts "Recorder for #{Thread.current}: on (#{@recorder.log.size} records)"
          else
            puts "Recorder for #{Thread.current}: off"
          end
          event! :result, nil

        when :dap
          process_dap args
        when :cdp
          process_cdp args
        else
          raise [cmd, *args].inspect
        end
      end

    rescue SuspendReplay, SystemExit, Interrupt
      raise
    rescue Exception => e
      pp ["DEBUGGER Exception: #{__FILE__}:#{__LINE__}", e, e.backtrace]
      raise
    end

    class Recorder
      attr_reader :log, :index
      attr_accessor :backup_frames

      include SkipPathHelper

      def initialize
        @log = []
        @index = 0
        @backup_frames = nil
        thread = Thread.current

        @tp_recorder ||= TracePoint.new(:line){|tp|
          next unless Thread.current == thread
          next if tp.path.start_with? __dir__
          next if tp.path.start_with? '<internal:'
          loc = caller_locations(1, 1).first
          next if skip_location?(loc)

          frames = DEBUGGER__.capture_frames(__dir__)
          frames.each{|frame|
            if b = frame.binding
              frame.binding = nil
              frame._local_variables = b.local_variables.map{|name|
                [name, b.local_variable_get(name)]
              }.to_h
              frame._callee = b.eval('__callee__')
            end
          }
          @log << frames
        }
      end

      def enable
        unless @tp_recorder.enabled?
          @log.clear
          @tp_recorder.enable
        end
      end

      def disable
        if @tp_recorder.enabled?
          @log.clear
          @tp_recorder.disable
        end
      end

      def enabled?
        @tp_recorder.enabled?
      end

      def step_back
        @index += 1
      end

      def step_forward
        @index -= 1
      end

      def step_reset
        @index = 0
        @backup_frames = nil
      end

      def replaying?
        @index > 0
      end

      def can_step_back?
        log.size > @index
      end

      def log_index
        @log.size - @index
      end

      def current_frame
        if @index == 0
          f = @backup_frames
          @backup_frames = nil
          f
        else
          frames = @log[log_index]
          frames
        end
      end

      # for debugging
      def current_position
        puts "INDEX: #{@index}"
        li = log_index
        @log.each_with_index{|frame, i|
          loc = frame.first&.location
          prefix = i == li ? "=> " : '   '
          puts "#{prefix} #{loc}"
        }
      end
    end

    # copied from irb
    class Output
      include Color

      MARGIN = "  "

      def initialize(output)
        @output = output
        @line_width = screen_width - MARGIN.length # right padding
      end

      def dump(name, strs)
        strs = strs.sort
        return if strs.empty?

        line = "#{colorize_blue(name)}: "

        # Attempt a single line
        if fits_on_line?(strs, cols: strs.size, offset: "#{name}: ".length)
          line += strs.join(MARGIN)
          @output << line
          return
        end

        # Multi-line
        @output << line

        # Dump with the largest # of columns that fits on a line
        cols = strs.size
        until fits_on_line?(strs, cols: cols, offset: MARGIN.length) || cols == 1
          cols -= 1
        end
        widths = col_widths(strs, cols: cols)
        strs.each_slice(cols) do |ss|
          @output << ss.map.with_index { |s, i| "#{MARGIN}%-#{widths[i]}s" % s }.join
        end
      end

      private

      def fits_on_line?(strs, cols:, offset: 0)
        width = col_widths(strs, cols: cols).sum + MARGIN.length * (cols - 1)
        width <= @line_width - offset
      end

      def col_widths(strs, cols:)
        cols.times.map do |col|
          (col...strs.size).step(cols).map do |i|
            strs[i].length
          end.max
        end
      end

      def screen_width
        SESSION.width
      rescue Errno::EINVAL # in `winsize': Invalid argument - <STDIN>
        80
      end
    end
    private_constant :Output
  end
end
