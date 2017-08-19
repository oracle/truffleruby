Truffle::Patching.require_original __FILE__

if Truffle::Boot.patching_openssl_enabled?
  # TruffleRuby: Use curl in bundler downloader for https requests

  require "truffle/https_downloader"

  module Truffle::Patching::BundlerFetcherDownloaderHTTPS
    def request(uri, options)
      if uri.scheme == "https"
        Truffle::HTTPSDownloader.download(uri)
      else
        super
      end
    end
  end

  class Bundler::Fetcher::Downloader
    prepend Truffle::Patching::BundlerFetcherDownloaderHTTPS
  end
end
