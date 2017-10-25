require_relative File.expand_path('../lib/optcarrot', __FILE__)

# Accomodates Rubinius
require_relative 'tools/shim' if RUBY_ENGINE == 'rbx'

rom = File.expand_path('../examples/Lan_Master.nes', __FILE__)
nes = Optcarrot::NES.new ['--headless', rom]
nes.reset

benchmark do
  nes.step
end
