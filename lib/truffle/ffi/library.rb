# From https://raw.githubusercontent.com/ffi/ffi/1.9.18/lib/ffi/library.rb
# Only a minimal subset to lookup libraries.
#
# Copyright (C) 2008-2010 Wayne Meissner
#
# This file is part of ruby-ffi.
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the Ruby FFI project nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.#

module FFI
  CURRENT_PROCESS = USE_THIS_PROCESS_AS_LIBRARY = Object.new

  # @param [#to_s] lib library name
  # @return [String] library name formatted for current platform
  # Transform a generic library name to a platform library name
  # @example
  #  # Linux
  #  FFI.map_library_name 'c'     # -> "libc.so.6"
  #  FFI.map_library_name 'jpeg'  # -> "libjpeg.so"
  #  # Windows
  #  FFI.map_library_name 'c'     # -> "msvcrt.dll"
  #  FFI.map_library_name 'jpeg'  # -> "jpeg.dll"
  def self.map_library_name(lib)
    # Mangle the library name to reflect the native library naming conventions
    lib = lib.to_s unless lib.kind_of?(String)
    lib = Library::LIBC if lib == 'c'

    if lib && File.basename(lib) == lib
      lib = Platform::LIBPREFIX + lib unless lib =~ /^#{Platform::LIBPREFIX}/
      r = Platform::IS_GNU ? '\\.so($|\\.[1234567890]+)' : "\\.#{Platform::LIBSUFFIX}$"
      lib += ".#{Platform::LIBSUFFIX}" unless lib =~ /#{r}/
    end

    lib
  end

  module Library

    # @param [Array] names names of libraries to load
    # @return [Array<DynamicLibrary>]
    # @raise {LoadError} if a library cannot be opened
    # Load native libraries.
    def ffi_lib(*names)
      raise LoadError.new('library names list must not be empty') if names.empty?

      lib_flags = defined?(@ffi_lib_flags) ? @ffi_lib_flags : FFI::DynamicLibrary::RTLD_LAZY | FFI::DynamicLibrary::RTLD_LOCAL
      ffi_libs = names.map do |name|

        if name == FFI::CURRENT_PROCESS
          FFI::DynamicLibrary.open(nil, FFI::DynamicLibrary::RTLD_LAZY | FFI::DynamicLibrary::RTLD_LOCAL)

        else
          libnames = (name.is_a?(::Array) ? name : [ name ]).map { |n| [ n, FFI.map_library_name(n) ].uniq }.flatten.compact
          lib = nil
          errors = {}

          libnames.each do |libname|
            begin
              orig = libname
              lib = FFI::DynamicLibrary.open(libname, lib_flags)
              break if lib

            rescue Exception => ex
              ldscript = false
              if ex.message =~ /(([^ \t()])+\.so([^ \t:()])*):([ \t])*(invalid ELF header|file too short|invalid file format)/
                if File.read($1) =~ /(?:GROUP|INPUT) *\( *([^ \)]+)/
                  libname = $1
                  ldscript = true
                end
              end

              if ldscript
                retry
              else
                # TODO better library lookup logic
                libname = libname.to_s
                unless libname.start_with?('/')
                  path = ['/usr/lib/','/usr/local/lib/'].find do |pth|
                    File.exist?(pth + libname)
                  end
                  if path
                    libname = path + libname
                    retry
                  end
                end

                libr = (orig == libname ? orig : "#{orig} #{libname}")
                errors[libr] = ex
              end
            end
          end

          if lib.nil?
            raise LoadError.new(errors.values.join(".\n"))
          end

          # return the found lib
          lib
        end
      end

      @ffi_libs = ffi_libs
    end
  end
end
