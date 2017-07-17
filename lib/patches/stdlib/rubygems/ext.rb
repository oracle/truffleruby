Truffle::Patching.require_original __FILE__

# TruffleRuby: build C extensions conditionally

class Gem::Ext::Builder
  def build_extensions
    return if @spec.extensions.empty?

    if ENV['TRUFFLERUBY_CEXT_ENABLED']
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

        build_extension extension, dest_path
      end

      FileUtils.touch @spec.gem_build_complete_path
    else
      puts "WORKAROUND: Not building native extensions for #{@spec.name}.\n" +
           'Support of C extensions is in development, set TRUFFLERUBY_CEXT_ENABLED=true to experiment.'
    end
  end
end
