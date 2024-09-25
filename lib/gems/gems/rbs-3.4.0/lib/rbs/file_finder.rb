# frozen_string_literal: true

module RBS
  module FileFinder
    module_function

    def self.each_file(path, immediate:, skip_hidden:, &block)
      return enum_for((__method__ or raise), path, immediate: immediate, skip_hidden: skip_hidden) unless block

      case
      when path.file?
        if path.extname == ".rbs" || immediate
          yield path
        end

      when path.directory?
        if path.basename.to_s.start_with?("_")
          if skip_hidden
            unless immediate
              return
            end
          end
        end

        path.children.sort.each do |child|
          each_file(child, immediate: false, skip_hidden: skip_hidden, &block)
        end
      end
    end
  end
end
