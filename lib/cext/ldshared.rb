# The equivalent of cc -shared, as MRI's RbConfig::CONFIG['LDSHARED']
# Compiles a C file and link it as a shared library.

require 'rbconfig-for-mkmf'

argv = ARGV.dup
incdirs = []
linker_args = []
src = nil
cflags = RbConfig::CONFIG['CFLAGS'].split

while arg = argv.shift
  case arg
  when -> _ { cflags.include?(arg) }
    # Ignore, we already have cflags below
  when '-I'
    incdirs << arg << argv.shift
  when '-o'
    linker_args << arg << argv.shift
  when /.+\.c$/
    raise 'Only one .c' if src
    src = arg
  else
    raise "Unknown arg: #{arg}"
  end
end

raise 'Missing .c' unless src

objfile = "#{File.dirname(src)}/#{File.basename(src, '.*')}.#{RbConfig::CONFIG['OBJEXT']}"
compile = RbConfig::CONFIG['COMPILE_C'].gsub('$<', src).gsub('$@', objfile)
compile = compile.sub('$(INCFLAGS)', incdirs.join(' '))
compile = compile.sub('$(COUTFLAG)', '')

unless system(compile)
  raise "command failed: #{compile}"
end

Truffle::CExt::Linker.main([*linker_args, objfile])
