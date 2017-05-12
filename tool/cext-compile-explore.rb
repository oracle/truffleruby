# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# For example:
# $ jt run tool/cext-compile-explore.rb spec/ruby/optional/capi/ext/util_spec.c -Ispec/ruby/optional/capi/ext

raise 'you need to run this with TruffleRuby' unless RUBY_ENGINE == 'truffleruby'

require 'rbconfig'

file = ARGV.shift
args = ARGV

def run(command)
  puts '$ ' + command
  exit 1 unless system command
end

args_joined = args.join(' ')

run "ruby lib/cext/preprocess.rb #{file} > explore-pre.c"
run "#{RbConfig::CONFIG['CC']} -Wno-macro-redefined -E -I#{RbConfig::CONFIG['rubyhdrdir']} " +
        "#{args_joined} explore-pre.c -o explore-cpp.c"
run "#{RbConfig::CONFIG['CC']} -Werror=implicit-function-declaration -Wno-int-conversion -Wno-int-to-pointer-cast " +
        "-Wno-macro-redefined -Wno-unused-value -c -emit-llvm -I#{RbConfig::CONFIG['rubyhdrdir']} " +
        "#{args_joined} explore-pre.c -o explore-frontend.bc"
run 'llvm-dis-3.8 explore-frontend.bc'

opt_passes = %w[-always-inline -mem2reg -constprop]
run "#{ENV['JT_OPT'] || 'opt'} #{opt_passes.join(' ')} explore-frontend.bc -o explore-opt.bc"
run 'llvm-dis-3.8 explore-opt.bc'
