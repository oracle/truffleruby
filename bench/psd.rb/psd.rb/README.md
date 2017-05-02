# PSD.rb

[![Travis CI](https://travis-ci.org/layervault/psd.rb.png?branch=master)](https://travis-ci.org/layervault/psd.rb)

A general purpose Photoshop file parser written in Ruby. It allows you to work with a Photoshop document in a manageable tree structure and find out important data such as:

* Document structure
* Document size
* Layer/folder size + positioning
* Layer/folder names
* Layer/folder visibility and opacity
* Font data (via [psd-enginedata](https://github.com/layervault/psd-enginedata))
  * Text area contents
  * Font names, sizes, and colors
* Color mode and bit-depth
* Vector mask data
* Flattened image data
* Layer comps

PSD.rb is tested against:

* MRI 1.9.3 & 2.0.0
* JRuby (1.9.3 mode)
* Rubinius (1.9.3 mode)

If you use MRI Ruby and are interested in significantly speeding up PSD.rb with native code, check out [psd_native](https://github.com/layervault/psd_native).

## Installation

Add this line to your application's Gemfile:

    gem 'psd'

And then execute:

    $ bundle

Or install it yourself as:

    $ gem install psd

## Usage

The [full source code documentation](http://rubydoc.info/gems/psd/frames) is available, but here are some common ways to use and access the PSD data:

### Loading a PSD

``` ruby
require 'psd'

psd = PSD.new('/path/to/file.psd')
psd.parse!
```

Or, if you prefer the File.open way of doing things, you can do that too.

``` ruby
require 'psd'

PSD.open('path/to/file.psd') do |psd|
  p psd.tree.to_hash
end
```

As you can see, `open` calls `parse!` for you, so that you can get down to business right away.

If you happen to prefer things DSL-style, the `open` method will also let you operate on the PSD object directly. Again, the call to `parse!` is handled for you.

``` ruby
require 'psd'

PSD.open('path/to/file.psd') do
  p tree.to_hash
end
```

### Traversing the Document

To access the document as a tree structure, use `psd.tree` to get the root node. From there, you can traverse the tree using any of these methods:

* `root`: get the root node from anywhere in the tree
* `children`: get all immediate children of the node
* `ancestors`: get all ancestors in the path of this node (excluding the root)
* `siblings`: get all sibling tree nodes including the current one (e.g. all layers in a folder)
* `descendants`: get all descendant nodes not including the current one
* `subtree`: same as descendants but starts with the current node
* `depth`: calculate the depth of the current node

For any of the traversal methods, you can also retrieve folder or layer nodes only by appending `_layers` or `_groups` to the method. For example:

``` ruby
psd.tree.descendant_layers
```

If you know the path to a group or layer within the tree, you can search by that path. Note that this always returns an Array because layer/group names do not have to be unique.

``` ruby
psd.tree.children_at_path("Version A/Matte")
```

### Layer Comps

You can also filter nodes based on a layer comp. To generate a new tree with layer visibility and position set according to the layer comp data:

``` ruby
# Get information about all the available layer comps
puts psd.layer_comps

# Can filter by name or by ID (obtained from above)
tree = psd.tree.filter_by_comp('Version A')
puts tree.children.map(&:name)
```

This returns a new node tree and does not alter the original so you won't lose any data.

### Accessing Layer Data

To get data such as the name or dimensions of a layer:

``` ruby
psd.tree.descendant_layers.first.name
psd.tree.descendant_layers.first.width
```

PSD files also store various pieces of information in "layer info" blocks. Which blocks a layer has varies from layer-to-layer, but to access them you can do:

``` ruby
psd.tree.descendant_layers.first.text[:font]

# Returns
{:name=>"HelveticaNeue-Light",
 :sizes=>[33.0],
 :colors=>[[255, 19, 120, 98]],
 :css=>
  "font-family: \"HelveticaNeue-Light\", \"AdobeInvisFont\", \"MyriadPro-Regular\";\nfont-size: 33.0pt;\ncolor: rgba(19, 120, 98, 255);"}
```

### Exporting Data

When working with the tree structure, you can recursively export any node to a Hash.

``` ruby
pp psd.tree.to_hash
```

Which produces something like:

``` ruby
{:children=>
  [{:type=>:group,
    :visible=>false,
    :opacity=>1.0,
    :blending_mode=>"normal",
    :name=>"Version D",
    :left=>0,
    :right=>900,
    :top=>0,
    :bottom=>600,
    :height=>900,
    :width=>600,
    :children=>
     [{:type=>:layer,
       :visible=>true,
       :opacity=>1.0,
       :blending_mode=>"normal",
       :name=>"Make a change and save.",
       :left=>275,
       :right=>636,
       :top=>435,
       :bottom=>466,
       :height=>31,
       :width=>361,
       :text=>
        {:value=>"Make a change and save.",
         :font=>
          {:name=>"HelveticaNeue-Light",
           :sizes=>[33.0],
           :colors=>[[255, 19, 120, 98]],
           :css=>
            "font-family: \"HelveticaNeue-Light\", \"AdobeInvisFont\", \"MyriadPro-Regular\";\nfont-size: 33.0pt;\ncolor: rgba(19, 120, 98, 255);"},
         :left=>0,
         :top=>0,
         :right=>0,
         :bottom=>0,
         :transform=>
          {:xx=>1.0, :xy=>0.0, :yx=>0.0, :yy=>1.0, :tx=>456.0, :ty=>459.0}},
       :ref_x=>264.0,
       :ref_y=>-3.0}]
  }],
:document=>{:width=>900, :height=>600}}
```

You can also export the PSD to a flattened image. Please note that, at this time, not all image modes + depths are supported.

``` ruby
png = psd.image.to_png # reference to PNG data
psd.image.save_as_png 'path/to/output.png' # writes PNG to disk
```

### Debugging

If you run into any problems parsing a PSD, you can enable debug logging via the `PSD_DEBUG` environment variable. For example:

``` bash
PSD_DEBUG=true bundle exec examples/parse.rb
```

You can also give a path to a file instead. If you need to enable debugging programatically:

``` ruby
PSD.debug = true
```

### Preview Building

**This is currently an experimental feature. It works "well enough" but is not perfect yet.**

You can build previews of any subset or version of the PSD document. This is useful for generating previews of layer comps or exporting individual layer groups as images.

``` ruby
# Save a layer comp
psd.tree.filter_by_comp("Version A").save_as_png('./Version A.png')

# Generate PNG of individual layer group
psd.tree.children_at_path("Group 1").first.to_png
```

## To-do

There are a few features that are currently missing from PSD.rb.

* More image modes + depths for image exporting
* A few layer info blocks
* Support for rendering all layer styles
* Render engine fixes for groups with lowered opacity
