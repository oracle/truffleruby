module Kernel
  # Take this alias name so RubyGems will reuse this copy
  # and skip the method below once RubyGems is loaded.
  alias :gem_original_require :require

  $truffle_loading_rubygems = false

  private def require(path)
    return gem_original_require(path) if $truffle_loading_rubygems

    begin
      gem_original_require(path)
    rescue LoadError
      $truffle_loading_rubygems = true
      require 'rubygems'
      require path
    end
  end

  private def gem(*args)
    require 'rubygems'
    gem(*args)
  end
end

class Object
  autoload :Gem, 'rubygems'
end
