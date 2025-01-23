# frozen_string_literal: true

# Copyright (c) 2018, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# From https://openssl-library.org/policies/releasestrat/index.html
# 3.0 is LTS, 3.1+ is non-LTS and 1.1 is EOL

# See the existing formula at https://github.com/Homebrew/homebrew-core/tree/master/Formula/o
search_homebrew = -> homebrew {
  if prefix = "#{homebrew}/opt/openssl@3.0" and Dir.exist?(prefix)
    prefix
  elsif prefix = "#{homebrew}/opt/openssl@3" and Dir.exist?(prefix)
    prefix
  elsif prefix = "#{homebrew}/opt/openssl@1.1" and Dir.exist?(prefix)
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

    # See https://ports.macports.org/search/?q=openssl&name=on for the list of MacPorts openssl ports
    if homebrew and prefix = search_homebrew.call(homebrew)
      # found
    elsif Dir.exist?('/opt/local/libexec/openssl3')
      prefix = '/opt/local/libexec/openssl3'
    elsif Dir.exist?('/opt/local/libexec/openssl11')
      prefix = '/opt/local/libexec/openssl11'
    elsif Dir.exist?('/opt/local/include/openssl') # symlinks, unknown version
      prefix = '/opt/local'
    end
  end

  if prefix
    # Set OPENSSL_PREFIX in ENV to find the OpenSSL headers
    ENV['OPENSSL_PREFIX'] = prefix
  else
    abort 'Could not find OpenSSL headers, install via Homebrew or MacPorts or set OPENSSL_PREFIX'
  end
end

if openssl_prefix = ENV['OPENSSL_PREFIX']
  Truffle::Debug.log_config("Found OpenSSL in #{openssl_prefix}")
end
