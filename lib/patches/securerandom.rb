# truffleruby_primitives: true

require Primitive.get_original_require(__FILE__)

module SecureRandom
  @randomizer = Truffle::SecureRandomizer.new

  def self.gen_random(n)
    Primitive.vm_dev_urandom_bytes(n)
  end
end
