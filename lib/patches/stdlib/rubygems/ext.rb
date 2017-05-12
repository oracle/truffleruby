Truffle::Patching.require_original __FILE__

# TruffleRuby: Ignore native extensions

class Gem::Ext::Builder
  def build_extensions
    return if @spec.extensions.empty?

    if @build_args.empty?
      say "Building native extensions.  This could take a while..."
    else
      say "Building native extensions with: '#{@build_args.join ' '}'"
      say "This could take a while..."
    end

    dest_path = @spec.extension_dir

    FileUtils.rm_f @spec.gem_build_complete_path

    @ran_rake = false # only run rake once

    @spec.extensions.each do |extension|
      break if @ran_rake
      # TruffleRuby: build C extensions conditionally
      puts "WORKAROUND: Skipping build extension:#{extension}, dest_path:#{dest_path}. " +
               'Support of C extensions is in development, set environment variable TRUFFLERUBY_CEXT_ENABLED to experiment.'
      build_extension extension, dest_path if ENV['TRUFFLERUBY_CEXT_ENABLED']
    end

    FileUtils.touch @spec.gem_build_complete_path if ENV['TRUFFLERUBY_CEXT_ENABLED']
  end
end
