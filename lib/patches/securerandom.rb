# truffleruby_primitives: true

require 'securerandom'

module SecureRandom
  @randomizer = Truffle::SecureRandomizer.new

  def self.gen_random(n)
    Primitive.vm_dev_urandom_bytes(n)
  end
end
