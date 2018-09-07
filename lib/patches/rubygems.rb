require 'rubygems'

# Add TruffleRuby rubygems hooks to install and uninstall executables from additional
# GraalVM bin directories (./bin, ./jre/bin)
unless RbConfig::CONFIG['extra_bindirs'].empty?
  require 'rubygems/extra_executables_installer'
  Gem::ExtraExecutablesInstaller.install_hooks_for RbConfig::CONFIG['extra_bindirs'].split(File::PATH_SEPARATOR)
end

# Make sure we don't use foreign gem directories
require 'rubygems/gem_dirs_verification'
Gem::GemDirsVerification.verify(Gem.path)
Gem::GemDirsVerification.install_hook

# Because did_you_mean was required directly without RubyGems.
# did_you_mean is only required if --disable-gems was not passed.
if Truffle::Boot.get_option 'did_you_mean' and Truffle::Boot.get_option 'rubygems'
  begin
    gem 'did_you_mean'
  rescue LoadError => e
    Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
  end
end
