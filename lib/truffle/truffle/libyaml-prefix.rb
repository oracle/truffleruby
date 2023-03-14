# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Set LIBYAML_PREFIX in ENV to find the libyaml headers

search_homebrew = -> homebrew {
  if prefix = "#{homebrew}/opt/libyaml" and Dir.exist?(prefix)
    prefix
  end
}

if Truffle::Platform.darwin? && !ENV['LIBYAML_PREFIX']
  default_homebrew_prefix = Truffle::System.host_cpu == 'aarch64' ? '/opt/homebrew' : '/usr/local'
  if prefix = search_homebrew.call(default_homebrew_prefix)
    # found
  else
    homebrew = `brew --prefix 2>/dev/null`.strip
    homebrew = nil unless $?.success? and !homebrew.empty? and Dir.exist?(homebrew)

    if homebrew and prefix = search_homebrew.call(homebrew)
      # found
    elsif Dir.exist?('/opt/local/include/libyaml') # MacPorts
      prefix = '/opt/local'
    end
  end

  if prefix
    ENV['LIBYAML_PREFIX'] = prefix
  else
    abort 'Could not find libyaml headers, install via Homebrew or MacPorts or set LIBYAML_PREFIX'
  end
end

if libyaml_prefix = ENV['LIBYAML_PREFIX']
  Truffle::Debug.log_config("Found libyaml in #{libyaml_prefix}")
end
