require 'singleton'

module RBS
  module Collection
    module Sources
      # signatures that are bundled in rbs gem under the stdlib/ directory
      class Stdlib
        include Singleton

        def has?(config_entry)
          gem_dir(config_entry).exist?
        end

        def versions(config_entry)
          gem_dir(config_entry).glob('*/').map { |path| path.basename.to_s }
        end

        def install(dest:, config_entry:, stdout:)
          # Do nothing because stdlib RBS is available by default
          name = config_entry['name']
          version = config_entry['version'] or raise
          from = gem_dir(config_entry) / version
          stdout.puts "Using #{name}:#{version} (#{from})"
        end

        def manifest_of(config_entry)
          version = config_entry['version'] or raise
          manifest_path = gem_dir(config_entry).join(version, 'manifest.yaml')
          YAML.safe_load(manifest_path.read) if manifest_path.exist?
        end

        def to_lockfile
          {
            'type' => 'stdlib',
          }
        end

        private def gem_dir(config_entry)
          Repository::DEFAULT_STDLIB_ROOT.join(config_entry['name'])
        end
      end
    end
  end
end
