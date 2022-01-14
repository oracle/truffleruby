# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Set OPENSSL_PREFIX in ENV to find the OpenSSL headers

search_homebrew = -> homebrew {
  if prefix = "#{homebrew}/opt/openssl@1.1" and Dir.exist?(prefix)
    prefix
  elsif prefix = "#{homebrew}/opt/openssl" and Dir.exist?(prefix)
    prefix
  end
}

macOS = RUBY_PLATFORM.include?('darwin')

if macOS && !ENV['OPENSSL_PREFIX']
  if prefix = search_homebrew.call('/usr/local')
    # found
  else
    homebrew = `brew --prefix 2>/dev/null`.strip
    homebrew = nil unless $?.success? and !homebrew.empty? and Dir.exist?(homebrew)

    if homebrew and prefix = search_homebrew.call(homebrew)
      # found
    elsif Dir.exist?('/opt/local/libexec/openssl11') # MacPorts, prefer OpenSSL 1.1 as known to be compatible
      prefix = '/opt/local/libexec/openssl11'
    # MacPorts, try the generic version, too, but might not be compatible
    elsif Dir.exist?('/opt/local/include/openssl')
      prefix = '/opt/local'
    end
  end

  if prefix
    ENV['OPENSSL_PREFIX'] = prefix
  else
    abort 'Could not find OpenSSL headers, install via Homebrew or MacPorts or set OPENSSL_PREFIX'
  end
end

if openssl_prefix = ENV['OPENSSL_PREFIX']
  Truffle::Debug.log_config("Found OpenSSL in #{openssl_prefix}")

  # We need to set PKG_CONFIG_PATH too, see https://github.com/oracle/truffleruby/issues/1830
  # OpenSSL's extconf.rb calls the pkg_config() helper.
  ENV['PKG_CONFIG_PATH'] = ["#{openssl_prefix}/lib/pkgconfig", *ENV['PKG_CONFIG_PATH']].join(':')
end
