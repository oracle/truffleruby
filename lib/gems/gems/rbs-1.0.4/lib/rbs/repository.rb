module RBS
  class Repository
    DEFAULT_STDLIB_ROOT = Pathname(_ = __dir__) + "../../stdlib"

    class GemRBS
      attr_reader :name
      attr_reader :paths

      def initialize(name:)
        @name = name
        @paths = []
        @versions = nil
      end

      def versions
        load! unless @versions
        @versions or raise
      end

      def load!
        @versions = {}
        versions = @versions or raise

        paths.each do |gem_path|
          gem_path.each_child(false) do |child|
            next unless Gem::Version.correct?(child.to_s)

            if version = Gem::Version.create(child.to_s)
              unless version.prerelease?
                path = gem_path + child

                if prev = versions[version]
                  RBS.logger.info { "Overwriting gem RBS in repository: gem=#{name}, prev_path=#{prev.path}, new_path=#{path}" }
                end

                versions[version] = VersionPath.new(gem: self, version: version, path: path)
              end
            end
          end
        end
      end

      def version_names
        versions.keys.sort_by(&:version)
      end

      def oldest_version
        oldest = version_names.first or raise
        versions[oldest] or raise
      end

      def latest_version
        latest = version_names.last or raise
        versions[latest] or raise
      end

      def find_best_version(version)
        return latest_version unless version

        if v = version_names.reverse.bsearch {|v| v <= version ? true : false }
          versions[v]
        else
          oldest_version
        end
      end

      def empty?
        versions.empty?
      end
    end

    VersionPath = _ = Struct.new(:gem, :version, :path, keyword_init: true)

    attr_reader :dirs
    attr_reader :gems

    def initialize(no_stdlib: false)
      @dirs = []
      @gems = {}

      unless no_stdlib
        add(DEFAULT_STDLIB_ROOT)
      end
    end

    def self.default
      new().tap do |repo|
        repo.add(DEFAULT_STDLIB_ROOT)
      end
    end

    def add(dir)
      dirs << dir

      dir.each_child(false) do |child|
        gem_name = child.to_s
        gem_rbs = (gems[gem_name] ||= GemRBS.new(name: gem_name))
        gem_rbs.paths << dir + child
      end
    end

    def lookup(gem, version)
      _, set = lookup_path(gem, version)
      set&.path
    end

    def lookup_path(gem, version)
      if gem_rbs = gems[gem]
        unless gem_rbs.empty?
          set = if v = Gem::Version.create(version)&.release
            gem_rbs.find_best_version(v)
          else
            gem_rbs.latest_version
          end

          [gem_rbs, set]
        end
      end
    end
  end
end
