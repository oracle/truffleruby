# frozen_string_literal: true

# Copyright (c) 2007-2015, Evan Phoenix and contributors
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
# * Neither the name of Rubinius nor the names of its contributors
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
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Truffle::FileOperations.exist? is used instead of File.exist? so that adding
# a mock for File.exist? does not affect Dir.glob.

class Dir
  module Glob
    no_meta_chars = '[^*?\\[\\]{}\\\\]'
    NO_GLOB_META_CHARS = /\A#{no_meta_chars}+\z/
    TRAILING_BRACES = /\A(#{no_meta_chars}+)(?:\{(#{no_meta_chars}*)\})?\z/

    class Node
      def initialize(nxt, flags)
        @flags = flags
        @next = nxt
        @separator = nil
      end

      attr_writer :separator

      def separator
        @separator || '/'
      end

      def path_join(parent, ent)
        Dir::Glob.path_join(parent, ent, separator)
      end
    end

    class ConstantDirectory < Node
      def initialize(nxt, flags, dir)
        super nxt, flags
        @dir = dir
      end

      def call(matches, path, glob_base_dir)
        full = path_join(path, @dir)

        # Don't check if full exists. It just costs us time
        # and the downstream node will be able to check properly.
        @next.call matches, full, glob_base_dir
      end
    end

    class ConstantEntry < Node
      def initialize(nxt, flags, name)
        super nxt, flags
        @name = name
      end

      def call(matches, parent, glob_base_dir)
        path = path_join(parent, @name)

        if Truffle::FileOperations.exist? path_join(glob_base_dir, path)
          matches << path
        end
      end
    end

    class RootDirectory < Node
      def call(matches, path, glob_base_dir)
        @next.call matches, '/', glob_base_dir
      end
    end

    class RecursiveDirectories < Node
      def call(matches, start, glob_base_dir)
        return if !start || !Truffle::FileOperations.exist?(path_join(glob_base_dir, start))

        # Even though the recursive entry is zero width
        # in this case, its left separator is still the
        # dominant one, so we fix things up to use it.
        switched = @next.dup
        switched.separator = @separator
        switched.call matches, start, glob_base_dir

        stack = [start]

        allow_dots = ((@flags & File::FNM_DOTMATCH) != 0)

        until stack.empty?
          path = stack.pop
          begin
            dir = Dir.new(path_join(glob_base_dir, path))
          rescue Errno::ENOTDIR
            next
          end

          fd = dir.fileno
          while dirent = Truffle::DirOperations.readdir(dir)
            ent = dirent[0]
            type = dirent[1]
            next if ent == '.' || ent == '..'
            is_dir = false
            if type == Truffle::DirOperations::DT_DIR
              is_dir = true
            elsif type == Truffle::DirOperations::DT_UNKNOWN
              mode = Truffle::POSIX.truffleposix_fstatat_mode(fd, ent, Truffle::DirOperations::AT_SYMLINK_NOFOLLOW)
              is_dir = Truffle::StatOperations.directory?(mode)
            end

            if is_dir and (allow_dots or ent.getbyte(0) != 46) # ?.
              full = path_join(path, ent)
              stack << full
              @next.call matches, full, glob_base_dir
            end
          end
          dir.close
        end
      end
    end

    class StartRecursiveDirectories < Node
      def call(matches, start, glob_base_dir)
        raise 'invalid usage' if start

        # Even though the recursive entry is zero width
        # in this case, its left separator is still the
        # dominant one, so we fix things up to use it.
        if @separator
          switched = @next.dup
          switched.separator = @separator
          switched.call matches, start, glob_base_dir
        else
          @next.call matches, start, glob_base_dir
        end

        stack = []

        allow_dots = ((@flags & File::FNM_DOTMATCH) != 0)

        dir = Dir.new(path_join(glob_base_dir, '.'))
        fd = dir.fileno
        while ent = dir.read
          next if ent == '.' || ent == '..'
          mode = Truffle::POSIX.truffleposix_fstatat_mode(fd, ent, Truffle::DirOperations::AT_SYMLINK_NOFOLLOW)

          if Truffle::StatOperations.directory?(mode) and (allow_dots or ent.getbyte(0) != 46) # ?.
            stack << ent
            @next.call matches, ent, glob_base_dir
          end
        end
        dir.close

        until stack.empty?
          path = stack.pop
          dir = Dir.new(path_join(glob_base_dir, path))
          fd = dir.fileno
          while ent = dir.read
            next if ent == '.' || ent == '..'
            full = path_join(path, ent)
            mode = Truffle::POSIX.truffleposix_fstatat_mode(fd, ent, Truffle::DirOperations::AT_SYMLINK_NOFOLLOW)

            if Truffle::StatOperations.directory?(mode) and (allow_dots or ent.getbyte(0) != 46) # ?.
              stack << full
              @next.call matches, full, glob_base_dir
            end
          end
          dir.close
        end
      end
    end

    class Match < Node
      def initialize(nxt, flags, glob)
        super nxt, flags
        @glob = glob || +''
      end

      def match?(str)
        File.fnmatch @glob, str, @flags
      end
    end

    class DirectoryMatch < Match
      def initialize(nxt, flags, glob)
        super

        @glob.gsub! '**', '*'
      end

      def call(matches, path, glob_base_dir)
        return if path and !Truffle::FileOperations.exist?(path_join(glob_base_dir, "#{path}/."))

        dir = Dir.new(path_join(glob_base_dir, path ? path : '.'))
        while ent = dir.read
          if match? ent
            full = path_join(path, ent)

            if File.directory? path_join(glob_base_dir, full)
              @next.call matches, full, glob_base_dir
            end
          end
        end
        dir.close
      end
    end

    class EntryMatch < Match
      def call(matches, path, glob_base_dir)
        return if path and !Truffle::FileOperations.exist?("#{path_join(glob_base_dir, path)}/.")

        dir_path = path_join(glob_base_dir, path ? path : '.')
        dir = Dir.allocate.send(:initialize_internal, dir_path)
        if dir
          begin
            while ent = dir.read
              if match? ent
                matches << path_join(path, ent)
              end
            end
          ensure
            dir.close
          end
        end
      end
    end

    class DirectoriesOnly < Node
      def call(matches, path, glob_base_dir)
        if path and Truffle::FileOperations.exist?("#{path_join(glob_base_dir, path)}/.")
          matches << "#{path}/"
        end
      end
    end

    def self.path_split(str)
      start = 0
      ret = []

      last_match = nil

      while match = Truffle::RegexpOperations.match_from(%r!/+!, str, start)
        cur_start = Primitive.match_data_byte_begin(match, 0)
        cur_end = Primitive.match_data_byte_end(match, 0)
        ret << str.byteslice(start, cur_start - start)
        ret << str.byteslice(cur_start, cur_end - cur_start)

        start = cur_end

        last_match = match
      end

      if last_match
        ret << last_match.post_match
      else
        ret << str
      end

      # Trim from end
      if !ret.empty?
        while s = ret.last and s.empty?
          ret.pop
        end
      end

      ret
    end

    def self.single_compile(glob, flags=0)
      if glob.getbyte(-1) != 47 && NO_GLOB_META_CHARS.match?(glob) # byte value 47 = ?/
        return ConstantEntry.new nil, flags, glob
      end

      parts = path_split(glob)

      if glob.getbyte(-1) == 47 # ?/
        last = DirectoriesOnly.new nil, flags
      else
        file = parts.pop
        if NO_GLOB_META_CHARS.match?(file)
          last = ConstantEntry.new nil, flags, file
        else
          last = EntryMatch.new nil, flags, file
        end
      end

      until parts.empty?
        last.separator = parts.pop
        dir = parts.pop

        if dir == '**'
          if parts.empty?
            last = StartRecursiveDirectories.new last, flags
          else
            last = RecursiveDirectories.new last, flags
          end
        elsif NO_GLOB_META_CHARS.match?(dir)
          while NO_GLOB_META_CHARS.match?(parts[-2])
            next_sep = parts.pop
            next_sect = parts.pop

            dir = next_sect << next_sep << dir
          end

          last = ConstantDirectory.new last, flags, dir
        elsif !dir.empty?
          last = DirectoryMatch.new last, flags, dir
        end
      end

      if glob.getbyte(0) == 47  # ?/
        last = RootDirectory.new last, flags
      end

      last
    end

    def self.run(node, all_matches, glob_base_dir)
      if ConstantEntry === node
        node.call all_matches, nil, glob_base_dir
      else
        matches = []
        node.call matches, nil, glob_base_dir
        # Truffle: ensure glob'd files are always sorted in consistent order,
        # it avoids headaches due to platform differences (OS X is sorted, Linux not).
        matches.sort!
        all_matches.concat(matches)
      end
    end

    def self.path_join(parent, entry, separator = '/')
      return entry unless parent

      if parent == '/'
        "/#{entry}"
      else
        "#{parent}#{separator}#{entry}"
      end
    end

    def self.glob(base_dir, pattern, flags, matches)
      # Rubygems uses Dir[] as a glorified File.exist? to check for multiple
      # extensions. So we went ahead and sped up that specific case.

      if flags == 0 and m = TRAILING_BRACES.match(pattern)
        # no meta characters, so this is a glorified
        # File.exist? check. We allow for a brace expansion
        # only as a suffix.

        if braces = m[2]
          stem = m[1]

          braces.split(',').each do |s|
            path = "#{stem}#{s}"
            if Truffle::FileOperations.exist? path_join(base_dir, path)
              matches << path
            end
          end

          # Split strips an empty closing part, so we need to add it back in
          if braces.getbyte(-1) == 44 # ?,
            matches << stem if Truffle::FileOperations.exist? path_join(base_dir, stem)
          end
        else
          matches << pattern if Truffle::FileOperations.exist?(path_join(base_dir, pattern))
        end

        return matches
      end

      left_brace_index = pattern.index('{')
      if left_brace_index
        patterns = compile(pattern, left_brace_index, flags)

        patterns.each do |node|
          run node, matches, base_dir
        end
      elsif node = single_compile(pattern, flags)
        run node, matches, base_dir
      else
        matches
      end
    end

    def self.compile(pattern, left_brace_index, flags, patterns=[])
      escape = (flags & File::FNM_NOESCAPE) == 0

      lbrace = left_brace_index
      rbrace = nil

      i = left_brace_index

      # If there was a { found, then search
      if i
        nest = 0

        while i < pattern.size
          char = pattern[i]

          if char == '{'
            nest += 1
          elsif char == '}'
            nest -= 1

            if nest == 0
              rbrace = i
              break
            end
          elsif char == '\\' and escape
            i += 1
          end

          i += 1
        end
      end

      # There was a full {} expression detected, expand each part of it
      # recursively.
      if lbrace and rbrace
        pos = lbrace
        front = pattern[0...lbrace]
        back = pattern[(rbrace + 1)..-1]

        while pos < rbrace
          nest = 0
          pos += 1
          last = pos

          while pos < rbrace and not (pattern[pos] == ',' and nest == 0)
            char = pattern[pos]

            if char == '{'
              nest += 1
            elsif char == '}'
              nest -= 1
            elsif char == '\\' and escape
              pos += 1
              break if pos == rbrace
            end

            pos += 1
          end

          middle = pattern[last...pos]
          brace_pattern = "#{front}#{middle}#{back}"

          # The front part of the constructed string can't possibly have a '{' character, but the other parts might.
          # By searching each part rather than the constructed string, we can reduce the number of characters that
          # need to be checked.
          next_left_brace = middle.index('{')
          if next_left_brace
            next_left_brace += front.size
          else
            next_left_brace = back.index('{')
            if next_left_brace
              next_left_brace += front.size + middle.size
            end
          end

          compile brace_pattern, next_left_brace, flags, patterns
        end

        # No braces found, match the pattern normally
      else
        # Don't use .glob here because this code can detect properly
        # if a { is a brace or a just a normal character, but .glob can't.
        # if .glob is used and there is a { as a normal character, it will
        # recurse forever.
        if node = single_compile(pattern, flags)
          patterns << node
        end
      end

      patterns
    end
  end
end
