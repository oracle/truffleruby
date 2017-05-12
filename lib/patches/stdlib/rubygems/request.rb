Truffle::Patching.require_original __FILE__

# TruffleRuby: Use curl when uri has https scheme

class Gem::Request
  class CurlResponse < Net::HTTPOK
    def body
      @body
    end
  end

  HTTP_PROXY_HEADER="HTTP/1.0 200 Connection established\r\n\r\n"

  if Truffle::Boot.patching_openssl_enabled?
    def fetch
      request = @request_class.new @uri.request_uri

      unless @uri.nil? || @uri.user.nil? || @uri.user.empty? then
        request.basic_auth Gem::UriFormatter.new(@uri.user).unescape,
                           Gem::UriFormatter.new(@uri.password).unescape
      end

      request.add_field 'User-Agent', @user_agent
      request.add_field 'Connection', 'keep-alive'
      request.add_field 'Keep-Alive', '30'

      if @last_modified then
        request.add_field 'If-Modified-Since', @last_modified.httpdate
      end

      yield request if block_given?

      # TruffleRuby: start
      if @uri.scheme == "https"
        resp = CurlResponse.new("1.1", 200, "OK")
        resp_raw = if (`curl --help` rescue nil)
                     `curl -i -s #{@uri}`
                   else
                     raise 'curl is missing'
                   end
        index_offset = resp_raw.start_with?(HTTP_PROXY_HEADER) ? HTTP_PROXY_HEADER.size : 0
        blank_line_idx = resp_raw.index("\r\n\r\n", index_offset)
        header = resp_raw[0, blank_line_idx]
        resp.body = resp_raw[(blank_line_idx+4)..-1]
        if m = /ETag: (\"[[:alnum:]]*\")/.match(header)
          resp["ETag"] = m[1]
        end
        resp
      else
        perform_request request
      end
      # TruffleRuby: end
    end
  end
end
