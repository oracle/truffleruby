# frozen_string_literal: true

$LOAD_PATH.unshift "#{__dir__}/lib"
require 'liquid'

template = Liquid::Template.parse(<<LIQUID)
  <ul id="products">
    {% for product in products %}
      <li>
        <h2>{{ product.name }}</h2>
        Only {{ product.price | price }}

        {{ product.description | prettyprint | paragraph }}
      </li>
    {% endfor %}
  </ul>
LIQUID

dev_null = File.open('/dev/null', 'w')

benchmark do
  output = template.render!('products' => [
    { 'name' => 'a', 'price' => 1.1, 'description' => 'desc a' },
    { 'name' => 'b', 'price' => 2.2, 'description' => 'desc b' },
    { 'name' => 'c', 'price' => 3.3, 'description' => 'desc c' }
  ])
  dev_null.write output
end
