Truffle::Patching.require_original __FILE__

if Truffle::Boot.patching_openssl_enabled?
  # TruffleRuby: skip some checksum/digest verification

  class Gem::Package
    def verify
      @files = []
      @spec  = nil

      @gem.with_read_io do |io|
        Gem::Package::TarReader.new io do |reader|
          read_checksums reader

          verify_files reader
        end
      end

      # TruffleRuby:disable
      # verify_checksums @digests, @checksums

      @security_policy.verify_signatures @spec, @digests, @signatures if @security_policy

      true
    rescue Gem::Security::Exception
      @spec  = nil
      @files = []
      raise
    rescue Errno::ENOENT => e
      raise Gem::Package::FormatError.new e.message
    rescue Gem::Package::TarInvalidError => e
      raise Gem::Package::FormatError.new e.message, @gem
    end
  end
end
