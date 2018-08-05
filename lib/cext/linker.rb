# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::CExt
  class Linker
    def self.main(argv = ARGV)
      argv = argv.dup
      @output = 'out.su'
      @libraries = []
      @files = []
      @search_paths = []
      process_args(argv)
      @libraries = @libraries.uniq
      @libraries = resolve_libraries(@libraries, @search_paths)
      @files = @files.uniq
      @files = link_bitcode(@files)
      Truffle::CExt.linker(@output, @libraries, @files)
    end

    def self.process_args(argv)
      @llvm_link = argv.shift

      while arg = argv.shift
        case arg
        when ''
          # Empty arg, do nothing.
        when '-h', '-help', '--help', '/?', '/help'
          puts "#{$0} -e Truffle::CExt::Linker.main [-o out.su] [-l one.so -l two.so ...] one.bc two.bc ..."
          puts '  Links zero or more LLVM binary bitcode files into a single file which can be loaded by Sulong.'
        when '-o'
          raise '-o needs to be followed by a file name' if argv.empty?
          @output = argv.shift
        when '-l'
          raise '-l needs to be followed by a file name' if argv.empty?
          lib = argv.shift
          @libraries << standardize_lib_name(lib)
        when /\A-l(.+)\z/ # -llib as a single argument
          lib = $1
          @libraries << standardize_lib_name(lib)
        when '-L'
          raise '-L needs to be followed by a directory name' if argv.empty?
          @search_paths << argv.shift
        when /\A-L(.+)\z/ # -L/libdir as a single argument
          @search_paths <<  $1
        when /\A-Wl(.+)\z/
          subargs = $1.split(',')
          process_args(subargs)
        when '-rpath'
          raise '-rpath needs to be followed by a directory name' if argv.empty?
          @search_paths << argv.shift
        else
          if arg.start_with?('-')
            raise "Unknown argument: #{arg}"
          else
            @files << arg
          end
        end
      end
    end

    def self.link_bitcode(files)
      output = 'out.bc'
      command = [@llvm_link, '-o', output, *files]
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

if $0 == __FILE__
  Truffle::CExt::Linker.main
end
