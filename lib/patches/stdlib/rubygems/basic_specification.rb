Truffle::Patching.require_original __FILE__

if !ENV['TRUFFLERUBY_CEXT_ENABLED'] || ENV['TRUFFLERUBY_CEXT_ENABLED'] == "false"
  module Truffle::Patching::NoWarnIfBuildingCextDisabled
    def contains_requirable_file? file
      if @ignored then
        return false
      elsif missing_extensions? then
        @ignored = true

        # Truffle: warn only if verbose when cext building is not enabled
        if $VERBOSE
          warn "Ignoring #{full_name} because its extensions are not built.  " +
            "Try: gem pristine #{name} --version #{version}"
        end
        return false
      end

      super
    end
  end

  class Gem::BasicSpecification
    prepend Truffle::Patching::NoWarnIfBuildingCextDisabled
  end
end
