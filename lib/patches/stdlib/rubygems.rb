Truffle::Patching.require_original __FILE__
Truffle::Patching.install_gem_activation_hook

# Because did_you_mean was required directly without RubyGems
if Truffle::Boot.get_option 'did_you_mean'
  begin
    gem 'did_you_mean'
  rescue LoadError => e
    Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
  end
end

# Add TruffleRuby rubygems hooks to install and uninstall executables from additional
# GraalVM bin directories (./bin, ./jre/bin)
unless RbConfig::CONFIG['extra_bindirs'].empty?
  require 'rubygems/extra_executables_installer'
  Gem::ExtraExecutablesInstaller.install_hooks_for RbConfig::CONFIG['extra_bindirs']
end

# Make sure we don't use foreign gem directories
require 'rubygems/gem_home_dirs_marker'
Gem::GemHomeDirsMarker.verify(Gem.path)
Gem::GemHomeDirsMarker.install_hook

