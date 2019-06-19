# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'rbconfig-for-mkmf'

module Truffle::CExt
  class Linker
    def self.main(argv = ARGV)
      argv = argv.dup
      @output = 'out.su'
      @libraries = []
      @files = []
      @search_paths = []
      @incflags = []

      process_args(argv)
      @files = @files.uniq
      @libraries = @libraries.uniq

      @libraries = resolve_libraries(@libraries, @search_paths)
      @files = compile_if_needed(@files)
      @files = link_bitcode(@files)
      Truffle::CExt.linker(@output, @libraries, @files)
    end

    def self.process_args(argv)
      arg = nil
      next_arg = -> description {
        raise "#{arg} needs to be followed by a #{description}" if argv.empty?
        argv.shift
      }

      while arg = argv.shift
        case arg
        when ''
          # Empty arg, do nothing.
        when '-h', '-help', '--help', '/?', '/help'
          puts "#{$0} -e Truffle::CExt::Linker.main [-o out.su] [-l one.so -l two.so ...] one.bc two.bc ..."
          puts '  Links zero or more LLVM binary bitcode files into a single file which can be loaded by Sulong.'
          exit
        when '-o'
          @output = next_arg['file name']
        when '-I'
          @incflags << '-I' << next_arg['directory name']
        when '-l'
          lib = next_arg['file name']
          @libraries << standardize_lib_name(lib)
        when /\A-l(.+)\z/ # -llib as a single argument
          lib = $1
          @libraries << standardize_lib_name(lib)
        when '-L'
          @search_paths << next_arg['directory name']
        when /\A-L(.+)\z/ # -L/libdir as a single argument
          @search_paths <<  $1
        when /\A-Wl(.+)\z/
          subargs = $1.split(',')
          process_args(subargs)
        when '-rpath'
          @search_paths << next_arg['directory name']
        else
          if arg.start_with?('-')
            raise "Unknown argument: #{arg}"
          else
            @files << arg
          end
        end
      end
    end

    def self.compile_if_needed(files)
      files.map do |file|
        if file.end_with?('.c')
          objfile = "#{File.dirname(file)}/#{File.basename(file, '.*')}.#{RbConfig::CONFIG['OBJEXT']}"
          compile = RbConfig::CONFIG['TRUFFLE_RAW_COMPILE_C'].gsub('$<', file).gsub('$@', objfile)
          compile = compile.sub('$(INCFLAGS)', @incflags.join(' '))
          compile = compile.sub('$(COUTFLAG)', '')
          if system(compile)
            objfile
          else
            raise "command failed: #{compile}"
          end
        else
          file
        end
      end
    end

    def self.link_bitcode(files)
      output = 'out.bc'
      command = [RbConfig::CONFIG['LLVM_LINK'], '-o', output, *files]
      raise "Linker failed: #{command}" unless system(*command)
      [output]
    end

    def self.resolve_libraries(libraries, search_paths)
      require 'pathname'
      libraries.map do |lib|
        if Pathname.new(lib).absolute?
          lib
        else
          library_in_search_path_or_default(lib, search_paths)
        end
      end
    end

    def self.library_in_search_path_or_default(lib, search_paths)
      search_paths.each do |path|
        lib_in_path = File.join(path, lib)
        if File.exist?(lib_in_path)
          return lib_in_path
        end
      end
      lib
    end

    def self.standardize_lib_name(lib_name)
      require 'ffi'
      extend FFI::Library

      # Take a first pass at normalizing the library name much like the linker would normally do for us.
      # Since we ultimately call `dlopen` to open the library, the name must be in a form that `dlopen` can
      # handle. This pass will, for example, map "jpeg" -> "libjpeg.so".
      normalized_lib_name = FFI.map_library_name(lib_name)

      # Mapping a library name is a best effort approach towards standardizing a library name. It works in many cases,
      # but in others the simple mapping is not sufficient. On Linux, for example, that name may correspond to an `ld`
      # script, rather than the actual library we want to load. So, we try loading the library here, which will attempt
      # to resolve the full library path if necessary (and possible).
      begin
        dynamic_lib = ffi_lib(normalized_lib_name).first
        dynamic_lib.name
      rescue LoadError
        # A LoadError indicates `ffi_lib` couldn't find a library that would successfully load with `dlopen`. The search
        # path used for the `dlopen` calls may be incomplete and it's possible that Sulong, which runs downstream, may
        # have a more comprehensive search path. Additionally, it's possible Sulong never even needs to load the library.
        # Consequently, on search failure, we just return the normalized library name and allow Sulong an attempt at
        # validation.
        warn "Could not find a search path for #{lib_name} -- passing #{normalized_lib_name} on to Sulong"
        normalized_lib_name
      end
    end
  end
end

Truffle::CExt::Linker.main
