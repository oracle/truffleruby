# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Set OPENSSL_PREFIX in ENV to find the OpenSSL headers

require 'rbconfig'

macOS = RbConfig::CONFIG['host_os'].include?('darwin')

if macOS && !ENV['OPENSSL_PREFIX']
  homebrew_prefix = `brew --prefix openssl 2>/dev/null`.chomp
  if $?.success? and Dir.exist?(homebrew_prefix) # Homebrew
    ENV['OPENSSL_PREFIX'] = homebrew_prefix
  elsif Dir.exist?('/opt/local/include/openssl') # MacPorts
    ENV['OPENSSL_PREFIX'] = '/opt/local'
  else
    abort 'Could not find OpenSSL headers, install via Homebrew or MacPorts or set OPENSSL_PREFIX'
  end
end
