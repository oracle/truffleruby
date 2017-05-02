module Optcarrot
  # dirty methods manipulating and generating methods...
  module CodeOptimizationHelper
    def initialize(loglevel, enabled_opts)
      @loglevel = loglevel
      options = self.class::OPTIONS
      opts = {}
      enabled_opts ||= [:all]
      default =
        (enabled_opts == [:all] || enabled_opts != [] && enabled_opts.all? {|opt| opt.to_s.start_with?("-") })
      options.each {|opt| opts[opt] = default }
      (enabled_opts - [:none, :all]).each do |opt|
        val = true
        if opt.to_s.start_with?("-")
          opt = opt.to_s[1..-1].to_sym
          val = false
        end
        raise "unknown optimization: `#{ opt }'" unless options.include?(opt)
        opts[opt] = val
      end
      options.each {|opt| instance_variable_set(:"@#{ opt }", opts[opt]) }
    end

    def depends(opt, depended_opt)
      if instance_variable_get(:"@#{ opt }") && !instance_variable_get(:"@#{ depended_opt }")
        raise "`#{ opt }' depends upon `#{ depended_opt }'"
      end
    end

    def gen(*codes)
      codes.map {|code| code.to_s.chomp }.join("\n") + "\n"
    end

    # change indent
    def indent(i, code)
      if i > 0
        code.gsub(/^(.+)$/) { " " * i + $1 }
      elsif i < 0
        code.gsub(/^ {#{ -i }}/, "")
      else
        code
      end
    end

    # generate a branch
    def branch(cond, code1, code2)
      gen(
        "if #{ cond }",
        indent(2, code1),
        "else",
        indent(2, code2),
        "end",
      )
    end

    MethodDef = Struct.new(:params, :body)

    METHOD_DEFINITIONS_RE = /
      ^(\ +)def\s+(\w+)(?:\((.*)\))?\n
      ^((?:\1\ +.*\n|\n)*)
      ^\1end$
    /x
    # extract all method definitions
    def parse_method_definitions(file)
      src = File.read(file)
      mdefs = {}
      src.scan(METHOD_DEFINITIONS_RE) do |indent, meth, params, body|
        body = indent(-indent.size - 2, body)

        # noramlize: break `when ... then`
        body = body.gsub(/^( *)when +(.*?) +then +(.*)/) { $1 + "when #{ $2 }\n" + $1 + "  " + $3 }

        # normalize: return unless
        body = "if " + $1 + indent(2, $') + "end\n" if body =~ /\Areturn unless (.*)/

        # normalize: if modifier -> if statement
        nil while body.gsub!(/^( *)((?!#)\S.*) ((?:if|unless) .*\n)/) { indent($1.size, gen($3, "  " + $2, "end")) }

        mdefs[meth.to_sym] = MethodDef[params ? params.split(", ") : nil, body]
      end
      mdefs
    end

    # inline method calls with no arguments
    def expand_methods(code, mdefs, meths = mdefs.keys)
      code.gsub(/^( *)\b(#{ meths * "|" })\b(?:\((.*?)\))?\n/) do
        indent, meth, args = $1, $2, $3
        body = mdefs[meth.to_sym]
        body = body.body if body.is_a?(MethodDef)
        if args
          mdefs[meth.to_sym].params.zip(args.split(", ")) do |param, arg|
            body = replace_var(body, param, arg)
          end
        end
        indent(indent.size, body)
      end
    end

    def expand_inline_methods(code, meth, mdef)
      code.gsub(/\b#{ meth }\b(?:\(((?:@?\w+, )*@?\w+)\))?/) do
        args = $1
        b = "(#{ mdef.body.chomp.gsub(/ *#.*/, "").gsub("\n", "; ") })"
        if args
          mdef.params.zip(args.split(", ")) do |param, arg|
            b = replace_var(b, param, arg)
          end
        end
        b
      end
    end

    def replace_var(code, var, bool)
      re = var.start_with?("@") ? /#{ var }\b/ : /\b#{ var }\b/
      code.gsub(re) { bool }
    end

    def replace_cond_var(code, var, bool)
      code.gsub(/(if|unless)\s#{ var }\b/) { $1 + " " + bool }
    end

    TRIVIAL_BRANCH_RE = /
      ^(\ *)(if|unless)\ (true|false)\n
      ^((?:\1\ +.*\n|\n)*)
       (?:
         \1else\n
         ((?:\1\ +.*\n|\n)*)
       )?
      ^\1end\n
    /x
    # remove "if true" or "if false"
    def remove_trivial_branches(code)
      code = code.dup
      nil while
        code.gsub!(TRIVIAL_BRANCH_RE) do
          if ($2 == "if") == ($3 == "true")
            indent(-2, $4)
          else
            $5 ? indent(-2, $5) : ""
          end
        end
      code
    end

    # replace instance variables with temporal local variables
    # CAUTION: the instance variable must not be accessed out of CPU#run
    def localize_instance_variables(code, ivars = code.scan(/@\w+/).uniq.sort)
      ivars = ivars.map {|ivar| ivar.to_s[1..-1] }

      inits, finals = [], []
      ivars.each do |ivar|
        lvar = "__#{ ivar }__"
        inits << "#{ lvar } = @#{ ivar }"
        finals << "@#{ ivar } = #{ lvar }"
      end

      code = code.gsub(/@(#{ ivars * "|" })\b/) { "__#{ $1 }__" }

      gen(
        "begin",
        indent(2, inits.join("\n")),
        indent(2, code),
        "ensure",
        indent(2, finals.join("\n")),
        "end",
      )
    end
  end
end
