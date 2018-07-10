# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Set OPENSSL_PREFIX in ENV to find the OpenSSL headers

macOS = `uname`.chomp == 'Darwin'

if macOS && !ENV['OPENSSL_PREFIX']
  if Dir.exist?('/usr/local/opt/openssl') # Homebrew
    ENV['OPENSSL_PREFIX'] = '/usr/local/opt/openssl'
  elsif Dir.exist?('/opt/local/include/openssl') # MacPorts
    ENV['OPENSSL_PREFIX'] = '/opt/local'
  else
    abort 'Could not find OpenSSL headers, install via Homebrew or MacPorts or set OPENSSL_PREFIX'
  end
end
