# frozen_string_literal: true

require_relative 'performance/shopify/liquid'

cart_template_source = File.read("#{__dir__}/performance/tests/dropify/cart.liquid")

dev_null = File.open('/dev/null', 'w')

benchmark do
  input = rand(256)
  output = Liquid::Template.new.parse(cart_template_source + input.to_s)
  dev_null.write output
end
