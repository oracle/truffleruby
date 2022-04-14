# frozen_string_literal: true

require_relative 'performance/shopify/liquid'

template_files = %w[
  ./performance/tests/tribble/404.liquid
  ./performance/tests/tribble/collection.liquid
  ./performance/tests/tribble/theme.liquid
  ./performance/tests/tribble/index.liquid
  ./performance/tests/tribble/page.liquid
  ./performance/tests/tribble/blog.liquid
  ./performance/tests/tribble/product.liquid
  ./performance/tests/tribble/cart.liquid
  ./performance/tests/tribble/article.liquid
  ./performance/tests/tribble/search.liquid
  ./performance/tests/dropify/collection.liquid
  ./performance/tests/dropify/theme.liquid
  ./performance/tests/dropify/index.liquid
  ./performance/tests/dropify/page.liquid
  ./performance/tests/dropify/blog.liquid
  ./performance/tests/dropify/product.liquid
  ./performance/tests/dropify/cart.liquid
  ./performance/tests/dropify/article.liquid
  ./performance/tests/ripen/collection.liquid
  ./performance/tests/ripen/theme.liquid
  ./performance/tests/ripen/index.liquid
  ./performance/tests/ripen/page.liquid
  ./performance/tests/ripen/blog.liquid
  ./performance/tests/ripen/product.liquid
  ./performance/tests/ripen/cart.liquid
  ./performance/tests/ripen/article.liquid
  ./performance/tests/vogue/collection.liquid
  ./performance/tests/vogue/theme.liquid
  ./performance/tests/vogue/index.liquid
  ./performance/tests/vogue/page.liquid
  ./performance/tests/vogue/blog.liquid
  ./performance/tests/vogue/product.liquid
  ./performance/tests/vogue/cart.liquid
  ./performance/tests/vogue/article.liquid].freeze

template_source = template_files.map { |f| File.read("#{__dir__}/#{f}") }

benchmark do
  template_source.each do |t|
    result = Liquid::Template.new.parse(t)
    raise StandardError, "Parsing resulted in unexpected result" unless result.root.nodelist.size > 0
  end
end
