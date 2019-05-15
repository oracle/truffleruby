require 'rake/baseextensiontask'

module Truffle::Patching::RakeBaseExtensionTaskOverrideBinaryExt
  def binary(platform = nil)
    if Gem.loaded_specs["rake-compiler"].version < Gem::Version.new('1.0')
      "#{File.basename(@name)}.#{RbConfig::CONFIG['DLEXT']}"
    else
      "#{@name}.#{RbConfig::CONFIG['DLEXT']}"
    end
  end
end

module Rake
  class BaseExtensionTask < TaskLib
    prepend Truffle::Patching::RakeBaseExtensionTaskOverrideBinaryExt
  end
end
