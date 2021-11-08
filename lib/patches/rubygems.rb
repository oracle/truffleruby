# truffleruby_primitives: true

# Check if the Gem paths are correct as early as possible
unless Truffle::Boot.get_option 'testing-rubygems'
  Truffle::GemUtil.verify_gem_paths
end

require Primitive.get_original_require(__FILE__)

# Add TruffleRuby rubygems hooks to install and uninstall executables from additional
# GraalVM bin directories (./bin, ./jre/bin on JDK8)
unless RbConfig::CONFIG['extra_bindirs'].empty?
  require 'rubygems/extra_executables_installer'
  Gem::ExtraExecutablesInstaller.install_hooks_for RbConfig::CONFIG['extra_bindirs'].split(File::PATH_SEPARATOR)
end

# Make sure we don't use foreign gem directories
unless Truffle::Boot.get_option 'testing-rubygems'
  require 'rubygems/gem_dirs_verification'
  Gem::GemDirsVerification.install_hook
end

# We register did_you_mean only here because it was required directly
# without RubyGems in post-boot.rb.

# did_you_mean is only registered as a gem if --disable-gems was not passed, as
# --disable-gems implies --disable-did-you-mean on MRI, i.e.,
# MRI raises NameError for `ruby --disable-gems -e DidYouMean.formatter`.
if Truffle::Boot.get_option 'did-you-mean' and Truffle::Boot.get_option 'rubygems'
  begin
    gem 'did_you_mean'
  rescue LoadError
    # Ignore, this happens when GEM_HOME and GEM_PATH are set and do not include
    # the default gem home. In such a case, despite did_you_mean having been
    # loaded already during post-boot.rb, it is no longer possible to register
    # the gem with RubyGems. This happens for instance with 'bundle exec' after
    # `bundle install --deployment`.
    nil
  end
end
