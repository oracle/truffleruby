module RBS
  class Validator
    attr_reader :env
    attr_reader :resolver

    def initialize(env:, resolver:)
      @env = env
      @resolver = resolver
    end

    def absolute_type(type, context:)
      type.map_type_name do |type_name, _, type|
        resolver.resolve(type_name, context: context) || yield(type)
      end
    end

    # Validates presence of the relative type, and application arity match.
    def validate_type(type, context:)
      case type
      when Types::ClassInstance, Types::Interface
        # @type var type: Types::ClassInstance | Types::Interface
        if type.name.namespace.relative?
          type = _ = absolute_type(type, context: context) do |_|
            NoTypeFoundError.check!(type.name.absolute!, env: env, location: type.location)
          end
        end

        type_params = case type
                      when Types::ClassInstance
                        env.class_decls[type.name]&.type_params
                      when Types::Interface
                        env.interface_decls[type.name]&.decl&.type_params
                      end

        unless type_params
          raise NoTypeFoundError.new(type_name: type.name, location: type.location)
        end

        InvalidTypeApplicationError.check!(
          type_name: type.name,
          args: type.args,
          params: type_params.each.map(&:name),
          location: type.location
        )

      when Types::Alias, Types::ClassSingleton
        # @type var type: Types::Alias | Types::ClassSingleton
        type = _ = absolute_type(type, context: context) { type.name.absolute! }
        NoTypeFoundError.check!(type.name, env: env, location: type.location)
      end

      type.each_type do |type|
        validate_type(type, context: context)
      end
    end
  end
end
