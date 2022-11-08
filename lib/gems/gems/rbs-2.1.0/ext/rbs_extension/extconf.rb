require 'mkmf'
$INCFLAGS << " -I$(top_srcdir)" if $extmk
create_makefile 'rbs_extension'

if defined?(::TruffleRuby) # Mark the gem extension as built
  require 'rubygems'
  require 'fileutils'
  spec = Gem::Specification.find_by_name('rbs')
  gem_build_complete_path = spec.gem_build_complete_path
  FileUtils.mkdir_p(File.dirname(gem_build_complete_path))
  FileUtils.touch(gem_build_complete_path)
end
