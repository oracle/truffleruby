# frozen-string-literal: false
#
# Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
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

require_relative 'truffle/cext_preprocessor.rb'

module RbConfig

  ruby_home = Truffle::Boot.ruby_home
  raise 'The TruffleRuby home needs to be set to require RbConfig' unless ruby_home
  TOPDIR = ruby_home

  host_os  = Truffle::System.host_os
  host_cpu = Truffle::System.host_cpu
  host_vendor = 'unknown'
  # Some config entries report linux-gnu rather than simply linux.
  if host_os == 'linux'
    host_os_full = 'linux-gnu'
  else
    host_os_full = host_os
  end
  # Host should match the target triplet for clang or GCC, otherwise some C extension builds will fail.
  host         = "#{host_cpu}-#{host_vendor}-#{host_os_full}"

  ruby_install_name = 'truffleruby'

  ruby_base_name = 'ruby'

  # The full TruffleRuby version, so C extensions from one TruffleRuby version
  # are not reused with another TruffleRuby version.
  ruby_abi_version = RUBY_ENGINE_VERSION

  arch     = "#{host_cpu}-#{host_os}"
  libs     = ''

  # Sorted alphabetically using sort(1)
  CONFIG = {
    'arch'              => arch,
    'ARCH_FLAG'         => '',
    'build'             => host,
    'build_os'          => host_os_full,
    'configure_args'    => ' ',
    'CCDLFLAGS'         => '-fPIC',
    'COUTFLAG'          => '-o ',
    'CPPOUTFILE'        => '-o conftest.i',
    'DLDLIBS'           => '',
    'DLEXT'             => Truffle::Platform::DLEXT.dup,
    'ENABLE_SHARED'     => 'yes', # We use a dynamic library for libruby
    'EXECUTABLE_EXTS'   => '',
    'exeext'            => '',
    'EXEEXT'            => '',
    'host_alias'        => '',
    'host_cpu'          => host_cpu,
    'host'              => host,
    'host_os'           => host_os_full,
    'libdirname'        => 'libdir',
    'LIBEXT'            => 'a',
    'LIBRUBY'           => '',
    'LIBRUBY_A'         => '',
    'LIBRUBYARG_STATIC' => '',
    'LIBRUBY_SO'        => "cext/libtruffleruby.#{Truffle::Platform::SOEXT}",
    'LIBS'              => libs,
    'NULLCMD'           => ':',
    'OBJEXT'            => 'o',
    'OUTFLAG'           => '-o ',
    'PATH_SEPARATOR'    => File::PATH_SEPARATOR.dup,
    'prefix'            => '',
    'RM'                => 'rm -f',
    'RUBY_BASE_NAME'    => ruby_base_name,
    'ruby_install_name' => ruby_install_name,
    'RUBY_INSTALL_NAME' => ruby_install_name,
    'ruby_version'      => ruby_abi_version.dup,
    'SOEXT'             => Truffle::Platform::SOEXT.dup,
    'target_cpu'        => host_cpu,
    'target_os'         => host_os,
    'UNICODE_VERSION'   => '12.0.0',
    'UNICODE_EMOJI_VERSION' => '12.0',
  }

  MAKEFILE_CONFIG = CONFIG.dup

  expanded = CONFIG
  mkconfig = MAKEFILE_CONFIG

  expanded['RUBY_SO_NAME'] = ruby_base_name
  mkconfig['RUBY_SO_NAME'] = '$(RUBY_BASE_NAME)'

  prefix        = ruby_home
  bindir        = "#{prefix}/bin"
  graalvm_home = Truffle::System.get_java_property 'org.graalvm.home'
  extra_bindirs = if graalvm_home
                    jre_bin = "#{graalvm_home}/jre/bin"
                    [
                        "#{graalvm_home}/bin",
                        *(jre_bin if File.directory?(jre_bin))
                    ]
                  else
                    []
                  end

  common = {
    'prefix' => prefix,
    'bindir' => bindir,
    'extra_bindirs' => extra_bindirs.join(File::PATH_SEPARATOR),
    'hdrdir' => "#{prefix}/lib/cext/include",
    'rubyhdrdir' => "#{prefix}/lib/cext/include",
    'rubyarchhdrdir' => "#{prefix}/lib/cext/include",
    'includedir' => "#{prefix}/lib/cext", # the parent dir of rubyhdrdir
    'sysconfdir' => "#{prefix}/etc", # doesn't exist, as in MRI
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
  expanded['rubylibdir'] = "#{libdir}/mri"
  mkconfig['rubylibdir'] = '$(libdir)/mri'
  rubyarchdir = \
  expanded['rubyarchdir'] = rubylibdir
  mkconfig['rubyarchdir'] = '$(rubylibdir)'
  archdir = \
  expanded['archdir'] = rubyarchdir
  mkconfig['archdir'] = '$(rubyarchdir)'
  sitearch = \
  expanded['sitearch'] = arch
  mkconfig['sitearch'] = '$(arch)'
  sitedir = \
  expanded['sitedir'] = "#{rubylibprefix}/site_ruby"
  mkconfig['sitedir'] = '$(rubylibprefix)/site_ruby'
  # Must be kept in sync with post.rb
  sitelibdir = \
  expanded['sitelibdir'] = "#{sitedir}/#{ruby_abi_version}"
  mkconfig['sitelibdir'] = '$(sitedir)/$(ruby_version)'
  expanded['sitearchdir'] = "#{sitelibdir}/#{sitearch}"
  mkconfig['sitearchdir'] = '$(sitelibdir)/$(sitearch)'
  expanded['topdir'] = archdir
  mkconfig['topdir'] = '$(archdir)'
  datarootdir = \
  expanded['datarootdir'] = "#{prefix}/share"
  mkconfig['datarootdir'] = '$(prefix)/share'
  expanded['ridir'] = "#{datarootdir}/ri"
  mkconfig['ridir'] = '$(datarootdir)/ri'

  # Defined here for RubyInline
  cc = Truffle::Boot.tool_path(:CC)
  cxx = Truffle::Boot.tool_path(:CXX)

  expanded['AR'] = mkconfig['AR'] = Truffle::Boot.tool_path(:AR)
  expanded['STRIP'] = mkconfig['STRIP'] = Truffle::Boot.tool_path(:STRIP)

  expanded['CC'] = mkconfig['CC'] = cc
  expanded['CXX'] = mkconfig['CXX'] = cxx

  expanded['CPP'] = "#{cc} -E"
  mkconfig['CPP'] = '$(CC) -E'
  if Truffle::Platform.darwin?
    expanded['LDSHARED'] = "#{cc} -dynamic -bundle"
    mkconfig['LDSHARED'] = '$(CC) -dynamic -bundle'
    expanded['LDSHAREDXX'] = "#{cxx} -dynamic -bundle"
    mkconfig['LDSHAREDXX'] = '$(CXX) -dynamic -bundle'
  else
    expanded['LDSHARED'] = "#{cc} -shared"
    mkconfig['LDSHARED'] = '$(CC) -shared'
    expanded['LDSHAREDXX'] = "#{cxx} -shared"
    mkconfig['LDSHAREDXX'] = '$(CXX) -shared'
  end

  # Determine the various flags for native compilation
  optflags = ''
  debugflags = ''
  warnflags = [
      '-Wimplicit-function-declaration', # To make missing C ext functions clear
      '-Wno-int-conversion',             # MRI has VALUE defined as long while we have it as void*
      '-Wno-int-to-pointer-cast',        # Same as above
      '-Wno-incompatible-pointer-types', # Fix byebug 8.2.1 compile (st_data_t error)
      '-Wno-format-invalid-specifier',   # Our PRIsVALUE generates this because compilers ignore printf extensions
      '-Wno-format-extra-args',          # Our PRIsVALUE generates this because compilers ignore printf extensions
      '-ferror-limit=500'
  ]

  cppflags = ''
  defs = ''
  ldflags = ''
  dldflags = Truffle::Platform.darwin? ? '-Wl,-undefined,dynamic_lookup -Wl,-multiply_defined,suppress' : ''

  cext_dir = "#{RbConfig::CONFIG['libdir']}/cext"
  dlext = RbConfig::CONFIG['DLEXT']

  # Make C extensions use the same libssl as the one used for the openssl C extension
  if Truffle::Platform.darwin?
    require 'truffle/openssl-prefix'
    openssl_prefix = ENV['OPENSSL_PREFIX']
    if openssl_prefix
      # Change the same variables as MRI's --with-opt-dir configure option would
      cppflags << " -I#{openssl_prefix}/include"
      ldflags << " -L#{openssl_prefix}/lib"
      dldflags << " -L#{openssl_prefix}/lib"
    end
  end

  # Set extra flags needed for --building-core-cexts
  if Truffle::Boot.get_option 'building-core-cexts'
    ruby_home = Truffle::Boot.ruby_home
    libtruffleruby = "#{ruby_home}/src/main/c/cext/libtruffleruby.#{dlext}"

    relative_debug_paths = "-fdebug-prefix-map=#{ruby_home}=."
    cppflags << relative_debug_paths

    warnflags << '-Wundef' # Warn for undefined preprocessor macros for core C extensions
    warnflags << '-Werror' # Make sure there are no warnings in core C extensions
  else
    libtruffleruby = "#{cext_dir}/libtruffleruby.#{dlext}"

    # GR-19453: workaround for finding libc++.so when using NFI on the library since the toolchain does not pass -rpath automatically
    rpath_libcxx = " -rpath #{File.expand_path("../../lib", RbConfig::CONFIG['CC'])}"
    ldflags << rpath_libcxx
    dldflags << rpath_libcxx
  end

  # Link to libtruffleruby by absolute path
  libtruffleruby_dir = File.dirname(libtruffleruby)
  librubyarg = "-L#{libtruffleruby_dir} -rpath #{libtruffleruby_dir} -ltruffleruby -lpolyglot-mock"

  warnflags = warnflags.join(' ')

  common = {
      'optflags' => optflags,
      'debugflags' => debugflags,
      'warnflags' => warnflags,
      'cppflags' => cppflags,
      'DEFS' => defs,
      'DLDFLAGS' => dldflags,
      'LDFLAGS' => ldflags,
      'LIBRUBYARG' => librubyarg,
      'LIBRUBYARG_SHARED' => librubyarg,
  }
  expanded.merge!(common)
  mkconfig.merge!(common)

  cflags = \
  expanded['cflags'] = "#{optflags} #{debugflags} #{warnflags}"
  mkconfig['cflags'] = '$(optflags) $(debugflags) $(warnflags)'
  expanded['CFLAGS'] = cflags
  mkconfig['CFLAGS'] = '$(cflags)'

  cxxflags = \
  expanded['cxxflags'] = "#{optflags} #{debugflags} #{warnflags}"
  mkconfig['cxxflags'] = '$(optflags) $(debugflags) $(warnflags)'
  expanded['CXXFLAGS'] = cxxflags
  mkconfig['CXXFLAGS'] = '$(cxxflags)'
  defs = ''
  cppflags_hardcoded = Truffle::Platform.darwin? ? ' -D_DARWIN_C_SOURCE' : ''
  expanded['CPPFLAGS'] = "#{cppflags_hardcoded} #{defs} #{cppflags}"
  mkconfig['CPPFLAGS'] = "#{cppflags_hardcoded} $(DEFS) $(cppflags)"

  # We use -I$(<D) (the directory portion of the prerequisite - i.e. the
  # C or C++ file) to add the file's path as the first entry on the
  # include path. This is to ensure that files from the source file's
  # directory are included in preference to others on the include path,
  # and is required because we are actually piping the file into the
  # compiler which disables this standard behaviour of the C preprocessor.
  begin
    with_conditional_preprocessing = proc do |command1, command2|
      Truffle::CExt::Preprocessor.makefile_matcher(command1, command2)
    end

    for_file = proc do |compiler, flags|
      "#{compiler} #{flags} $(CSRCFLAG)$<"
    end

    for_pipe = proc do |compiler, flags|
      language_flag = '$(CXX)' == compiler ? '-xc++' : '-xc'
      "#{RbConfig.ruby} #{cext_dir}/preprocess.rb $< | #{compiler} -I$(<D) #{flags} #{language_flag} -"
    end

    c_flags = '$(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -c'
    cxx_flags = '$(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -c'

    mkconfig['COMPILE_C'] = with_conditional_preprocessing.call(
        for_pipe.call('$(CC)', c_flags),
        for_file.call('$(CC)', c_flags))

    mkconfig['COMPILE_CXX'] = with_conditional_preprocessing.call(
        for_pipe.call('$(CXX)', cxx_flags),
        for_file.call('$(CXX)', cxx_flags))
  end

  %w[COMPILE_C COMPILE_CXX].each do |key|
    expanded[key] = mkconfig[key].gsub(/\$\((\w+)\)/) { expanded.fetch($1) { $& } }
  end

  launcher = Truffle::Boot.get_option 'launcher'
  if launcher.empty?
    if ruby_home
      launcher = "#{bindir}/#{ruby_install_name}"
    else
      launcher = ruby_install_name
    end
  end

  RUBY = launcher

  def self.ruby
    RUBY
  end

  def RbConfig.expand(val, config = CONFIG)
    newval = val.gsub(/\$\$|\$\(([^()]+)\)|\$\{([^{}]+)\}/) do
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
    end
    val.replace(newval) unless newval == val
    val
  end

end

CROSS_COMPILING = nil
