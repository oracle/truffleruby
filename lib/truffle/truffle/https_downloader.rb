# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require 'net/http'

module Truffle
  module HTTPSDownloader
    HTTP_PROXY_HEADER = "HTTP/1.0 200 Connection established\r\n\r\n"

    class CurlResponse < Net::HTTPOK
      def body
        @body
      end
    end

    def self.download(uri)
      resp = CurlResponse.new('1.1', 200, 'OK')
      raise 'curl is missing' unless (`curl --help` rescue nil)
      resp_raw = `curl -i -s #{uri}`
      index_offset = resp_raw.start_with?(HTTP_PROXY_HEADER) ? HTTP_PROXY_HEADER.size : 0
      blank_line_idx = resp_raw.index("\r\n\r\n", index_offset)
      header = resp_raw[0...blank_line_idx]
      resp.body = resp_raw[(blank_line_idx+4)..-1]

      if m = /ETag: (\"[[:alnum:]]*\")/.match(header)
        resp['ETag'] = m[1]
      end
      resp
    end

    def self.download_to(uri, path)
      raise 'curl is missing' unless (`curl --help` rescue nil)
      system 'curl', '-s', uri, '-o', path
    end
  end
end
