# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative 'common_patches'

require_relative 'byebug_patches'
require_relative 'json_patches'
require_relative 'mysql2_patches'
require_relative 'nio4r_patches'
require_relative 'nokogiri_patches'
require_relative 'pg_patches'
require_relative 'puma_patches'
require_relative 'sqlite3_patches'
require_relative 'websocket_driver_patches'

class Preprocessor < CommonPatches

  LOCAL = /\w+\s*(\[\s*\d+\s*\])?/
  VALUE_LOCALS = /^(\s+)VALUE\s+(#{LOCAL}(\s*,\s*#{LOCAL})*);\s*$/

  ALLOCA_LOCALS = /^(\s+)VALUE\s*\*\s*([a-z_][a-zA-Z_0-9]*)\s*=\s*(\(\s*VALUE\s*\*\s*\)\s*)?alloca\(/

  PATCHED_FILES = {}

  PATCHED_FILES.merge!(::NokogiriPatches::PATCHES)
  PATCHED_FILES.merge!(::PgPatches::PATCHES)
  PATCHED_FILES.merge!(::Nio4RPatches::PATCHES)
  PATCHED_FILES.merge!(::JsonPatches::PATCHES)  
  PATCHED_FILES.merge!(::MySQL2Patches::PATCHES)  
  PATCHED_FILES.merge!(::ByeBugPatches::PATCHES)  
  PATCHED_FILES.merge!(::PumaPatches::PATCHES)  
  PATCHED_FILES.merge!(::SQLite3Patches::PATCHES)  
  PATCHED_FILES.merge!(::WebsocketDriverPatches::PATCHES)  

  def self.preprocess(line)
    if line =~ VALUE_LOCALS
      # Translate
      #   VALUE args[6], failed, a1, a2, a3, a4, a5, a6;
      #  into
      #   VALUE failed, a1, a2, a3, a4, a5, a6; VALUE *args = truffle_managed_malloc(6 * sizeof(VALUE));

      simple = []
      arrays = []

      line = $1

      $2.split(',').each do |local|
        local.strip!
        if local.end_with?(']')
          raise unless local =~ /(\w+)\s*\[\s*(\d+)\s*\]/
          arrays << [$1, $2.to_i]
        else
          simple << local
        end
      end

      unless simple.empty?
        line += "VALUE #{simple.join(', ')};"
      end

      arrays.each do |name, size|
        line += " VALUE *#{name} = truffle_managed_malloc(#{size} * sizeof(VALUE));"
      end
      line
    elsif line =~ ALLOCA_LOCALS
      # Translate
      #   VALUE *argv = alloca(sizeof(VALUE) * argc);
      # into
      #   VALUE *argv = (VALUE *)truffle_managed_malloc(sizeof(VALUE) * argc);

      line = "#{$1}VALUE *#{$2} = truffle_managed_malloc(#{$'}"
    else
      line
    end
  end

  def self.patch(file, contents, directory)
    if patched_file = PATCHED_FILES[File.basename(file)]
      matched = if patched_file[:ext_dir]
                  directory.end_with?(File.join(patched_file[:gem], 'ext', patched_file[:ext_dir]))
                else
                  regexp = /^#{Regexp.escape(patched_file[:gem])}\b/
                  directory.split('/').last(2).any? { |part| part =~ regexp } || file.split('/').last(2).any? { |part| part =~ regexp }
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
    puts "#line 1 \"#{ARGF.filename}\""

    contents = patch(ARGF.filename, File.read(ARGF.filename), Dir.pwd)
    contents.each_line do |line|
      puts preprocess(line)
    end
  end
end

