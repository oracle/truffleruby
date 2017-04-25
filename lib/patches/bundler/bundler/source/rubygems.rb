# frozen_string_literal: true

Truffle::Patching.require_original __FILE__

module Bundler
  class Source
    class Rubygems < Source
      def installed_specs
        @installed_specs ||= begin
          idx = Index.new
          have_bundler = false
          Bundler.rubygems.all_specs.reverse_each do |spec|
            next if spec.name == "bundler" && spec.version.to_s != VERSION
            have_bundler = true if spec.name == "bundler"
            spec.source = self
            # if Bundler.rubygems.spec_missing_extensions?(spec, false)
            #   Bundler.ui.debug "Source #{self} is ignoring #{spec} because it is missing extensions"
            #   next
            # end
            idx << spec
          end

          # Always have bundler locally
          unless have_bundler
            # We're running bundler directly from the source
            # so, let's create a fake gemspec for it (it's a path)
            # gemspec
            bundler = Gem::Specification.new do |s|
              s.name     = "bundler"
              s.version  = VERSION
              s.platform = Gem::Platform::RUBY
              s.source   = self
              s.authors  = ["bundler team"]
              s.loaded_from = File.expand_path("..", __FILE__)
            end
            idx << bundler
          end
          idx
        end
      end

      def cached_specs
        @cached_specs ||= begin
          idx = installed_specs.dup

          Dir["#{cache_path}/*.gem"].each do |gemfile|
            next if gemfile =~ /^bundler\-[\d\.]+?\.gem/
            s ||= Bundler.rubygems.spec_from_gem(gemfile)
            s.source = self
            # if Bundler.rubygems.spec_missing_extensions?(s, false)
            #   Bundler.ui.debug "Source #{self} is ignoring #{s} because it is missing extensions"
            #   next
            # end
            idx << s
          end
        end

        idx
      end
    end
  end
end
