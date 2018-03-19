Truffle::Patching.require_original __FILE__

module Bundler
  class Runtime

    alias_method :original_setup, :setup

    def setup(*groups)
      original_setup(*groups)

      groups.map!(&:to_sym)
      specs = groups.any? ? @definition.specs_for(groups) : requested_specs

      specs.each { |spec| Truffle::Patching.insert_patching_dir(spec.name, *spec.load_paths) }

      self
    end
  end
end
