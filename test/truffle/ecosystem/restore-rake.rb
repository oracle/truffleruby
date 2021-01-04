require 'fileutils'

repo = ARGV.fetch(0)
src = "#{repo}/exe/rake"
raise src unless File.exist? src

bin_dirs = [RbConfig::CONFIG['bindir'], *RbConfig::CONFIG['extra_bindirs'].split(File::PATH_SEPARATOR)]
bin_dirs.each do |dir|
  FileUtils::Verbose.cp(src, "#{dir}/rake")
end
