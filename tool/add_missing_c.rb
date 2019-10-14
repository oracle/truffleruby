# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# This tool is designed to search the truffleruby C headers for function definitions which
# have not been defined in the ruby.c file. If a definition is missing, it will print the 
# corresponding C definition found in the MRI sources with an error message as the body of
# the function.

# This is assumed to be run from the truffleruby project directory 
# with the MRI project as a sibling "../ruby"
# ruby tool/add_missing_c.rb

header_files = Dir.glob("lib/cext/include/**/*.h")
header_files.reject! { |h| h.include?("truffle") }
ruby_path = File.expand_path("../ruby")
ruby_c_files = Dir.glob("#{ruby_path}/**/*.c")

header_definitions = []
excludes = [
            "rb_ia64_flushrs",            # Not found in MRI c
            "rb_enc_casefold",            # Not found in MRI c
            "rb_interned_id_p",           # Not found in MRI c
            "rb_sym_interned_p",          # Not found in MRI c
            "rb_random_int",              # Not found in MRI c
            "rb_trap_exec",               # Not found in MRI c
            "rb_str_buf_new2",            # Defined by macro definition
            "rb_str_buf_cat2",            # Defined by macro definition
            "rb_str_dup_frozen",          # Defined by macro definition
            "rb_hash_uint32",             # Defined by macro definition
            "rb_hash_uint",               # Defined by macro definition
            "rb_hash_end",                # Defined by macro definition
            "rb_check_safe_str",          # Defined by macro definition
            "rb_w32_times",               # Skip w32 functions
            "rb_w32_read",                # Skip w32 functions
            "rb_w32_write",               # Skip w32 functions
            "rb_w32_write_console",       # Skip w32 functions
            "rb_w32_asynchronize",        # Skip w32 functions
            "rb_w32_pow",                 # Skip w32 functions
            "rb_scan_args",               # TruffleRuby defines this as a macro
            "rb_funcall",                 # TruffleRuby defines this as a macro
            "rb_enumeratorize_with_size", # Compilation error when defined
          ]

header_files.each do |f|
    File.foreach(f) do |line|
      m = line.match(/^(?!#define )\b(\w+) (\w+)\(/)
      if m && m[2].start_with?("rb_") && !excludes.include?(m[2])
        header_definitions << "#{m[1]} #{m[2]}"
      end
    end
end

found_definitions = []
File.foreach("src/main/c/cext/ruby.c") do |line|
  m = line.match(/\b(\w+) (\w+)\((.*?)\)/)
  if m
    found_definitions << "#{m[1]} #{m[2]}"
  end
end

ruby_c_definitions = {}
ruby_c_files.each do |f|
    data = IO.binread(f)
    data.scan(/\b(\w+)\n(\w+)\(([^;]+?)\)\n/m) do |x,y,z|
        ruby_c_definitions["#{x} #{y}"] = {:full => "#{x} #{y}(#{z})", :name => y}        
    end
    # encoding.c defines a few functions differently
    data.scan(/\b(\w+) (\w+)\((.*?)\) {/) do |x,y,z|
      ruby_c_definitions["#{x} #{y}"] = {:full => "#{x} #{y}(#{z})", :name => y}        
    end
end

missing = header_definitions.select { |d| !found_definitions.include?(d) }
missing.uniq!

# Some C sources are defined using macros then undefined just before C definition
prepend_undef = ["rb_enc_str_new_cstr","rb_enc_code_to_mbclen","rb_utf8_str_new",
                 "rb_utf8_str_new_cstr","rb_str_cat_cstr"]

missing.each do |m|
  puts ""
  if prepend_undef.include?(ruby_c_definitions[m][:name])
    puts "#undef #{ruby_c_definitions[m][:name]}"
  end
  definition = ruby_c_definitions[m][:full]
  definition.gsub!(/[\n\t]/,' ')
  definition = definition.squeeze(" ")
  puts "#{definition} {"
  puts "  rb_tr_error(\"#{ruby_c_definitions[m][:name]} not implemented\");"
  puts "}"
end
