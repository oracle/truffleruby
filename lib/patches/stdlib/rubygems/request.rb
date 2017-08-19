Truffle::Patching.require_original __FILE__

if Truffle::Boot.patching_openssl_enabled?
  # TruffleRuby: Use curl when uri has https scheme

  require "truffle/https_downloader"

  module Truffle::Patching::GemRequestPerformHTTPSRequest
    def perform_request(request)
      if @uri.scheme == "https"
        Truffle::HTTPSDownloader.download(@uri)
      else
        super
      end
    end
  end

  class Gem::Request
    prepend Truffle::Patching::GemRequestPerformHTTPSRequest
  end
end
