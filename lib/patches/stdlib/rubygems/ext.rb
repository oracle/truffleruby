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

    sulong_present = !!ENV['SULONG_HOME']
    @spec.extensions.each do |extension|
      break if @ran_rake
      # TruffleRuby: build C extensions conditionally
      if sulong_present
        build_extension extension, dest_path
      else
        puts 'SULONG_HOME is not set therefore skipping C extension build.'
      end
    end

    FileUtils.touch @spec.gem_build_complete_path if sulong_present
  end
end
