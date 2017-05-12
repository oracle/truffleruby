Truffle::Patching.require_original __FILE__

module Bundler
  class Fetcher
    class CompactIndex < Base
      private

      if Truffle::Boot.patching_openssl_enabled?
        def md5_available?
          false
        end
      end
    end
  end
end
