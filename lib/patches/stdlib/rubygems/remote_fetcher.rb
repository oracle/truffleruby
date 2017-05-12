
Truffle::Patching.require_original __FILE__

# Hardcode DNS Resolution to rubygems.org for gem install

class Gem::RemoteFetcher
  def api_endpoint(uri)
    host = uri.host

    begin
      res = if host != "rubygems.org" && host != "api.rubygems.org"
              @dns.getresource "_rubygems._tcp.#{host}", Resolv::DNS::Resource::IN::SRV
            else
              nil
            end
    rescue Resolv::ResolvError => e
      verbose "Getting SRV record failed: #{e}"
      uri
    else
      target = if host != "rubygems.org" && host != "api.rubygems.org"
                 res.target.to_s.strip
               else
                 'api.rubygems.org'
               end

      if /\.#{Regexp.quote(host)}\z/ =~ target
        return URI.parse "#{uri.scheme}://#{target}#{uri.path}"
      end

      uri
    end
  end
end

# Use wget

class Gem::RemoteFetcher

  if Truffle::Boot.patching_openssl_enabled?
    def download(spec, source_uri, install_dir = Gem.dir)
      cache_dir =
          if Dir.pwd == install_dir then # see fetch_command
            install_dir
          elsif File.writable? install_dir then
            File.join install_dir, "cache"
          else
            File.join Gem.user_dir, "cache"
          end

      gem_file_name = File.basename spec.cache_file
      local_gem_path = File.join cache_dir, gem_file_name

      FileUtils.mkdir_p cache_dir rescue nil unless File.exist? cache_dir

      # Always escape URI's to deal with potential spaces and such
      # It should also be considered that source_uri may already be
      # a valid URI with escaped characters. e.g. "{DESede}" is encoded
      # as "%7BDESede%7D". If this is escaped again the percentage
      # symbols will be escaped.
      unless source_uri.is_a?(URI::Generic)
        begin
          source_uri = URI.parse(source_uri)
        rescue
          source_uri = URI.parse(URI.const_defined?(:DEFAULT_PARSER) ?
                                     URI::DEFAULT_PARSER.escape(source_uri.to_s) :
                                     URI.escape(source_uri.to_s))
        end
      end

      scheme = source_uri.scheme

      # URI.parse gets confused by MS Windows paths with forward slashes.
      scheme = nil if scheme =~ /^[a-z]$/i

      # REFACTOR: split this up and dispatch on scheme (eg download_http)
      # REFACTOR: be sure to clean up fake fetcher when you do this... cleaner
      case scheme
      when 'http', 'https', 's3' then
        unless File.exist? local_gem_path then
          begin
            verbose "Downloading gem #{gem_file_name}"

            remote_gem_path = source_uri + "gems/#{gem_file_name}"

            # TruffleRuby: start
            if (`wget --help` rescue nil)
              cmd = "wget -q #{remote_gem_path} -O #{local_gem_path}"
             `#{cmd}`
            else
              raise 'wget is missing'
            end
            # self.cache_update_path remote_gem_path, local_gem_path
            # TruffleRuby: end
          rescue Gem::RemoteFetcher::FetchError
            raise if spec.original_platform == spec.platform

            alternate_name = "#{spec.original_name}.gem"

            verbose "Failed, downloading gem #{alternate_name}"

            remote_gem_path = source_uri + "gems/#{alternate_name}"

            self.cache_update_path remote_gem_path, local_gem_path
          end
        end
      when 'file' then
        begin
          path = source_uri.path
          path = File.dirname(path) if File.extname(path) == '.gem'

          remote_gem_path = correct_for_windows_path(File.join(path, 'gems', gem_file_name))

          FileUtils.cp(remote_gem_path, local_gem_path)
        rescue Errno::EACCES
          local_gem_path = source_uri.to_s
        end

        verbose "Using local gem #{local_gem_path}"
      when nil then # TODO test for local overriding cache
        source_path = if Gem.win_platform? && source_uri.scheme &&
            !source_uri.path.include?(':') then
                        "#{source_uri.scheme}:#{source_uri.path}"
                      else
                        source_uri.path
                      end

        source_path = Gem::UriFormatter.new(source_path).unescape

        begin
          FileUtils.cp source_path, local_gem_path unless
              File.identical?(source_path, local_gem_path)
        rescue Errno::EACCES
          local_gem_path = source_uri.to_s
        end

        verbose "Using local gem #{local_gem_path}"
      else
        raise ArgumentError, "unsupported URI scheme #{source_uri.scheme}"
      end

      local_gem_path
    end
  end
end

