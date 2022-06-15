# Compiler Graphing

## Seafoam

The [Seafoam](https://github.com/Shopify/seafoam) tool is used to generate compiler graphs.

### Using `jt graph` to generate a graph

#### Steps

1. Create a file `sample.rb` with the code to graph inside a method named `foo`:

```ruby
def foo
  3 + 4
end

loop { foo }
```

2. Run the command `jt -u jvm-ce graph sample.rb` and view the resulting graph pdf.

#### Options

Use `jt help` to view `jt graph` options.
