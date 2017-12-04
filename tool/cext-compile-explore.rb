# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# For example:
# $ jt ruby tool/cext-compile-explore.rb spec/ruby/optional/capi/ext/util_spec.c -Ispec/ruby/optional/capi/ext

raise 'you need to run this with TruffleRuby' unless RUBY_ENGINE == 'truffleruby'

require 'rbconfig'
require 'rbconfig-for-mkmf'

root          = File.expand_path(__dir__ + '/..')
file          = ARGV.shift
file_basename = File.basename file, '.*'
file_dir      = File.dirname file

args = ARGV

def run(command)
  puts '$ ' + command
  exit 1 unless system command
end

args_joined = args.join(' ')

# use dot prefix so the files are not picked up and packed into .su file
preprocessed_path = "#{file_dir}/.#{file_basename}.pre.c"
expanded_path     = "#{file_dir}/.#{file_basename}.cpp.c"
frontend_path     = "#{file_dir}/.#{file_basename}.frontend.bc"
opt_path          = "#{file_dir}/.#{file_basename}.opt.bc"

run "ruby #{root}/lib/cext/preprocess.rb #{file} > #{preprocessed_path}"

run "#{RbConfig::CONFIG['CC']} -Wno-macro-redefined -E -I#{RbConfig::CONFIG['rubyhdrdir']} " +
        "#{args_joined} #{preprocessed_path} -o #{expanded_path}"
run "#{RbConfig::CONFIG['CC']} -Werror=implicit-function-declaration -Wno-int-conversion -Wno-int-to-pointer-cast " +
        "-Wno-macro-redefined -Wno-unused-value -c -emit-llvm -I#{RbConfig::CONFIG['rubyhdrdir']} " +
        "#{args_joined} #{expanded_path} -o #{frontend_path}"
run "llvm-dis #{frontend_path}"

opt_passes = %w[-always-inline -mem2reg -constprop]
run "opt #{opt_passes.join(' ')} #{frontend_path} -o #{opt_path}"
run "llvm-dis #{opt_path}"
