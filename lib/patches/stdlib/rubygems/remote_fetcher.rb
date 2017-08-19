
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
