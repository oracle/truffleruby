module TypeProf
  class ISeq
    # https://github.com/ruby/ruby/pull/4468
    CASE_WHEN_CHECKMATCH = RubyVM::InstructionSequence.compile("case 1; when Integer; end").to_a.last.any? {|insn,| insn == :checkmatch }

    class << self
      def compile(file)
        compile_core(nil, file)
      end

      def compile_str(str, path = nil)
        compile_core(str, path)
      end

      private def compile_core(str, path)
        opt = RubyVM::InstructionSequence.compile_option
        opt[:inline_const_cache] = false
        opt[:peephole_optimization] = false
        opt[:specialized_instruction] = false
        opt[:operands_unification] = false
        opt[:coverage_enabled] = false

        if str
          iseq = RubyVM::InstructionSequence.compile(str, path, **opt)
        else
          iseq = RubyVM::InstructionSequence.compile_file(path, **opt)
        end

        return new(iseq.to_a)
      end
    end

    Insn = Struct.new(:insn, :operands, :lineno)
    class Insn
      def check?(insn_cmp, operands_cmp = nil)
        return insn == insn_cmp && (!operands_cmp || operands == operands_cmp)
      end
    end

    ISEQ_FRESH_ID = [0]

    def initialize(iseq)
      @id = (ISEQ_FRESH_ID[0] += 1)

      _magic, _major_version, _minor_version, _format_type, _misc,
        @name, @path, @absolute_path, @start_lineno, @type,
        @locals, @fargs_format, catch_table, insns = *iseq

      convert_insns(insns)

      add_body_start_marker(insns)

      add_exception_cont_marker(insns, catch_table)

      labels = create_label_table(insns)

      @insns = setup_insns(insns, labels)

      @fargs_format[:opt] = @fargs_format[:opt].map {|l| labels[l] } if @fargs_format[:opt]

      @catch_table = []
      catch_table.map do |type, iseq, first, last, cont, stack_depth|
        iseq = iseq ? ISeq.new(iseq) : nil
        target = labels[cont]
        entry = [type, iseq, target, stack_depth]
        labels[first].upto(labels[last]) do |i|
          @catch_table[i] ||= []
          @catch_table[i] << entry
        end
      end

      rename_insn_types

      unify_instructions
    end

    def source_location(pc)
      "#{ @path }:#{ @insns[pc].lineno }"
    end

    attr_reader :name, :path, :absolute_path, :start_lineno, :type, :locals, :fargs_format, :catch_table, :insns
    attr_reader :id

    def pretty_print(q)
      q.text "ISeq["
      q.group do
        q.nest(1) do
          q.breakable ""
          q.text "@type=          #{ @type }"
          q.breakable ", "
          q.text "@name=          #{ @name }"
          q.breakable ", "
          q.text "@path=          #{ @path }"
          q.breakable ", "
          q.text "@absolute_path= #{ @absolute_path }"
          q.breakable ", "
          q.text "@start_lineno=  #{ @start_lineno }"
          q.breakable ", "
          q.text "@fargs_format=  #{ @fargs_format.inspect }"
          q.breakable ", "
          q.text "@insns="
          q.group(2) do
            @insns.each_with_index do |(insn, *operands), i|
              q.breakable
              q.group(2, "#{ i }: #{ insn.to_s }", "") do
                q.pp operands
              end
            end
          end
        end
        q.breakable
      end
      q.text "]"
    end

    def <=>(other)
      @id <=> other.id
    end

    # Remove lineno entry and convert instructions to Insn instances
    def convert_insns(insns)
      ninsns = []
      lineno = 0
      insns.each do |e|
        case e
        when Integer # lineno
          lineno = e
        when Symbol # label or trace
          ninsns << e
        when Array
          insn, *operands = e
          ninsns << Insn.new(insn, operands, lineno)
        else
          raise "unknown iseq entry: #{ e }"
        end
      end
      insns.replace(ninsns)
    end

    # Insert a dummy instruction "_iseq_body_start"
    def add_body_start_marker(insns)
      case @type
      when :method, :block
        # skip initialization code of optional arguments
        if @fargs_format[:opt]
          label = @fargs_format[:opt].last
          i = insns.index(label) + 1
        else
          i = insns.find_index {|insn| insn.is_a?(Insn) }
        end

        # skip initialization code of keyword arguments
        while insns[i][0] == :checkkeyword
          raise if insns[i + 1].insn != :branchif
          label = insns[i + 1].operands[0]
          i = insns.index(label) + 1
        end

        insns.insert(i, Insn.new(:_iseq_body_start, [], @start_lineno))
      end
    end

    # Insert "nop" instruction to continuation point of exception handlers
    def add_exception_cont_marker(insns, catch_table)
      # rescue/ensure clauses need to have a dedicated return addresses
      # because they requires to be virtually called.
      # So, this preprocess adds "nop" to make a new insn for their return addresses
      exception_cont_labels = {}
      catch_table.map! do |type, iseq, first, last, cont, stack_depth|
        if type == :rescue || type == :ensure
          exception_cont_labels[cont] = true
          cont = :"#{ cont }_exception_cont"
        end
        [type, iseq, first, last, cont, stack_depth]
      end

      i = 0
      while i < insns.size
        e = insns[i]
        if exception_cont_labels[e]
          insns.insert(i, :"#{ e }_exception_cont", Insn.new(:nop, []))
          i += 2
        end
        i += 1
      end
    end

    def create_label_table(insns)
      pc = 0
      labels = {}
      insns.each do |e|
        if e.is_a?(Symbol)
          labels[e] = pc
        else
          pc += 1
        end
      end
      labels
    end

    def setup_insns(insns, labels)
      ninsns = []
      insns.each do |e|
        case e
        when Symbol # label or trace
          nil
        when Insn
          operands = (INSN_TABLE[e.insn] || []).zip(e.operands).map do |type, operand|
            case type
            when "ISEQ"
              operand && ISeq.new(operand)
            when "lindex_t", "rb_num_t", "VALUE", "ID", "GENTRY", "CALL_DATA"
              operand
            when "OFFSET"
              labels[operand] || raise("unknown label: #{ operand }")
            when "IVC", "ISE"
              raise unless operand.is_a?(Integer)
              :_cache_operand
            else
              raise "unknown operand type: #{ type }"
            end
          end

          ninsns << Insn.new(e.insn, operands, e.lineno)
        else
          raise "unknown iseq entry: #{ e }"
        end
      end
      ninsns
    end

    def rename_insn_types
      @insns.each do |insn|
        case insn.insn
        when :branchif
          insn.insn, insn.operands = :branch, [:if] + insn.operands
        when :branchunless
          insn.insn, insn.operands = :branch, [:unless] + insn.operands
        when :branchnil
          insn.insn, insn.operands = :branch, [:nil] + insn.operands
        when :getblockparam, :getblockparamproxy
          insn.insn = :getlocal
        end
      end
    end

    # Unify some instructions for flow-sensitive analysis
    def unify_instructions
      # This method rewrites instructions to enable flow-sensitive analysis.
      #
      # Consider `if x; ...; else; ... end`.
      # When the variable `x` is of type "Integer | nil",
      # we want to make sure that `x` is "Integer" in then clause.
      # So, we need to split the environment to two ones:
      # one is that `x` is of type "Integer", and the other is that
      # `x` is type "nil".
      #
      # However, `if x` is compiled to "getlocal; branch".
      # TypeProf evaluates them as follows:
      #
      # * "getlocal" pushes the value of `x` to the stack, amd
      # * "branch" checks the value on the top of the stack
      #
      # TypeProf does not keep where the value comes from, so
      # it is difficult to split the environment when evaluating "branch".
      #
      # This method rewrites "getlocal; branch" to "nop; getlocal_branch".
      # The two instructions are unified to "getlocal_branch" instruction,
      # so TypeProf can split the environment.
      #
      # This is a very fragile appoach because it highly depends on the compiler of Ruby.

      # gather branch targets
      # TODO: catch_table should be also considered
      branch_targets = {}
      @insns.each do |insn|
        case insn.insn
        when :branch
          branch_targets[insn.operands[1]] = true
        when :jump
          branch_targets[insn.operands[0]] = true
        end
      end

      # flow-sensitive analysis for `case var; when A; when B; when C; end`
      # find a pattern: getlocal, (dup, putobject(true), getconstant(class name), checkmatch, branch)* for ..Ruby 3.0
      # find a pattern: getlocal, (putobject(true), getconstant(class name), top(1), send(===), branch)* for Ruby 3.1..
      case_branch_list = []
      if CASE_WHEN_CHECKMATCH
        (@insns.size - 1).times do |i|
          insn = @insns[i]
          next unless insn.insn == :getlocal && insn.operands[1] == 0
          getlocal_operands = insn.operands
          nops = [i]
          new_insns = []
          j = i + 1
          while true
            case @insns[j].insn
            when :dup
              break unless @insns[j + 1].check?(:putnil, [])
              break unless @insns[j + 2].check?(:putobject, [true])
              break unless @insns[j + 3].check?(:getconstant) # TODO: support A::B::C
              break unless @insns[j + 4].check?(:checkmatch, [2])
              break unless @insns[j + 5].check?(:branch)
              target_pc = @insns[j + 5].operands[1]
              break unless @insns[target_pc].check?(:pop, [])
              nops << j << (j + 4) << target_pc
              branch_operands = @insns[j + 5][1]
              new_insns << [j + 5, Insn.new(:getlocal_checkmatch_branch, [getlocal_operands, branch_operands])]
              j += 6
            when :pop
              nops << j
              case_branch_list << [nops, new_insns]
              break
            else
              break
            end
          end
        end
      else
        (@insns.size - 1).times do |i|
          insn = @insns[i]
          next unless insn.insn == :getlocal && insn.operands[1] == 0
          getlocal_operands = insn.operands
          nops = []
          new_insns = []
          j = i + 1
          while true
            insn = @insns[j]
            if insn.check?(:putnil, [])
              break unless @insns[j + 1].check?(:putobject, [true])
              break unless @insns[j + 2].check?(:getconstant) # TODO: support A::B::C
              break unless @insns[j + 3].check?(:topn, [1])
              break unless @insns[j + 4].check?(:send, [{:mid=>:===, :flag=>20, :orig_argc=>1}, nil])
              break unless @insns[j + 5].check?(:branch)
              target_pc = @insns[j + 5].operands[1]
              break unless @insns[target_pc].check?(:pop, [])
              nops << (j + 4) #<< target_pc
              send_operands = @insns[j + 4][1]
              branch_operands = @insns[j + 5][1]
              new_insns << [j + 5, Insn.new(:arg_getlocal_send_branch, [getlocal_operands, send_operands, branch_operands])]
              j += 6
            elsif insn.check?(:pop, [])
              #nops << j
              case_branch_list << [nops, new_insns]
              break
            else
              break
            end
          end
        end
      end
      case_branch_list.each do |nops, new_insns|
        nops.each {|i| @insns[i] = Insn.new(:nop, []) }
        new_insns.each {|i, insn| @insns[i] = insn }
      end

      # find a pattern: getlocal(recv), ..., send (is_a?, respond_to?), branch
      recv_getlocal_send_branch_list = []
      (@insns.size - 1).times do |i|
        insn = @insns[i]
        if insn.insn == :getlocal && insn.operands[1] == 0
          j = i + 1
          sp = 1
          while @insns[j]
            sp = check_send_branch(sp, j)
            if sp == :match
              recv_getlocal_send_branch_list << [i, j]
              break
            end
            break if !sp
            j += 1
          end
        end
      end
      recv_getlocal_send_branch_list.each do |i, j|
        next if (i + 1 .. j + 1).any? {|i| branch_targets[i] }
        getlocal_operands = @insns[i].operands
        send_operands = @insns[j].operands
        branch_operands = @insns[j + 1].operands
        @insns[j] = Insn.new(:nop, [])
        @insns[j + 1] = Insn.new(:recv_getlocal_send_branch, [getlocal_operands, send_operands, branch_operands])
      end

      # find a pattern: getlocal, send (===), branch
      arg_getlocal_send_branch_list = []
      (@insns.size - 1).times do |i|
        insn1 = @insns[i]
        next unless insn1.insn == :getlocal && insn1.operands[1] == 0
        insn2 = @insns[i + 1]
        next unless insn2.insn == :send
        send_operands = insn2.operands[0]
        next unless send_operands[:flag] == 16 && send_operands[:orig_argc] == 1
        insn3 = @insns[i + 2]
        next unless insn3.insn == :branch
        arg_getlocal_send_branch_list << i
      end
      arg_getlocal_send_branch_list.each do |i|
        next if (i .. i + 2).any? {|i| branch_targets[i] }
        getlocal_operands = @insns[i].operands
        send_operands = @insns[i + 1].operands
        branch_operands = @insns[i + 2].operands
        @insns[i + 1] = Insn.new(:nop, [])
        @insns[i + 2] = Insn.new(:arg_getlocal_send_branch, [getlocal_operands, send_operands, branch_operands])
      end

      # find a pattern: send (block_given?), branch
      send_branch_list = []
      (@insns.size - 1).times do |i|
        insn = @insns[i]
        if insn.insn == :send
          insn = @insns[i + 1]
          if insn.insn == :branch
            send_branch_list << i
          end
        end
      end
      send_branch_list.each do |i|
        next if branch_targets[i + 1]
        send_operands = @insns[i].operands
        branch_operands = @insns[i + 1].operands
        @insns[i] = Insn.new(:nop, [])
        @insns[i + 1] = Insn.new(:send_branch, [send_operands, branch_operands])
      end

      # find a pattern: getlocal, dup, branch
      (@insns.size - 2).times do |i|
        next if branch_targets[i + 1] || branch_targets[i + 2]
        insn0 = @insns[i]
        insn1 = @insns[i + 1]
        insn2 = @insns[i + 2]
        if insn0.insn == :getlocal && insn1.insn == :dup && insn2.insn == :branch && insn0.operands[1] == 0
          getlocal_operands = insn0.operands
          dup_operands      = insn1.operands
          branch_operands   = insn2.operands
          @insns[i    ] = Insn.new(:nop, [])
          @insns[i + 1] = Insn.new(:nop, [])
          @insns[i + 2] = Insn.new(:getlocal_dup_branch, [getlocal_operands, dup_operands, branch_operands])
        end
      end

      # find a pattern: dup, setlocal, branch
      (@insns.size - 2).times do |i|
        next if branch_targets[i + 1] || branch_targets[i + 2]
        insn0 = @insns[i]
        insn1 = @insns[i + 1]
        insn2 = @insns[i + 2]
        if insn0.insn == :dup && insn1.insn == :setlocal && insn2.insn == :branch && insn1.operands[1] == 0
          dup_operands      = insn0.operands
          setlocal_operands = insn1.operands
          branch_operands   = insn2.operands
          @insns[i    ] = Insn.new(:nop, [])
          @insns[i + 1] = Insn.new(:nop, [])
          @insns[i + 2] = Insn.new(:dup_setlocal_branch, [dup_operands, setlocal_operands, branch_operands])
        end
      end

      # find a pattern: dup, branch
      (@insns.size - 1).times do |i|
        next if branch_targets[i + 1]
        insn0 = @insns[i]
        insn1 = @insns[i + 1]
        if insn0.insn == :dup && insn1.insn == :branch
          dup_operands    = insn0.operands
          branch_operands = insn1.operands
          @insns[i    ] = Insn.new(:nop, [])
          @insns[i + 1] = Insn.new(:dup_branch, [dup_operands, branch_operands])
        end
      end

      # find a pattern: getlocal, branch
      (@insns.size - 1).times do |i|
        next if branch_targets[i + 1]
        insn0 = @insns[i]
        insn1 = @insns[i + 1]
        if insn0.insn == :getlocal && insn0.operands[1] == 0 && insn1.insn == :branch
          getlocal_operands = insn0.operands
          branch_operands   = insn1.operands
          @insns[i    ] = Insn.new(:nop, [])
          @insns[i + 1] = Insn.new(:getlocal_branch, [getlocal_operands, branch_operands])
        end
      end
    end

    def check_send_branch(sp, j)
      insn = @insns[j]
      operands = insn.operands

      case insn.insn
      when :putspecialobject, :putnil, :putobject, :duparray, :putstring,
           :putself
        sp += 1
      when :newarray, :newarraykwsplat, :newhash, :concatstrings
        len, = operands
        sp =- len
        return nil if sp <= 0
        sp += 1
      when :newhashfromarray
        raise NotImplementedError, "newhashfromarray"
      when :newrange, :tostring
        sp -= 2
        return nil if sp <= 0
        sp += 1
      when :freezestring
        # XXX: should leverage this information?
      when :toregexp
        _regexp_opt, len = operands
        sp -= len
        return nil if sp <= 0
        sp += 1
      when :intern
        sp -= 1
        return nil if sp <= 0
        sp += 1
      when :definemethod, :definesmethod
      when :defineclass
        sp -= 2
      when :send, :invokesuper
        opt, = operands
        _flags = opt[:flag]
        _mid = opt[:mid]
        kw_arg = opt[:kw_arg]
        argc = opt[:orig_argc]
        argc += 1 # receiver
        argc += kw_arg.size if kw_arg
        sp -= argc
        return :match if insn.insn == :send && sp == 0 && @insns[j + 1].insn == :branch
        sp += 1
      when :arg_getlocal_send_branch
        return # not implemented
      when :invokeblock
        opt, = operands
        sp -= opt[:orig_argc]
        return nil if sp <= 0
        sp += 1
      when :invokebuiltin
        raise NotImplementedError
      when :leave, :throw
        return
      when :once
        return # not implemented
      when :branch, :jump
        return # not implemented
      when :setinstancevariable, :setclassvariable, :setglobal
        sp -= 1
      when :setlocal, :setblockparam
        return # conservative
      when :getinstancevariable, :getclassvariable, :getglobal,
           :getlocal, :getblockparam, :getblockparamproxy
        sp += 1
      when :getconstant
        sp -= 2
        return nil if sp <= 0
        sp += 1
      when :setconstant
        sp -= 2
      when :getspecial
        sp += 1
      when :setspecial
        # flip-flop
        raise NotImplementedError, "setspecial"
      when :dup
        sp += 1
      when :duphash
        sp += 1
      when :dupn
        n, = operands
        sp += n
      when :pop
        sp -= 1
      when :swap
        sp -= 2
        return nil if sp <= 0
        sp += 2
      when :reverse
        n, = operands
        sp -= n
        return nil if sp <= 0
        sp += n
      when :defined
        sp -= 1
        return nil if sp <= 0
        sp += 1
      when :checkmatch
        sp -= 2
        return nil if sp <= 0
        sp += 1
      when :checkkeyword
        sp += 1
      when :adjuststack
        n, = operands
        sp -= n
      when :nop
      when :setn
        return nil # not implemented
      when :topn
        sp += 1
      when :splatarray
        sp -= 1
        return nil if sp <= 0
        sp += 1
      when :expandarray
        num, flag = operands
        splat = flag & 1 == 1
        sp -= 1
        return nil if sp <= 0
        sp += num + (splat ? 1 : 0)
      when :concatarray
        sp -= 2
        return nil if sp <= 0
        sp += 1
      when :checktype
        sp -= 1
        return nil if sp <= 0
        sp += 1
      else
        raise "Unknown insn: #{ insn }"
      end

      return nil if sp <= 0
      sp
    end
  end
end
