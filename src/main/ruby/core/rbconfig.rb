# Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
#
# Copyright (C) 1993-2013 Yukihiro Matsumoto. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
# OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
#  RbConfig.expand method imported from MRI sources
#

module RbConfig

  host_os  = Truffle::System.host_os
  host_cpu = Truffle::System.host_cpu

  ruby_install_name = 'truffleruby'

  ruby_api_version = RUBY_VERSION.dup
  raise ruby_api_version unless ruby_api_version[-2] == '.'
  ruby_api_version[-1] = '0'

  ruby_base_name = 'ruby'
  ruby_version   = ruby_api_version

  arch         = "#{host_cpu}-#{host_os}"
  cppflags     = ''
  libs         = ''

  CONFIG = {
      'arch'              => arch,
      'configure_args'    => ' ',
      'ARCH_FLAG'         => '',
      'CPPFLAGS'          => cppflags,
      'LDFLAGS'           => '',
      'DLDFLAGS'          => '',
      'DLEXT'             => 'su',
      'NATIVE_DLEXT'      => RUBY_PLATFORM.include?('darwin') ? 'dylib' : 'so',
      'host_os'           => host_os,
      'host_cpu'          => host_cpu,
      'LIBEXT'            => 'c',
      'OBJEXT'            => 'bc',
      'exeext'            => '',
      'EXEEXT'            => '',
      'EXECUTABLE_EXTS'   => '',
      'includedir'        => '',
      'LIBS'              => libs,
      'DLDLIBS'           => '',
      'LIBRUBYARG_STATIC' => '',
      'LIBRUBYARG_SHARED' => '',
      'libdirname'        => 'libdir',
      'LIBRUBY'           => '',
      'LIBRUBY_A'         => '',
      'LIBRUBYARG'        => '',
      'NULLCMD'           => ':',
      'RM'                => 'rm -f',
      'prefix'            => '',
      'ruby_install_name' => ruby_install_name,
      'RUBY_INSTALL_NAME' => ruby_install_name,
      'ruby_version'      => ruby_api_version,
      'RUBY_BASE_NAME'    => ruby_base_name,
      'target_cpu'        => host_cpu,
  }

  MAKEFILE_CONFIG = CONFIG.dup

  expanded = CONFIG
  mkconfig = MAKEFILE_CONFIG

  expanded['RUBY_SO_NAME'] = ruby_base_name
  mkconfig['RUBY_SO_NAME'] = '$(RUBY_BASE_NAME)'

  ruby_home = Truffle::Boot.ruby_home

  if ruby_home
    prefix = ruby_home

    common = {
      'prefix' => prefix,
      'bindir' => "#{prefix}/bin",
      'hdrdir' => "#{prefix}/lib/cext",
      'rubyhdrdir' => "#{prefix}/lib/cext",
      'rubyarchhdrdir' => "#{prefix}/lib/cext",
    }
    expanded.merge!(common)
    mkconfig.merge!(common)

    exec_prefix = \
    expanded['exec_prefix'] = prefix
    mkconfig['exec_prefix'] = '$(prefix)'
    libdir = \
    expanded['libdir'] = "#{exec_prefix}/lib"
    mkconfig['libdir'] = '$(exec_prefix)/lib'
    rubylibprefix = \
    expanded['rubylibprefix'] = "#{libdir}/#{ruby_base_name}"
    mkconfig['rubylibprefix'] = '$(libdir)/$(RUBY_BASE_NAME)'
    rubylibdir = \
    expanded['rubylibdir'] = "#{rubylibprefix}/#{ruby_version}"
    mkconfig['rubylibdir'] = '$(rubylibprefix)/$(ruby_version)'
    rubyarchdir = \
    expanded['rubyarchdir'] = "#{rubylibdir}/#{arch}"
    mkconfig['rubyarchdir'] = '$(rubylibdir)/$(arch)'
    archdir = \
    expanded['archdir'] = rubyarchdir
    mkconfig['archdir'] = '$(rubyarchdir)'
    sitearch = \
    expanded['sitearch'] = arch
    mkconfig['sitearch'] = '$(arch)'
    sitedir = \
    expanded['sitedir'] = "#{rubylibprefix}/site_ruby"
    mkconfig['sitedir'] = '$(rubylibprefix)/site_ruby'
    sitelibdir = \
    expanded['sitelibdir'] = "#{sitedir}/#{ruby_version}"
    mkconfig['sitelibdir'] = '$(sitedir)/$(ruby_version)'
    expanded['sitearchdir'] = "#{sitelibdir}/#{sitearch}"
    mkconfig['sitearchdir'] = '$(sitelibdir)/$(sitearch)'
    expanded['topdir'] = archdir
    mkconfig['topdir'] = '$(archdir)'
  end

  clang          = 'clang'
  opt            = 'opt'
  opt_passes     = ['-always-inline', '-mem2reg', '-constprop'].join(' ')
  cc             = clang
  cpp            = cc
  cflags         = ['-Werror=implicit-function-declaration',  # To make missing C ext functions very clear
                    '-Wno-unknown-warning-option',            # If we're on an earlier version of clang without a warning option, ignore it
                    '-Wno-int-conversion',                    # MRI has VALUE defined as long while we have it as void*
                    '-Wno-int-to-pointer-cast',               # Same as above
                    '-Wno-macro-redefined',                   # We redefine __DARWIN_ALIAS_C
                    '-Wno-unused-value',                      # RB_GC_GUARD leaves
                    '-Wno-incompatible-pointer-types',        # We define VALUE as void* rather than uint32_t
                    '-Wno-expansion-to-defined',              # The openssl C extension uses macros expanding to defined
                    '-c', '-emit-llvm'].join(' ')
  # We do not have a launcher if we are embedded with the polyglot API
  ruby_launcher = Truffle::Boot.ruby_launcher
  ruby_launcher ||= "#{expanded['bindir']}/#{ruby_install_name}" if ruby_home
  ruby_launcher ||= ruby_install_name

  common = {
    'CC' => cc,
    'CPP' => cpp,
    'CFLAGS' => cflags,
  }
  expanded.merge!(common)
  mkconfig.merge!(common)

  expanded['COMPILE_C'] = "ruby #{libdir}/cext/preprocess.rb < $< | #{cc} $(INCFLAGS) #{cppflags} #{cflags} $(COUTFLAG) -xc - -o $@ && #{opt} #{opt_passes} $@ -o $@",
  mkconfig['COMPILE_C'] = "ruby #{libdir}/cext/preprocess.rb < $< | $(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG) -xc - -o $@ && #{opt} #{opt_passes} $@ -o $@",
  expanded['LINK_SO'] = "#{ruby_launcher} -e Truffle::CExt::Linker.main -- -o $@ $(OBJS) #{libs}"
  mkconfig['LINK_SO'] = "#{ruby_launcher} -e Truffle::CExt::Linker.main -- -o $@ $(OBJS) $(LIBS)"
  expanded['TRY_LINK'] = "#{clang} -o conftest $(src) $(INCFLAGS) #{cflags} #{libs}"
  mkconfig['TRY_LINK'] = "#{clang} -o conftest $(src) $(INCFLAGS) $(CFLAGS) $(LIBS)"

  def self.ruby
    Truffle::Boot.ruby_launcher or
        raise "we can't find the TruffleRuby launcher - set -Xlauncher=, or your launcher should be doing this for you"
  end

  def RbConfig.expand(val, config = CONFIG)
    newval = val.gsub(/\$\$|\$\(([^()]+)\)|\$\{([^{}]+)\}/) {
      var = $&
      if !(v = $1 || $2)
        '$'
      elsif key = config[v = v[/\A[^:]+(?=(?::(.*?)=(.*))?\z)/]]
        pat, sub  = $1, $2
        config[v] = false
        config[v] = RbConfig.expand(key, config)
        key       = key.gsub(/#{Regexp.quote(pat)}(?=\s|\z)/n) {sub} if pat
        key
      else
        var
      end
    }
    val.replace(newval) unless newval == val
    val
  end

end

CROSS_COMPILING = nil
