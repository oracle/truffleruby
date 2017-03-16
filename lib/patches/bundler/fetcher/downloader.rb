# TruffleRuby: Use curl in bundler downloader for https requests

require "openssl-stubs"

module Bundler
  class Fetcher
    class Downloader
      HTTP_PROXY_HEADER="HTTP/1.0 200 Connection established\r\n\r\n"
      class CurlResponse < Net::HTTPOK
        def body
          @body
        end
      end

      def fetch(uri, options = {}, counter = 0)
        raise HTTPError, "Too many redirects" if counter >= redirect_limit

        # TruffleRuby: start
        response = if uri.scheme == "https"
                     resp           = CurlResponse.new("1.1", 200, "OK")
                     resp_raw = if (`curl --help` rescue nil)
                                  `curl -i -s #{uri}`
                                else
                                  raise 'curl is missing'
                                end
                     index_offset   = resp_raw.start_with?(HTTP_PROXY_HEADER) ? HTTP_PROXY_HEADER.size : 0
                     blank_line_idx = resp_raw.index("\r\n\r\n", index_offset)
                     header         = resp_raw[0, blank_line_idx]
                     resp.body      = resp_raw[(blank_line_idx+4)..-1]
                     if m = /ETag: (\"[[:alnum:]]*\")/.match(header)
                       resp["ETag"] = m[1]
                     end
                     resp
                   else
                     response = request(uri, options)
                   end
        # TruffleRuby: end

        Bundler.ui.debug("HTTP #{response.code} #{response.message}")

        case response
        when Net::HTTPSuccess, Net::HTTPNotModified
          response
        when Net::HTTPRedirection
          new_uri = URI.parse(response["location"])
          if new_uri.host == uri.host
            new_uri.user     = uri.user
            new_uri.password = uri.password
          end
          fetch(new_uri, options, counter + 1)
        when Net::HTTPRequestEntityTooLarge
          raise FallbackError, response.body
        when Net::HTTPUnauthorized
          raise AuthenticationRequiredError, uri.host
        when Net::HTTPNotFound
          raise FallbackError, "Net::HTTPNotFound"
        else
          raise HTTPError, "#{response.class}#{": #{response.body}" unless response.body.empty?}"
        end
      end

    end
  end
end
