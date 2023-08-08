require 'fileutils'
require 'rbconfig'

repo = ARGV.fetch(0)
src = "#{repo}/exe/rake"
raise src unless File.exist? src

FileUtils::Verbose.cp(src, "#{RbConfig::CONFIG['bindir']}/rake")
