module RBS
  module Prototype
    class RB
      Context = Struct.new(:module_function, :singleton, :namespace, keyword_init: true) do
        def self.initial(namespace: Namespace.root)
          self.new(module_function: false, singleton: false, namespace: namespace)
        end

        def method_kind
          if singleton
            :singleton
          elsif module_function
            :singleton_instance
          else
            :instance
          end
        end

        def attribute_kind
          if singleton
            :singleton
          else
            :instance
          end
        end
      end

      attr_reader :source_decls
      attr_reader :toplevel_members

      def initialize
        @source_decls = []
      end

      def decls
        decls = []

        top_decls, top_members = source_decls.partition {|decl| decl.is_a?(AST::Declarations::Base) }

        decls.push(*top_decls)

        unless top_members.empty?
          top = AST::Declarations::Class.new(
            name: TypeName.new(name: :Object, namespace: Namespace.empty),
            super_class: nil,
            members: top_members,
            annotations: [],
            comment: nil,
            location: nil,
            type_params: AST::Declarations::ModuleTypeParams.empty
          )
          decls << top
        end

        decls
      end

      def parse(string)
        comments = Ripper.lex(string).yield_self do |tokens|
          tokens.each.with_object({}) do |token, hash|
            if token[1] == :on_comment
              line = token[0][0]
              body = token[2][2..-1]

              body = "\n" if body.empty?

              comment = AST::Comment.new(string: body, location: nil)
              if (prev_comment = hash[line - 1])
                hash[line - 1] = nil
                hash[line] = AST::Comment.new(string: prev_comment.string + comment.string,
                                              location: nil)
              else
                hash[line] = comment
              end
            end
          end
        end

        process RubyVM::AbstractSyntaxTree.parse(string), decls: source_decls, comments: comments, context: Context.initial
      end

      def process(node, decls:, comments:, context:)
        case node.type
        when :CLASS
          class_name, super_class, *class_body = node.children
          kls = AST::Declarations::Class.new(
            name: const_to_name(class_name),
            super_class: super_class && AST::Declarations::Class::Super.new(name: const_to_name(super_class), args: [], location: nil),
            type_params: AST::Declarations::ModuleTypeParams.empty,
            members: [],
            annotations: [],
            location: nil,
            comment: comments[node.first_lineno - 1]
          )

          decls.push kls

          new_ctx = Context.initial(namespace: context.namespace + kls.name.to_namespace)
          each_node class_body do |child|
            process child, decls: kls.members, comments: comments, context: new_ctx
          end
          remove_unnecessary_accessibility_methods! kls.members

        when :MODULE
          module_name, *module_body = node.children

          mod = AST::Declarations::Module.new(
            name: const_to_name(module_name),
            type_params: AST::Declarations::ModuleTypeParams.empty,
            self_types: [],
            members: [],
            annotations: [],
            location: nil,
            comment: comments[node.first_lineno - 1]
          )

          decls.push mod

          new_ctx = Context.initial(namespace: context.namespace + mod.name.to_namespace)
          each_node module_body do |child|
            process child, decls: mod.members, comments: comments, context: new_ctx
          end
          remove_unnecessary_accessibility_methods! mod.members

        when :SCLASS
          this, body = node.children

          if this.type != :SELF
            RBS.logger.warn "`class <<` syntax with not-self may be compiled to incorrect code: #{this}"
          end

          accessibility = current_accessibility(decls)

          ctx = Context.initial.tap { |ctx| ctx.singleton = true }
          process_children(body, decls: decls, comments: comments, context: ctx)

          decls << accessibility

        when :DEFN, :DEFS
            if node.type == :DEFN
              def_name, def_body = node.children
              kind = context.method_kind
            else
              _, def_name, def_body = node.children
              kind = :singleton
            end

            types = [
              MethodType.new(
                type_params: [],
                type: function_type_from_body(def_body, def_name),
                block: block_from_body(def_body),
                location: nil
              )
            ]

            member = AST::Members::MethodDefinition.new(
              name: def_name,
              location: nil,
              annotations: [],
              types: types,
              kind: kind,
              comment: comments[node.first_lineno - 1],
              overload: false
            )

            decls.push member unless decls.include?(member)

        when :ALIAS
          new_name, old_name = node.children.map { |c| literal_to_symbol(c) }
          member = AST::Members::Alias.new(
            new_name: new_name,
            old_name: old_name,
            kind: context.singleton ? :singleton : :instance,
            annotations: [],
            location: nil,
            comment: comments[node.first_lineno - 1],
          )
          decls.push member unless decls.include?(member)

        when :FCALL, :VCALL
          # Inside method definition cannot reach here.
          args = node.children[1]&.children || []

          case node.children[0]
          when :include
            args.each do |arg|
              if (name = const_to_name(arg))
                decls << AST::Members::Include.new(
                  name: name,
                  args: [],
                  annotations: [],
                  location: nil,
                  comment: comments[node.first_lineno - 1]
                )
              end
            end
          when :extend
            args.each do |arg|
              if (name = const_to_name(arg, context: context))
                decls << AST::Members::Extend.new(
                  name: name,
                  args: [],
                  annotations: [],
                  location: nil,
                  comment: comments[node.first_lineno - 1]
                )
              end
            end
          when :attr_reader
            args.each do |arg|
              if arg && (name = literal_to_symbol(arg))
                decls << AST::Members::AttrReader.new(
                  name: name,
                  ivar_name: nil,
                  type: Types::Bases::Any.new(location: nil),
                  kind: context.attribute_kind,
                  location: nil,
                  comment: comments[node.first_lineno - 1],
                  annotations: []
                )
              end
            end
          when :attr_accessor
            args.each do |arg|
              if arg && (name = literal_to_symbol(arg))
                decls << AST::Members::AttrAccessor.new(
                  name: name,
                  ivar_name: nil,
                  type: Types::Bases::Any.new(location: nil),
                  kind: context.attribute_kind,
                  location: nil,
                  comment: comments[node.first_lineno - 1],
                  annotations: []
                )
              end
            end
          when :attr_writer
            args.each do |arg|
              if arg && (name = literal_to_symbol(arg))
                decls << AST::Members::AttrWriter.new(
                  name: name,
                  ivar_name: nil,
                  type: Types::Bases::Any.new(location: nil),
                  kind: context.attribute_kind,
                  location: nil,
                  comment: comments[node.first_lineno - 1],
                  annotations: []
                )
              end
            end
          when :alias_method
            if args[0] && args[1] && (new_name = literal_to_symbol(args[0])) && (old_name = literal_to_symbol(args[1]))
              decls << AST::Members::Alias.new(
                new_name: new_name,
                old_name: old_name,
                kind: context.singleton ? :singleton : :instance,
                annotations: [],
                location: nil,
                comment: comments[node.first_lineno - 1],
              )
            end
          when :module_function
            if args.empty?
              context.module_function = true
            else
              module_func_context = context.dup.tap { |ctx| ctx.module_function = true }
              args.each do |arg|
                if arg && (name = literal_to_symbol(arg))
                  if i = find_def_index_by_name(decls, name)
                    decls[i] = decls[i].update(kind: :singleton_instance)
                  end
                elsif arg
                  process arg, decls: decls, comments: comments, context: module_func_context
                end
              end
            end
          when :public, :private
            accessibility = __send__(node.children[0])
            if args.empty?
              decls << accessibility
            else
              args.each do |arg|
                if arg && (name = literal_to_symbol(arg))
                  if i = find_def_index_by_name(decls, name)
                    current = current_accessibility(decls, i)
                    if current != accessibility
                      decls.insert(i + 1, current)
                      decls.insert(i, accessibility)
                    end
                  end
                end
              end

              # For `private def foo` syntax
              current = current_accessibility(decls)
              decls << accessibility
              process_children(node, decls: decls, comments: comments, context: context)
              decls << current
            end
          else
            process_children(node, decls: decls, comments: comments, context: context)
          end

        when :ITER
          method_name = node.children.first.children.first
          case method_name
          when :refine
            # ignore
          else
            process_children(node, decls: decls, comments: comments, context: context)
          end

        when :CDECL
          const_name = case
                       when node.children[0].is_a?(Symbol)
                         TypeName.new(name: node.children[0], namespace: Namespace.empty)
                       else
                         const_to_name(node.children[0])
                       end

          value_node = node.children.last
          type = if value_node.nil?
                  # Give up type prediction when node is MASGN.
                  Types::Bases::Any.new(location: nil)
                else
                  node_type(value_node)
                end
          decls << AST::Declarations::Constant.new(
            name: const_name,
            type: type,
            location: nil,
            comment: comments[node.first_lineno - 1]
          )

        else
          process_children(node, decls: decls, comments: comments, context: context)
        end
      end

      def process_children(node, decls:, comments:, context:)
        each_child node do |child|
          process child, decls: decls, comments: comments, context: context
        end
      end

      def const_to_name(node, context: nil)
        case node&.type
        when :CONST
          TypeName.new(name: node.children[0], namespace: Namespace.empty)
        when :COLON2
          if node.children[0]
            namespace = const_to_name(node.children[0]).to_namespace
          else
            namespace = Namespace.empty
          end

          TypeName.new(name: node.children[1], namespace: namespace)
        when :COLON3
          TypeName.new(name: node.children[0], namespace: Namespace.root)
        when :SELF
          context&.then { |c| c.namespace.to_type_name }
        end
      end

      def literal_to_symbol(node)
        case node.type
        when :LIT
          node.children[0] if node.children[0].is_a?(Symbol)
        when :STR
          node.children[0].to_sym
        end
      end

      def each_node(nodes)
        nodes.each do |child|
          if child.is_a?(RubyVM::AbstractSyntaxTree::Node)
            yield child
          end
        end
      end

      def each_child(node, &block)
        each_node node.children, &block
      end

      def function_type_from_body(node, def_name)
        table_node, args_node, *_ = node.children

        pre_num, _pre_init, opt, _first_post, post_num, _post_init, rest, kw, kwrest, _block = args_from_node(args_node)

        return_type = if def_name == :initialize
                        Types::Bases::Void.new(location: nil)
                      else
                        function_return_type_from_body(node)
                      end

        fun = Types::Function.empty(return_type)

        table_node.take(pre_num).each do |name|
          fun.required_positionals << Types::Function::Param.new(name: name, type: untyped)
        end

        while opt&.type == :OPT_ARG
          lvasgn, opt = opt.children
          name = lvasgn.children[0]
          fun.optional_positionals << Types::Function::Param.new(
            name: name,
            type: node_type(lvasgn.children[1])
          )
        end

        if rest
          rest_name = rest == :* ? nil : rest # # For `def f(...) end` syntax
          fun = fun.update(rest_positionals: Types::Function::Param.new(name: rest_name, type: untyped))
        end

        table_node.drop(fun.required_positionals.size + fun.optional_positionals.size + (fun.rest_positionals ? 1 : 0)).take(post_num).each do |name|
          fun.trailing_positionals << Types::Function::Param.new(name: name, type: untyped)
        end

        while kw
          lvasgn, kw = kw.children
          name, value = lvasgn.children

          case value
          when nil, :NODE_SPECIAL_REQUIRED_KEYWORD
            fun.required_keywords[name] = Types::Function::Param.new(name: name, type: untyped)
          when RubyVM::AbstractSyntaxTree::Node
            fun.optional_keywords[name] = Types::Function::Param.new(name: name, type: node_type(value))
          else
            raise "Unexpected keyword arg value: #{value}"
          end
        end

        if kwrest && kwrest.children.any?
          fun = fun.update(rest_keywords: Types::Function::Param.new(name: kwrest.children[0], type: untyped))
        end

        fun
      end

      def function_return_type_from_body(node)
        body = node.children[2]
        return Types::Bases::Nil.new(location: nil) unless body

        if body.type == :BLOCK
          return_stmts = any_node?(body) do |n|
            n.type == :RETURN
          end&.map do |return_node|
            returned_value = return_node.children[0]
            returned_value ? literal_to_type(returned_value) : Types::Bases::Nil.new(location: nil)
          end || []
          last_node = body.children.last
          last_evaluated =  last_node ? literal_to_type(last_node) : Types::Bases::Nil.new(location: nil)
          types_to_union_type([*return_stmts, last_evaluated])
        else
          literal_to_type(body)
        end
      end

      def literal_to_type(node)
        case node.type
        when :STR
          lit = node.children[0]
          if lit.match?(/\A[ -~]+\z/)
            Types::Literal.new(literal: lit, location: nil)
          else
            BuiltinNames::String.instance_type
          end
        when :DSTR, :XSTR
          BuiltinNames::String.instance_type
        when :DSYM
          BuiltinNames::Symbol.instance_type
        when :DREGX
          BuiltinNames::Regexp.instance_type
        when :TRUE
          BuiltinNames::TrueClass.instance_type
        when :FALSE
          BuiltinNames::FalseClass.instance_type
        when :NIL
          Types::Bases::Nil.new(location: nil)
        when :LIT
          lit = node.children[0]
          case lit
          when Symbol
            if lit.match?(/\A[ -~]+\z/)
              Types::Literal.new(literal: lit, location: nil)
            else
              BuiltinNames::Symbol.instance_type
            end
          when Integer
            Types::Literal.new(literal: lit, location: nil)
          else
            type_name = TypeName.new(name: lit.class.name.to_sym, namespace: Namespace.root)
            Types::ClassInstance.new(name: type_name, args: [], location: nil)
          end
        when :ZLIST, :ZARRAY
          BuiltinNames::Array.instance_type([untyped])
        when :LIST, :ARRAY
          elem_types = node.children.compact.map { |e| literal_to_type(e) }
          t = types_to_union_type(elem_types)
          BuiltinNames::Array.instance_type([t])
        when :DOT2, :DOT3
          types = node.children.map { |c| literal_to_type(c) }
          type = range_element_type(types)
          BuiltinNames::Range.instance_type([type])
        when :HASH
          list = node.children[0]
          if list
            children = list.children
            children.pop
          else
            children = []
          end

          key_types = []
          value_types = []
          children.each_slice(2) do |k, v|
            if k
              key_types << literal_to_type(k)
              value_types << literal_to_type(v)
            else
              key_types << untyped
              value_types << untyped
            end
          end

          if !key_types.empty? && key_types.all? { |t| t.is_a?(Types::Literal) }
            fields = key_types.map { |t| t.literal }.zip(value_types).to_h
            Types::Record.new(fields: fields, location: nil)
          else
            key_type = types_to_union_type(key_types)
            value_type = types_to_union_type(value_types)
            BuiltinNames::Hash.instance_type([key_type, value_type])
          end
        else
          untyped
        end
      end

      def types_to_union_type(types)
        return untyped if types.empty?

        uniq = types.uniq
        return uniq.first if uniq.size == 1

        Types::Union.new(types: uniq, location: nil)
      end

      def range_element_type(types)
        types = types.reject { |t| t == untyped }
        return untyped if types.empty?

        types = types.map do |t|
          if t.is_a?(Types::Literal)
            type_name = TypeName.new(name: t.literal.class.name.to_sym, namespace: Namespace.root)
            Types::ClassInstance.new(name: type_name, args: [], location: nil)
          else
            t
          end
        end.uniq

        if types.size == 1
          types.first
        else
          untyped
        end
      end

      def block_from_body(node)
        _, args_node, body_node = node.children

        _pre_num, _pre_init, _opt, _first_post, _post_num, _post_init, _rest, _kw, _kwrest, block = args_from_node(args_node)

        method_block = nil

        if block
          method_block = Types::Block.new(
            # HACK: The `block` is :& on `def m(...)` syntax.
            #       In this case the block looks optional in most cases, so it marks optional.
            #       In other cases, we can't determine which is required or optional, so it marks required.
            required: block != :&,
            type: Types::Function.empty(untyped)
          )
        end

        if body_node
          if (yields = any_node?(body_node) {|n| n.type == :YIELD })
            method_block = Types::Block.new(
              required: true,
              type: Types::Function.empty(untyped)
            )

            yields.each do |yield_node|
              array_content = yield_node.children[0]&.children&.compact || []

              positionals, keywords = if keyword_hash?(array_content.last)
                                        [array_content.take(array_content.size - 1), array_content.last]
                                      else
                                        [array_content, nil]
                                      end

              if (diff = positionals.size - method_block.type.required_positionals.size) > 0
                diff.times do
                  method_block.type.required_positionals << Types::Function::Param.new(
                    type: untyped,
                    name: nil
                  )
                end
              end

              if keywords
                keywords.children[0].children.each_slice(2) do |key_node, value_node|
                  if key_node
                    key = key_node.children[0]
                    method_block.type.required_keywords[key] ||=
                      Types::Function::Param.new(
                        type: untyped,
                        name: nil
                      )
                  end
                end
              end
            end
          end
        end

        method_block
      end

      # NOTE: args_node may be a nil by a bug
      #       https://bugs.ruby-lang.org/issues/17495
      def args_from_node(args_node)
        args_node&.children || [0, nil, nil, nil, 0, nil, nil, nil, nil, nil]
      end

      def keyword_hash?(node)
        if node
          if node.type == :HASH
            node.children[0].children.compact.each_slice(2).all? {|key, _|
              key.type == :LIT && key.children[0].is_a?(Symbol)
            }
          end
        end
      end

      def any_node?(node, nodes: [], &block)
        if yield(node)
          nodes << node
        end

        each_child node do |child|
          any_node? child, nodes: nodes, &block
        end

        nodes.empty? ? nil : nodes
      end

      def node_type(node, default: Types::Bases::Any.new(location: nil))
        case node.type
        when :LIT
          case node.children[0]
          when Symbol
            BuiltinNames::Symbol.instance_type
          when Integer
            BuiltinNames::Integer.instance_type
          when Float
            BuiltinNames::Float.instance_type
          else
            default
          end
        when :STR, :DSTR
          BuiltinNames::String.instance_type
        when :NIL
          # This type is technical non-sense, but may help practically.
          Types::Optional.new(
            type: Types::Bases::Any.new(location: nil),
            location: nil
          )
        when :TRUE, :FALSE
          Types::Bases::Bool.new(location: nil)
        when :ARRAY, :LIST
          BuiltinNames::Array.instance_type(default)
        when :HASH
          BuiltinNames::Hash.instance_type(default, default)
        else
          default
        end
      end

      def untyped
        @untyped ||= Types::Bases::Any.new(location: nil)
      end

      def private
        @private ||= AST::Members::Private.new(location: nil)
      end

      def public
        @public ||= AST::Members::Public.new(location: nil)
      end

      def current_accessibility(decls, index = decls.size)
        idx = decls.slice(0, index).rindex { |decl| decl == private || decl == public }
        (idx && decls[idx]) || public
      end

      def remove_unnecessary_accessibility_methods!(decls)
        current = public
        idx = 0

        loop do
          decl = decls[idx] or break
          if current == decl
            decls.delete_at(idx)
            next
          end

          if 0 < idx && is_accessibility?(decls[idx - 1]) && is_accessibility?(decl)
            decls.delete_at(idx - 1)
            idx -= 1
            current = current_accessibility(decls, idx)
            next
          end

          current = decl if is_accessibility?(decl)
          idx += 1
        end

        decls.pop while decls.last && is_accessibility?(decls.last)
      end

      def is_accessibility?(decl)
        decl == public || decl == private
      end

      def find_def_index_by_name(decls, name)
        decls.find_index do |decl|
          case decl
          when AST::Members::MethodDefinition, AST::Members::AttrReader
            decl.name == name
          when AST::Members::AttrWriter
            decl.name == :"#{name}="
          end
        end
      end
    end
  end
end
