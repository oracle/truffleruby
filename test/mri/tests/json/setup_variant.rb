# frozen_string_literal: false
case ENV['JSON']
when 'pure'
  $:.unshift 'lib'
  require 'json/pure'
when 'ext'
  $:.unshift 'ext', 'lib'
  require 'json/ext'
else
  # $:.unshift 'ext', 'lib' # Modified for TruffleRuby, different directory layout
  require 'json'
end
