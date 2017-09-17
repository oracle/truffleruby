Truffle::Patching.require_original __FILE__

module Truffle::Patching::RakeBaseExtensionTaskOverrideBinaryExt
  def binary(platform = nil)
    if Gem.loaded_specs["rake-compiler"].version < Gem::Version.new('1.0')
      "#{File.basename(@name)}.su"
    else
      "#{@name}.su"
    end
  end
end

module Rake
  class BaseExtensionTask < TaskLib
    prepend Truffle::Patching::RakeBaseExtensionTaskOverrideBinaryExt
  end
end
