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
  
  host_os = Truffle::System.host_os
  host_cpu = Truffle::System.host_cpu

  ruby_install_name = 'truffleruby'

  ruby_api_version = RUBY_VERSION.dup
  raise ruby_api_version unless ruby_api_version[-2] == '.'
  ruby_api_version[-1] = '0'

  ruby_base_name = 'ruby'
  ruby_version = ruby_api_version

  arch = "#{host_cpu}-#{host_os}"
  cppflags = ''
  libs = ''
  ruby_so_name = ruby_base_name

  MAKEFILE_CONFIG = {
      'arch' => arch,
      'configure_args' => ' ',
      'ARCH_FLAG' => '',
      'CPPFLAGS' => cppflags,
      'LDFLAGS' => '',
      'DLDFLAGS' => '',
      'DLEXT' => 'su',
      'host_os' => host_os,
      'host_cpu' => host_cpu,
      'LIBEXT' => 'c',
      'OBJEXT' => 'bc',
      'exeext' => '',
      'EXEEXT' => '',
      'EXECUTABLE_EXTS' => '',
      'includedir' => '',
      'LIBS' => libs,
      'DLDLIBS' => '',
      'LIBRUBYARG_STATIC' => '',
      'LIBRUBYARG_SHARED' => '',
      'libdirname' => 'libdir',
      'LIBRUBY' => '',
      'LIBRUBY_A' => '',
      'LIBRUBYARG' => '',
      'prefix' => '',
      'ruby_install_name' => ruby_install_name,
      'RUBY_INSTALL_NAME' => ruby_install_name,
      'ruby_version' => ruby_version,
      'RUBY_BASE_NAME' => ruby_base_name,
      'RUBY_SO_NAME' => '$(RUBY_BASE_NAME)',
      'target_cpu' => host_cpu
  }


  CONFIG = {
      'arch' => arch,
      'configure_args' => ' ',
      'ARCH_FLAG' => '',
      'CPPFLAGS' => cppflags,
      'LDFLAGS' => '',
      'DLDFLAGS' => '',
      'DLEXT' => 'su',
      'host_os' => host_os,
      'host_cpu' => host_cpu,
      'LIBEXT' => 'c',
      'OBJEXT' => 'bc',
      'exeext' => '',
      'EXEEXT' => '',
      'EXECUTABLE_EXTS' => '',
      'includedir' => '',
      'LIBS' => libs,
      'DLDLIBS' => '',
      'LIBRUBYARG_STATIC' => '',
      'LIBRUBYARG_SHARED' => '',
      'libdirname' => 'libdir',
      'LIBRUBY' => '',
      'LIBRUBY_A' => '',
      'LIBRUBYARG' => '',
      'prefix' => '',
      'ruby_install_name' => ruby_install_name,
      'RUBY_INSTALL_NAME' => ruby_install_name,
      'ruby_version' => ruby_api_version,
      'RUBY_BASE_NAME' => ruby_base_name,
      'RUBY_SO_NAME' => ruby_so_name,
      'target_cpu' => host_cpu
  }

  ruby_home = Truffle::Boot.ruby_home

  if ruby_home
    libdir = "#{ruby_home}/lib"
    bindir = "#{libdir}/bin"
    prefix = ruby_home
    exec_prefix = prefix
    libdir =  "#{exec_prefix}/lib"
    rubylibprefix = "#{libdir}/#{ruby_base_name}"
    rubylibdir = "#{rubylibprefix}/#{ruby_version}"
    rubyarchdir = "#{rubylibdir}/#{arch}"
    archdir = rubyarchdir
    sitearch = arch
    sitedir = "#{rubylibprefix}/site_ruby"
    sitelibdir = "#{sitedir}/#{ruby_version}"
    sitearchdir = "#{sitelibdir}/#{sitearch}"
    topdir = archdir

    CONFIG.merge!({
          'prefix' => prefix,
          'exec_prefix' => exec_prefix,
          'libdir' => libdir,
          'rubylibprefix' => rubylibprefix,
          'rubylibdir' => rubylibdir,
          'rubyarchdir' => rubyarchdir,
          'archdir' => archdir,
          'bindir' => bindir,
          'hdrdir' => "#{ruby_home}/lib/cext",
          'sitearch' => sitearch,
          'sitedir' => sitedir,
          'sitelibdir' => sitelibdir,
          'sitearchdir' => sitearchdir,
          'rubyhdrdir' => "#{libdir}/cext",
          'topdir' => topdir,
          'rubyarchhdrdir' => "#{libdir}/cext",
    })
    MAKEFILE_CONFIG.merge!({
         'prefix' => ruby_home,
         'exec_prefix' => '$(prefix)',
         'libdir' => '$(exec_prefix)/lib',
         'rubylibprefix' => '$(libdir)/$(RUBY_BASE_NAME)',
         'rubylibdir' => '$(rubylibprefix)/$(ruby_version)',
         'rubyarchdir' => '$(rubylibdir)/$(arch)',
         'archdir' => '$(rubyarchdir)',
         'bindir' => bindir,
         'hdrdir' => "#{ruby_home}/lib/cext",
         'sitearch' => '$(arch)',
         'sitedir' => '$(rubylibprefix)/site_ruby',
         'sitelibdir' => '$(sitedir)/$(ruby_version)',
         'sitearchdir' => '$(sitelibdir)/$(sitearch)',
         'rubyhdrdir' => "#{libdir}/cext",
         'topdir' => '$(archdir)',
         'rubyarchhdrdir' => "#{libdir}/cext",
      })
  end

  if Truffle::Safe.memory_safe? && Truffle::Safe.processes_safe?
    clang = ENV['JT_CLANG'] || 'clang'
    opt = ENV['JT_OPT'] || 'opt'
    cc = "#{clang} -I#{ENV['SULONG_HOME']}/include"
    cpp = cc
    cflags = '-Werror=implicit-function-declaration -c -emit-llvm'

    CONFIG.merge!({
        'CC' => cc,
        'CPP' => cpp,
        'COMPILE_C' => "#{cc} $(INCFLAGS) #{cppflags} #{cflags} $(COUTFLAG)$< -o $@ && #{opt} -always-inline -mem2reg $@ -o $@",
        'CFLAGS' => cflags,
        'LINK_SO' => "mx -v -p #{ENV['SULONG_HOME']} su-link -o $@ $(OBJS) #{libs}",
        'TRY_LINK' => "#{clang} $(src) $(INCFLAGS) #{cflags} -I#{ENV['SULONG_HOME']}/include #{libs}"
    })
    MAKEFILE_CONFIG.merge!({
        'CC' => cc,
        'CPP' => cpp,
        'COMPILE_C' => "$(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$< -o $@ && #{opt} -always-inline -mem2reg $@ -o $@",
        'CFLAGS' => cflags,
        'LINK_SO' => "mx -v -p #{ENV['SULONG_HOME']} su-link -o $@ $(OBJS) $(LIBS)",
        'TRY_LINK' => "#{clang} $(src) $(INCFLAGS) $(CFLAGS) -I#{ENV['SULONG_HOME']}/include $(LIBS)"
    })
  end

  def self.ruby
    ruby = Truffle::Boot.ruby_launcher
    raise "we can't find the TruffleRuby launcher - set -Xlauncher=, or your launcher should be doing this for you" unless ruby
    ruby
  end

  def RbConfig.expand(val, config = CONFIG)
    newval = val.gsub(/\$\$|\$\(([^()]+)\)|\$\{([^{}]+)\}/) {
      var = $&
      if !(v = $1 || $2)
        '$'
      elsif key = config[v = v[/\A[^:]+(?=(?::(.*?)=(.*))?\z)/]]
        pat, sub = $1, $2
        config[v] = false
        config[v] = RbConfig.expand(key, config)
        key = key.gsub(/#{Regexp.quote(pat)}(?=\s|\z)/n) {sub} if pat
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
