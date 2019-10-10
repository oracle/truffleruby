# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Split from rbconfig.rb since we needed to do expensive tasks like checking the
# clang, opt and llvm-link versions, historically.
# TODO: consider merging in rbconfig.rb

require 'rbconfig'
require_relative 'truffle/cext_preprocessor.rb'

debugflags = ''
warnflags = [
  '-Wimplicit-function-declaration', # To make missing C ext functions clear
  '-Wundef',                         # Warn for undefined preprocessor macros
  '-Wno-unknown-warning-option',     # If we're on an earlier version of clang without a warning option, ignore it
  '-Wno-int-conversion',             # MRI has VALUE defined as long while we have it as void*
  '-Wno-int-to-pointer-cast',        # Same as above
  '-Wno-incompatible-pointer-types', # Fix byebug 8.2.1 compile (st_data_t error)
  '-Wno-format-invalid-specifier',   # Our PRIsVALUE generates this because compilers ignore printf extensions
  '-Wno-format-extra-args',          # Our PRIsVALUE generates this because compilers ignore printf extensions
  '-ferror-limit=500'
].join(' ')

base_cflags = "#{debugflags} #{warnflags}"
cflags = "#{base_cflags} -fPIC -c"
cxxflags = cflags

cext_dir = "#{RbConfig::CONFIG['libdir']}/cext"

dlext = RbConfig::CONFIG['DLEXT']

expanded = RbConfig::CONFIG
mkconfig = RbConfig::MAKEFILE_CONFIG

if Truffle::Boot.get_option 'building-core-cexts'
  ruby_home = Truffle::Boot.ruby_home

  libtruffleruby = "#{ruby_home}/src/main/c/cext/libtruffleruby.#{dlext}"

  relative_debug_paths = "-fdebug-prefix-map=#{ruby_home}=."
  expanded['CPPFLAGS'] = mkconfig['CPPFLAGS'] = relative_debug_paths
else
  libtruffleruby = "#{cext_dir}/libtruffleruby.#{dlext}"
end

# Link to libtruffleruby by absolute path
libtruffleruby_dir = File.dirname(libtruffleruby)
librubyarg = "-L#{libtruffleruby_dir} -rpath #{libtruffleruby_dir} -ltruffleruby -lpolyglot-mock"

common = {
  'LIBRUBYARG' => librubyarg,
  'LIBRUBYARG_SHARED' => librubyarg,
  'debugflags' => debugflags,
  'warnflags' => warnflags,
  'CFLAGS' => cflags,
  'CXXFLAGS' => cxxflags
}
expanded.merge!(common)
mkconfig.merge!(common)

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

  c_flags = '$(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@'
  cxx_flags = '$(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@'

  mkconfig['TRUFFLE_RAW_COMPILE_C'] = for_file.call('$(CC)', c_flags)
  mkconfig['COMPILE_C'] = with_conditional_preprocessing.call(
    for_pipe.call('$(CC)', c_flags),
    for_file.call('$(CC)', c_flags))

  mkconfig['COMPILE_CXX'] = with_conditional_preprocessing.call(
    for_pipe.call('$(CXX)', cxx_flags),
    for_file.call('$(CXX)', cxx_flags))
end

# From mkmf.rb: "$(CC) #{OUTFLAG}#{CONFTEST}#{$EXEEXT} $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(src) $(LIBPATH) $(LDFLAGS) $(ARCH_FLAG) $(LOCAL_LIBS) $(LIBS)"
mkconfig['TRY_LINK'] = "$(CC) -o conftest $(INCFLAGS) $(CPPFLAGS) #{base_cflags} $(src) $(LIBPATH) $(LDFLAGS) $(ARCH_FLAG) $(LOCAL_LIBS) $(LIBS)"

%w[COMPILE_C COMPILE_CXX TRY_LINK TRUFFLE_RAW_COMPILE_C].each do |key|
  expanded[key] = mkconfig[key].gsub(/\$\((\w+)\)/) { expanded.fetch($1) { $& } }
end
