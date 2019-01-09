# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'patches/json_patches'
require_relative 'patches/nokogiri_patches'
require_relative 'patches/pg_patches'

class Preprocessor

  PATCHED_FILES = {}

  def self.add_gem_patches(patch_hash, gem_patches)
    gem = gem_patches[:gem]
    patch_list = gem_patches[:patches]
    patch_list.each do |path_parts, patch|
      processed_patch = {}
      if path_parts.kind_of?(String)
        key = path_parts
      else
        key = path_parts.last
        processed_patch[:ext_dir] = path_parts.first if path_parts.size > 1
      end
      processed_patch[:patches] = patch
      processed_patch[:gem] = gem
      raise "Duplicate patch file #{key}." if patch_hash.include?(key)
      patch_hash[key] = processed_patch
    end
  end

  add_gem_patches(PATCHED_FILES, ::JsonPatches::PATCHES)
  add_gem_patches(PATCHED_FILES, ::NokogiriPatches::PATCHES)
  add_gem_patches(PATCHED_FILES, ::PgPatches::PATCHES)

  LOCAL = /\w+\s*(\[\s*\d+\s*\])?/
  VALUE_LOCALS = /^(?<before>\s+)VALUE\s+(?<locals>#{LOCAL}(\s*,\s*#{LOCAL})*);(?<after>\s*(\/\/.+)?)$/

  ALLOCA_LOCALS = /^(?<before>\s+)VALUE\s*\*\s*(?<var>[a-z_][a-zA-Z_0-9]*)\s*=\s*(\(\s*VALUE\s*\*\s*\)\s*)?alloca\(/

  def self.patch(file, contents, directory)
    if patched_file = PATCHED_FILES[File.basename(file)]
      matched = if patched_file[:ext_dir]
                  directory.end_with?(File.join(patched_file[:gem], 'ext', patched_file[:ext_dir]))
                else
                  regexp = /^#{Regexp.escape(patched_file[:gem])}\b/
                  directory.split('/').last(3).any? { |part| part =~ regexp } || file.split('/').last(2).any? { |part| part =~ regexp }
                end
      if matched
        patched_file[:patches].each do |patch|
          contents = contents.gsub(patch[:match], patch[:replacement].rstrip)
        end
      end
    end
    contents
  end

  if __FILE__ == $0
    require 'stringio'

    file_name = ARGF.filename
    original_content = File.read(file_name)
    output = patch(file_name, original_content, Dir.pwd)

    if ENV['PREPROCESS_DEBUG'] && original_content != output
      patched_file_name = "#{File.dirname file_name}/.#{File.basename file_name, '.*'}.patched#{File.extname file_name}"
      File.write patched_file_name, output
      $stderr.print `git diff --no-index --color -- #{file_name} #{patched_file_name}`
      file_name = patched_file_name
    end

    $stdout.puts "#line 1 \"#{file_name}\""
    $stdout.puts output
  end
end
