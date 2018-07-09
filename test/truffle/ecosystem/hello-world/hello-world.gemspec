Gem::Specification.new do |spec|
  spec.name    = 'hello-world'
  spec.version = '0.0.1'
  spec.author  = 'TruffleRuby'
  spec.summary = 'A gem which prints Hello World!'
  # spec.description   = %q{TODO: Write a longer description or delete this line.}
  spec.files         = [__FILE__]
  spec.bindir        = 'bin'
  spec.executables   = 'hello-world.rb'
  spec.require_paths = ['lib']
end
