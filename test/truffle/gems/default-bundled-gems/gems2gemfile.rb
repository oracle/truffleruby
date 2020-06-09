require 'rbconfig'
require 'json'

gems = []

`#{RbConfig.ruby} -S gem list`.lines.map(&:chomp).reject { |line|
  line.empty? or line.include?('LOCAL GEMS')
}.each { |line|
  gem, versions = line.split(' (', 2)
  versions = versions.chomp(')')
  versions = versions.split(', ')

  versions.each { |version|
    if version.include?('default: ')
      gems << [gem, version.sub('default: ', '')]
    end
  }
}

bundled_gems = JSON.load(File.read(File.expand_path('../' * 5 + 'versions.json', __FILE__ )))['gems']['bundled']
gems += bundled_gems.to_a

File.write 'Gemfile', <<GEMFILE
source 'https://rubygems.org'

#{gems.map { |name, version| "gem #{name.inspect}, #{version.inspect}" }.join("\n")}
GEMFILE
