module RBS
  module AST
    module Declarations
      class Base
      end

      class ModuleTypeParams
        attr_reader :params

        TypeParam = _ = Struct.new(:name, :variance, :skip_validation, :location, keyword_init: true) do
          # @implements TypeParam

          def to_json(state = _ = nil)
            {
              name: name,
              variance: variance,
              skip_validation: skip_validation,
            }.to_json(state)
          end

          def ==(other)
            other.is_a?(TypeParam) &&
              other.name == name &&
              other.variance == variance &&
              other.skip_validation == skip_validation
          end

          alias eql? ==

          def hash
            self.class.hash ^ name.hash ^ variance.hash ^ skip_validation.hash
          end
        end

        def initialize()
          @params = []
        end

        def add(param)
          params << param
          self
        end

        def ==(other)
          other.is_a?(ModuleTypeParams) && other.params == params
        end

        alias eql? ==

        def hash
          params.hash
        end

        def [](name)
          params.find {|p| p.name == name }
        end

        def to_json(state = _ = nil)
          {
            params: params
          }.to_json(state)
        end

        def each(&block)
          if block
            params.each(&block)
          else
            params.each
          end
        end

        def self.empty
          new
        end

        def variance(name)
          var = self[name] or raise
          var.variance
        end

        def skip_validation?(name)
          var = self[name] or raise
          var.skip_validation
        end

        def empty?
          params.empty?
        end

        def size
          params.size
        end

        def rename_to(names)
          ModuleTypeParams.new().tap do |params|
            names.each.with_index do |new_name, index|
              param = self.params[index]
              params.add(TypeParam.new(name: new_name, variance: param.variance, skip_validation: param.skip_validation, location: param.location))
            end
          end
        end
      end

      module NestedDeclarationHelper
        def each_member
          if block_given?
            members.each do |member|
              if member.is_a?(Members::Base)
                yield(_ = member)
              end
            end
          else
            enum_for :each_member
          end
        end

        def each_decl
          if block_given?
            members.each do |member|
              if member.is_a?(Base)
                yield(_ = member)
              end
            end
          else
            enum_for :each_decl
          end
        end
      end

      module MixinHelper
        def each_mixin(&block)
          if block
            @mixins ||= begin
                          _ = members.select do |member|
                            case member
                            when Members::Include, Members::Extend, Members::Prepend
                              true
                            else
                              false
                            end
                          end
                        end
            @mixins.each(&block)
          else
            enum_for :each_mixin
          end
        end
      end

      class Class < Base
        class Super
          attr_reader :name
          attr_reader :args
          attr_reader :location

          def initialize(name:, args:, location:)
            @name = name
            @args = args
            @location = location
          end

          def ==(other)
            other.is_a?(Super) && other.name == name && other.args == args
          end

          alias eql? ==

          def hash
            self.class.hash ^ name.hash ^ args.hash
          end

          def to_json(state = _ = nil)
            {
              name: name,
              args: args,
              location: location
            }.to_json(state)
          end
        end

        include NestedDeclarationHelper
        include MixinHelper

        attr_reader :name
        attr_reader :type_params
        attr_reader :members
        attr_reader :super_class
        attr_reader :annotations
        attr_reader :location
        attr_reader :comment

        def initialize(name:, type_params:, super_class:, members:, annotations:, location:, comment:)
          @name = name
          @type_params = type_params
          @super_class = super_class
          @members = members
          @annotations = annotations
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Class) &&
            other.name == name &&
            other.type_params == type_params &&
            other.super_class == super_class &&
            other.members == members
        end

        alias eql? ==

        def hash
          self.class.hash ^ name.hash ^ type_params.hash ^ super_class.hash ^ members.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :class,
            name: name,
            type_params: type_params,
            members: members,
            super_class: super_class,
            annotations: annotations,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end

      class Module < Base
        class Self
          attr_reader :name
          attr_reader :args
          attr_reader :location

          def initialize(name:, args:, location:)
            @name = name
            @args = args
            @location = location
          end

          def ==(other)
            other.is_a?(Self) && other.name == name && other.args == args
          end

          alias eql? ==

          def hash
            self.class.hash ^ name.hash ^ args.hash ^ location.hash
          end

          def to_json(state = _ = nil)
            {
              name: name,
              args: args,
              location: location
            }.to_json(state)
          end

          def to_s
            if args.empty?
              name.to_s
            else
              "#{name}[#{args.join(", ")}]"
            end
          end
        end

        include NestedDeclarationHelper
        include MixinHelper

        attr_reader :name
        attr_reader :type_params
        attr_reader :members
        attr_reader :location
        attr_reader :annotations
        attr_reader :self_types
        attr_reader :comment

        def initialize(name:, type_params:, members:, self_types:, annotations:, location:, comment:)
          @name = name
          @type_params = type_params
          @self_types = self_types
          @members = members
          @annotations = annotations
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Module) &&
            other.name == name &&
            other.type_params == type_params &&
            other.self_types == self_types &&
            other.members == members
        end

        alias eql? ==

        def hash
          self.class.hash ^ name.hash ^ type_params.hash ^ self_types.hash ^ members.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :module,
            name: name,
            type_params: type_params,
            members: members,
            self_types: self_types,
            annotations: annotations,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end

      class Interface < Base
        attr_reader :name
        attr_reader :type_params
        attr_reader :members
        attr_reader :annotations
        attr_reader :location
        attr_reader :comment

        include MixinHelper

        def initialize(name:, type_params:, members:, annotations:, location:, comment:)
          @name = name
          @type_params = type_params
          @members = members
          @annotations = annotations
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Interface) &&
            other.name == name &&
            other.type_params == type_params &&
            other.members == members
        end

        alias eql? ==

        def hash
          self.class.hash ^ type_params.hash ^ members.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :interface,
            name: name,
            type_params: type_params,
            members: members,
            annotations: annotations,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end

      class Alias < Base
        attr_reader :name
        attr_reader :type
        attr_reader :annotations
        attr_reader :location
        attr_reader :comment

        def initialize(name:, type:, annotations:, location:, comment:)
          @name = name
          @type = type
          @annotations = annotations
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Alias) &&
            other.name == name &&
            other.type == type
        end

        alias eql? ==

        def hash
          self.class.hash ^ name.hash ^ type.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :alias,
            name: name,
            type: type,
            annotations: annotations,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end

      class Constant < Base
        attr_reader :name
        attr_reader :type
        attr_reader :location
        attr_reader :comment

        def initialize(name:, type:, location:, comment:)
          @name = name
          @type = type
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Constant) &&
            other.name == name &&
            other.type == type
        end

        alias eql? ==

        def hash
          self.class.hash ^ name.hash ^ type.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :constant,
            name: name,
            type: type,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end

      class Global < Base
        attr_reader :name
        attr_reader :type
        attr_reader :location
        attr_reader :comment

        def initialize(name:, type:, location:, comment:)
          @name = name
          @type = type
          @location = location
          @comment = comment
        end

        def ==(other)
          other.is_a?(Global) &&
            other.name == name &&
            other.type == type
        end

        alias eql? ==

        def hash
          self.class.hash ^ name.hash ^ type.hash
        end

        def to_json(state = _ = nil)
          {
            declaration: :global,
            name: name,
            type: type,
            location: location,
            comment: comment
          }.to_json(state)
        end
      end
    end
  end
end
