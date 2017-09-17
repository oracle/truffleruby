Truffle::Patching.require_original __FILE__

module Truffle::Patching::RakeExtensionTaskOverridePatterns
  def init(name = nil, gem_spec = nil)
    super
    @source_pattern = "*.{c,cc,cpp}"
    @compiled_pattern = "*.{bc,su}"
  end
end

module Rake
  class ExtensionTask < BaseExtensionTask
    prepend Truffle::Patching::RakeExtensionTaskOverridePatterns
  end
end
