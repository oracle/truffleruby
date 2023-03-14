# -*- coding: us-ascii -*-
# frozen_string_literal: true
require 'mkmf'
require 'fileutils'

# :stopdoc:

if defined?(::TruffleRuby)
  require 'truffle/libyaml-prefix'
  dir_config('libyaml', ENV['LIBYAML_PREFIX'])
else
  dir_config 'libyaml'
end

if defined?(::TruffleRuby)
  # From https://github.com/ruby/psych/blob/v5.0.0/ext/psych/extconf.rb#L42-L43
  find_header('yaml.h') or abort "yaml.h not found"
  find_library('yaml', 'yaml_get_version') or abort "libyaml not found"
else
if enable_config("bundled-libyaml", false) || !(find_header('yaml.h') && find_library('yaml', 'yaml_get_version'))
  # Embed libyaml since we could not find it.

  $VPATH << "$(srcdir)/yaml"
  $INCFLAGS << " -I$(srcdir)/yaml"

  $srcs = Dir.glob("#{$srcdir}/{,yaml/}*.c").map {|n| File.basename(n)}.sort

  header = 'yaml/yaml.h'
  header = "{$(VPATH)}#{header}" if $nmake
  if have_macro("_WIN32")
    $CPPFLAGS << " -DYAML_DECLARE_STATIC -DHAVE_CONFIG_H"
  end

  have_header 'dlfcn.h'
  have_header 'inttypes.h'
  have_header 'memory.h'
  have_header 'stdint.h'
  have_header 'stdlib.h'
  have_header 'strings.h'
  have_header 'string.h'
  have_header 'sys/stat.h'
  have_header 'sys/types.h'
  have_header 'unistd.h'

  find_header 'yaml.h'
  have_header 'config.h'
end
end

create_makefile 'psych' do |mk|
  mk << "YAML_H = #{header}".strip << "\n"
end

# :startdoc:
