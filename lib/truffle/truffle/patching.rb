module Truffle::Patching
  DIR     = "#{Truffle::Boot.ruby_home}/lib/patches"
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
    unless File.exist? File.join(DIR, file) + '.rb'
      raise "file #{file} is missing in #{DIR}"
    end
  end

  Dir["#{DIR}/**/*.rb"].each do |file|
    relative_path = file[(DIR.size + 1)..-4]
    unless PATCHES.key? relative_path
      raise "file #{relative_path} is not declared in PATCHES"
    end
  end

  def self.find_absolute_path(relative_path)
    $LOAD_PATH.each do |base|
      candidate = File.join(base, relative_path) + '.rb'
      return candidate if File.exist? candidate
    end
    raise "absolute path for #{relative_path} was not found in #{$LOAD_PATH}"
  end

  def self.find_relative_path(absolute_path)
    return nil unless File.exist? absolute_path

    $LOAD_PATH.each do |base|
      if absolute_path.start_with?(base)
        return File.basename absolute_path[(base.size)..-1], '.*'
      end
    end

    nil
  end

  def self.required?(relative_path)
    $LOADED_FEATURES.include? find_absolute_path relative_path
  end
end

module Kernel

  private

  alias_method :require_without_truffle_patching, :gem_original_require

  def gem_original_require(path)
    path           = Rubinius::Type.coerce_to path, String, :to_str
    extname        = File.extname path
    path_no_suffix = path[0..(-1-extname.size)]
    relative_path  = if path_no_suffix.start_with?('/')
                       Truffle::Patching.find_relative_path path_no_suffix + '.rb'
                     else
                       path_no_suffix
                     end

    operation = Truffle::Patching::PATCHES[relative_path]

    # not patched requiring normally
    return require_without_truffle_patching path if operation.nil?

    # apply patch based on operation
    log = -> { Truffle::System.log :PATCH, "#{operation} original require '#{path}'" }
    case operation
    when :after
      require_without_truffle_patching(path).tap do |required|
        if required
          log.call
          require_without_truffle_patching "#{Truffle::Patching::DIR}/#{path_no_suffix}.rb"
        end
      end
    when :before
      if Truffle::Patching.required? relative_path
        result = require_without_truffle_patching path
        raise 'should be already loaded' if result
        result
      else
        log.call
        require_without_truffle_patching "#{Truffle::Patching::DIR}/#{path_no_suffix}.rb"
        require_without_truffle_patching path
      end
    when :instead
      log.call
      require_without_truffle_patching "#{Truffle::Patching::DIR}/#{path_no_suffix}.rb"
    when :ignored
      require_without_truffle_patching path
    else
      raise "unrecognised operation #{operation} for #{relative_path}"
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
