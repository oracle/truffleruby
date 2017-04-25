module Truffle::Patching
  extend self

  def patches
    @patches ||= begin
      require 'pathname'
      Pathname.glob(dir.join('*')).each_with_object({}) do |path, patches|
        patches[path.basename.to_s] = path if path.directory?
      end
    end
  end

  def log(name, path)
    Truffle::System.log :PATCH,
                        "patching '#{name}' by inserting directory '#{path}' in LOAD_PATH before the original paths"
  end

  def insert_patching_dir(name, *paths)
    path = Truffle::Patching.patches[name]
    if path
      insertion_point = paths.
          map { |gem_require_path| $LOAD_PATH.index gem_require_path }.
          min
      originals[name] = paths
      Truffle::Patching.log(name, path)
      $LOAD_PATH.insert insertion_point, path.to_s if $LOAD_PATH[insertion_point-1] != path.to_s
      true
    else
      false
    end
  end

  def require_original(file)
    file                           = Pathname(file)
    relative_file_path_to_patching = file.relative_path_from dir
    name                           = relative_file_path_to_patching.descend.first
    require_path                   = relative_file_path_to_patching.relative_path_from(name)

    original = originals[name.to_s].flat_map do |original_path|
      Pathname.glob(File.join(original_path, require_path))
    end.first

    require original
  end

  def install_gem_activation_hook
    Gem::Specification.class_eval do
      alias_method :activate_without_truffle_patching, :activate

      def activate
        result = activate_without_truffle_patching
        Truffle::Patching.insert_patching_dir name, *full_require_paths
        result
      end
    end
  end

  private

  def originals
    @originals ||= {}
  end

  def dir
    @dir ||= Pathname(Truffle::Boot.ruby_home).join('lib/patches')
  end

end
