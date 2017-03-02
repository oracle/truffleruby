module Truffle::Patching
  TRUFFLE_PATCHES_DIRECTORY = "#{Truffle::Boot.ruby_home}/lib/patches"
  TRUFFLE_PATCHES           = Dir.glob("#{TRUFFLE_PATCHES_DIRECTORY}/**/*.rb").
      select { |path| File.file? path }.
      map { |path| [path[(TRUFFLE_PATCHES_DIRECTORY.size + 1)..-4], true] }.
      to_h
end

module Kernel

  private

  alias_method :require_without_truffle_patching, :require

  def require(path)
    required = require_without_truffle_patching path

    if required && Truffle::Patching::TRUFFLE_PATCHES[path]
      Truffle::System.log :PATCH, "applying #{path}"
      require_without_truffle_patching "#{Truffle::Patching::TRUFFLE_PATCHES_DIRECTORY}/#{path}.rb"
    end
    required
  end
end

class Module

  alias_method :autoload_without_truffle_patching, :autoload
  private :autoload_without_truffle_patching

  def autoload(const, path)
    if Truffle::Patching::TRUFFLE_PATCHES[path]
      require path
    else
      autoload_without_truffle_patching const, path
    end
  end
end
