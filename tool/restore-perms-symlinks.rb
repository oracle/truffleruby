require 'fileutils'

dir = ARGV.first
Dir.chdir(dir)

File.readlines('META-INF/symlinks').map(&:chomp).map { |spec|
  spec.split(' = ', 2)
}.each { |link, target|
  FileUtils::Verbose.mkdir_p File.dirname(link)
  FileUtils::Verbose.ln_s target, link
}

File.readlines('META-INF/permissions').map(&:chomp).map { |spec|
  spec.split(' = ', 2)
}.each { |file, perms|
  raise perms unless perms.size == 9
  u, g, o = perms.chars.each_slice(3).map { |e| e.join.tr('-', '') }
  FileUtils::Verbose.chmod("u=#{u},g=#{g},o=#{o}", file)
}
