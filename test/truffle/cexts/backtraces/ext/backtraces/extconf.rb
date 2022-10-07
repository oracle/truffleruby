require 'mkmf'

# Compile a real native library
# Commands from src/main/c/truffleposix/Makefile

def command(*args)
  $stderr.puts args.join(' ')
  ret = system(*args)
  raise unless ret
end

dir = File.expand_path('../..', __FILE__)
name = "#{dir}/libnativetest"
so = RbConfig::CONFIG['SOEXT']

original_path = ENV['PATH'].delete_prefix("#{RbConfig::CONFIG['toolchain_path']}:")
system_cc = find_executable('cc', original_path)

cflags = %w[-Wall -Werror -fPIC -std=c99]
ldflags = %w[]

command system_cc, '-o', "#{name}.o", '-c', *cflags, *ldflags, "#{name}.c"
command system_cc, '-shared', *ldflags, '-o', "#{name}.#{so}", "#{name}.o"

$LIBS += " #{name}.#{so}"

create_makefile('backtraces')
