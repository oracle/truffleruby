# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Split from rbconfig.rb since we need to do expensive tasks like checking the
# clang, opt and llvm-link versions.

require 'rbconfig'
require_relative 'truffle/cext_preprocessor.rb'

search_paths = {}
if Truffle::Platform.darwin?
  homebrew_prefix = `brew --prefix llvm@4 2>/dev/null`.chomp
  if $?.success? and Dir.exist?("#{homebrew_prefix}/bin") # Homebrew
    search_paths["#{homebrew_prefix}/bin/"] = "#{homebrew_prefix}/bin"
  elsif Dir.exist?('/opt/local/libexec/llvm-4.0/bin') # MacPorts
    search_paths['/opt/local/libexec/llvm-4.0/bin/'] = '/opt/local/libexec/llvm-4.0/bin'
  end
end
search_paths[''] = '$PATH'

# First, find in which prefix clang, opt and llvm-link are available.
# We want to use all tools from the same prefix.
versions = {}
prefix = search_paths.keys.find do |search_path|
  %w[clang opt llvm-link].all? do |tool|
    tool_path = "#{search_path}#{tool}"
    begin
      versions[tool_path] = `#{tool_path} --version`
    rescue Errno::ENOENT
      false # Not found
    end
  end
end

unless prefix
  search_paths_description = search_paths.values.join(' or ')
  abort "The clang, opt and llvm-link tools, part of LLVM, do not appear to be available in #{search_paths_description}.\n" +
        'You need to install LLVM, see https://github.com/oracle/truffleruby/blob/master/doc/user/installing-llvm.md'
end

clang = "#{prefix}clang"
opt = "#{prefix}opt"
llvm_link = "#{prefix}llvm-link"
extra_cflags = nil

# Check the versions
versions.each_pair do |tool, version|
  if version =~ /\bversion (\d+\.\d+\.\d+)/
    major, minor, _patch = $1.split('.').map(&:to_i)
    if (major == 3 && minor >= 8) || (major == 4 && minor == 0)
      # OK
    elsif major >= 5
      extra_cflags = '-Xclang -disable-O0-optnone'
    else
      abort "unsupported #{tool} version: #{$1}, see https://github.com/oracle/truffleruby/blob/master/doc/user/installing-llvm.md"
    end
  else
    abort "cannot parse the version of #{tool} from #{version.inspect}"
  end
end

opt_passes = ['-always-inline', '-mem2reg', '-constprop'].join(' ')

debugflags = '-g' # Show debug information such as line numbers in backtrace
warnflags = [
  '-Wimplicit-function-declaration', # To make missing C ext functions clear
  '-Wundef',                         # Warn for undefined preprocessor macros
  '-Wno-unknown-warning-option',     # If we're on an earlier version of clang without a warning option, ignore it
  '-Wno-int-conversion',             # MRI has VALUE defined as long while we have it as void*
  '-Wno-int-to-pointer-cast',        # Same as above
  '-Wno-incompatible-pointer-types', # Fix byebug 8.2.1 compile (st_data_t error)
  '-Wno-format-invalid-specifier',   # Our PRIsVALUE generates this because compilers ignore printf extensions
  '-ferror-limit=500'
].join(' ')

cc = clang
cxx = "#{clang}++"

base_cflags = "#{debugflags} #{warnflags}"
cflags = "#{base_cflags} -c -emit-llvm"
cflags = "#{extra_cflags} #{cflags}" if extra_cflags
cxxflags = "#{cflags} -stdlib=libc++"

cext_dir = "#{RbConfig::CONFIG['libdir']}/cext"

expanded = RbConfig::CONFIG
mkconfig = RbConfig::MAKEFILE_CONFIG

if Truffle::Boot.get_option 'building-core-cexts'
  ruby_home = Truffle::Boot.ruby_home

  link_o_files = "#{ruby_home}/src/main/c/cext/ruby.o #{ruby_home}/src/main/c/sulongmock/sulongmock.o"

  relative_debug_paths = "-fdebug-prefix-map=#{ruby_home}=."
  polyglot_h = "-DSULONG_POLYGLOT_H='\"#{ENV.fetch('SULONG_POLYGLOT_H')}\"'"
  mkconfig['CPPFLAGS'] = "#{relative_debug_paths} #{polyglot_h}"
  expanded['CPPFLAGS'] = mkconfig['CPPFLAGS']

  # Default to the ruby in $PATH to build core C extensions faster
  preprocess_ruby = ENV['TRUFFLERUBY_PREPROCESS_RUBY'] || 'ruby'
else
  link_o_files = "#{cext_dir}/ruby.o #{cext_dir}/sulongmock.o"

  # TRUFFLERUBY_PREPROCESS_RUBY must only be used for TruffleRuby development, at your own risks
  preprocess_ruby = ENV['TRUFFLERUBY_PREPROCESS_RUBY'] || RbConfig.ruby
end

common = {
  'CC' => cc,
  'CPP' => cc,
  'CXX' => cxx,
  'LLVM_LINK' => llvm_link,
  'LDSHARED' => "#{RbConfig.ruby} #{cext_dir}/linker.rb",
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
# directory are include in preference to others on the include path,
# and is required because we are actually piping the file into the
# compiler which disables this standard behaviour of the C preprocessor.
begin
  with_conditional_preprocessing = proc do |command1, command2|
    Truffle::CExt::Preprocessor.makefile_matcher(command1, command2)
  end

  for_file = proc do |compiler, flags, opt_command|
    "#{compiler} #{flags} $< && #{opt_command}"
  end

  for_pipe = proc do |compiler, flags, opt_command|
    "#{preprocess_ruby} #{cext_dir}/preprocess.rb $< | #{compiler} -I$(<D) #{flags}&& #{opt_command}"
  end

  c_flags = '$(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$@ -xc'
  cxx_flags = '$(INCFLAGS) $(CPPFLAGS) $(CXXFLAGS) $(COUTFLAG)$@ -xc++'
  opt_command = "#{opt} #{opt_passes} $@ -o $@"

  mkconfig['TRUFFLE_RAW_COMPILE_C'] = for_file.call('$(CC)', c_flags, opt_command)
  mkconfig['COMPILE_C']   = with_conditional_preprocessing.call(
    for_pipe.call('$(CC)', c_flags, opt_command),
    for_file.call('$(CC)', c_flags, opt_command))

  mkconfig['COMPILE_CXX']   = with_conditional_preprocessing.call(
    for_pipe.call('$(CXX)', cxx_flags, opt_command),
    for_file.call('$(CXX)', cxx_flags, opt_command))
end

# From mkmf.rb: "$(CC) #{OUTFLAG}#{CONFTEST}#{$EXEEXT} $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(src) $(LIBPATH) $(LDFLAGS) $(ARCH_FLAG) $(LOCAL_LIBS) $(LIBS)"
mkconfig['TRY_LINK'] = "#{cc} -o conftest $(INCFLAGS) $(CPPFLAGS) #{base_cflags} #{link_o_files} $(src) $(LIBPATH) $(LDFLAGS) $(ARCH_FLAG) $(LOCAL_LIBS) $(LIBS)"

%w[COMPILE_C COMPILE_CXX TRY_LINK TRUFFLE_RAW_COMPILE_C].each do |key|
  expanded[key] = mkconfig[key].gsub(/\$\((\w+)\)/) { expanded.fetch($1) { $& } }
end
