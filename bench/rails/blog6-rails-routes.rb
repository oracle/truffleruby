# This is a long running benchmark and should
# be run using fixed iterations. E.g.
# jt benchmark bench/rails/blog6-rails-routes.rb --elapsed --iterations --ips --fixed-iterations 1
# RUBY_BENCHMARKS=true jt mx benchmark ecosystem
#

require_relative './blog6-setup.rb'
require_relative '../../tool/jt.rb'

Blog6Setup.setup_bundler
Blog6Setup.bundle_install

benchmark 'blog6-rails-routes' do
  Dir.chdir(Blog6Setup::BLOG6_DIR) do
    JT.ruby(*%w[-S bin/rails routes])
  end
end
