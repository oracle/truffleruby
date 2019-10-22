# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Split from rbconfig.rb since we needed to do expensive tasks like checking the
# clang, opt and llvm-link versions, historically.
# TODO: consider merging in rbconfig.rb

require 'rbconfig'
require_relative 'truffle/cext_preprocessor.rb'

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
dldflags = ''

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
end

# Link to libtruffleruby by absolute path
libtruffleruby_dir = File.dirname(libtruffleruby)
librubyarg = "-L#{libtruffleruby_dir} -rpath #{libtruffleruby_dir} -ltruffleruby -lpolyglot-mock"

warnflags = warnflags.join(' ')

# Set values in RbConfig
expanded = RbConfig::CONFIG
mkconfig = RbConfig::MAKEFILE_CONFIG

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
expanded['CPPFLAGS'] = " #{defs} #{cppflags}"
mkconfig['CPPFLAGS'] = ' $(DEFS) $(cppflags)'

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
