# frozen_string_literal: true

require_relative 'performance/shopify/liquid'
require_relative 'performance/shopify/database'

cart_template = Liquid::Template.new.parse(File.read("#{__dir__}/performance/tests/dropify/cart.liquid"))
theme_template = Liquid::Template.new.parse(File.read("#{__dir__}/performance/tests/dropify/theme.liquid"))

assigns = Database.tables
assigns['page_title'] = 'Page title'

dev_null = File.open('/dev/null', 'w')

benchmark do
  input = rand(256)
  assigns['x'] = input
  assigns['content_for_layout'] = cart_template.render!(assigns)
  output = theme_template.render!(assigns)
  dev_null.write output
end
