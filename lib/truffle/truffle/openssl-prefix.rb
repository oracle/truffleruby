# Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
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

if Truffle::Platform.darwin? && !ENV['OPENSSL_PREFIX']
  default_homebrew_prefix = Truffle::System.host_cpu == 'aarch64' ? '/opt/homebrew' : '/usr/local'
  if prefix = search_homebrew.call(default_homebrew_prefix)
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
end
