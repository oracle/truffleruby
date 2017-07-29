Truffle::Patching.require_original __FILE__

if Truffle::Boot.patching_openssl_enabled?
  module Bundler
    class Fetcher
      class CompactIndex < Base
        private def md5_available?
          false
        end
      end
    end
  end
end
