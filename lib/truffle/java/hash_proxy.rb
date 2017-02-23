class HashProxy
  include java.util.Map

  attr_reader :a_hash

  def initialize(hash)
    @a_hash = hash
  end

  def get(key)
    a_hash[key]
  end

  def put(key, val)
    a_hash[key] = val
  end

  def containsKey(key)
    a_hash.has_key?(key)
  end

  def size
    a_hash.size
  end
end
