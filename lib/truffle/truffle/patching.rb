require 'pathname'

module Truffle::Patching
  DIR     = Pathname(Truffle::Boot.ruby_home).join("lib/patches")
  # Allowed operations are: :before, :after, :instead, :ignore
  PATCHES = { "bundler"                                 => :before,
              "bundler/version"                         => :after,
              "bundler/cli/exec"                        => :after,
              "bundler/compact_index_client/updater"    => :after,
              "bundler/current_ruby"                    => :after,
              "bundler/dependency"                      => :after,
              "bundler/fetcher/downloader"              => :after,
              "bundler/fetcher/compact_index"           => :after,
              "bundler/ruby_version"                    => :after,
              "bundler/source/rubygems"                 => :after,

              "rubygems"                                => :before,
              "rubygems/ext"                            => :after,
              "rubygems/package"                        => :after,
              "rubygems/remote_fetcher"                 => :after,
              "rubygems/request"                        => :after,
              "rubygems/request_set/gem_dependency_api" => :after,

              "rspec/support"                           => :after,

              "unf/normalizer_cruby"                    => :before,
  }

  PATCHES.each do |file, _|
    unless DIR.join(file).sub_ext('.rb').exist?
      raise "file #{file} is missing in #{DIR}"
    end
  end

  Pathname.glob(DIR.join("**/*.rb")) do |file|
    relative_path = file.relative_path_from(DIR).sub_ext("")
    unless PATCHES.key? relative_path.to_s
      raise "file #{relative_path} is not declared in PATCHES"
    end
  end

  def self.find_absolute_path(relative_path)
    $LOAD_PATH.each do |base|
      candidate = Pathname(base).join(relative_path).sub_ext(".rb")
      return candidate if candidate.exist?
    end
    raise "absolute path for #{relative_path} was not found in #{$LOAD_PATH}"
  end

  def self.find_relative_path(absolute_path)
    return nil unless absolute_path.exist?

    $LOAD_PATH.each do |base|
      if absolute_path.to_s.start_with?(base)
        return absolute_path.relative_path_from(Pathname(base)).sub_ext("")
      end
    end

    patch_candidate = PATCHES.keys.find { |c| absolute_path.sub_ext("").to_s.end_with? c }
    if !patch_candidate.nil? && !(absolute_path.each_filename.to_a & ['gems', 'lib']).empty?
      Truffle::System.log :PATCH, <<-TXT
The file '#{absolute_path}' appears to be from a gem whose lib is not in $LOAD_PATH.
There is also a potential patch available '#{patch_candidate}' for the file.
No patches are applied. Please add the gem's lib to $LOAD_PATH first to fix.
      TXT
    end

    nil
  end

  def self.required?(relative_path)
    $LOADED_FEATURES.include? find_absolute_path(relative_path).to_s
  end
end

module Kernel

  private

  alias_method :require_without_truffle_patching, :gem_original_require

  def gem_original_require(path)
    path           = path.is_a?(Pathname) ? path : Pathname(Rubinius::Type.coerce_to(path, String, :to_str))
    path_no_suffix = path.sub_ext ""
    relative_path  = if path_no_suffix.absolute?
                       Truffle::Patching.find_relative_path path_no_suffix.sub_ext('.rb')
                     else
                       path_no_suffix
                     end

    operation = Truffle::Patching::PATCHES[relative_path.to_s]

    # not patched requiring normally
    return require_without_truffle_patching path if operation.nil?

    # apply patch based on operation
    log                 = -> { Truffle::System.log :PATCH, "#{operation} original require '#{path}'" }
    absolute_patch_path = Truffle::Patching::DIR.join(path_no_suffix).sub_ext(".rb")
    case operation
    when :after
      require_without_truffle_patching(path.to_s).tap do |required|
        if required
          log.call
          require_without_truffle_patching absolute_patch_path.to_s
        end
      end
    when :before
      if Truffle::Patching.required? relative_path
        result = require_without_truffle_patching path.to_s
        raise 'should be already loaded' if result
        result
      else
        log.call
        require_without_truffle_patching absolute_patch_path.to_s
        require_without_truffle_patching path.to_s
      end
    when :instead
      log.call
      require_without_truffle_patching absolute_patch_path.to_s
    when :ignored
      require_without_truffle_patching path.to_s
    else
      raise "unrecognized operation #{operation} for #{relative_path}"
    end
  end
end

class Module

  alias_method :autoload_without_truffle_patching, :autoload
  private :autoload_without_truffle_patching

  def autoload(const, path)
    if Truffle::Patching::PATCHES[path]
      require path
    else
      autoload_without_truffle_patching const, path
    end
  end
end
