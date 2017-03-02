Truffle::System.log :PATCH, 'applying nokogiri-stubs'

dir = File.join File.dirname(__FILE__),
                File.basename(__FILE__, '.*')
$LOAD_PATH.unshift(dir)
require "#{dir}/nokogiri"
