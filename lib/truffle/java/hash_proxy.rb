class HashProxy
  include java.util.Map

  class HashEntry
    include java.util.Map::Entry

    attr_reader :key
    attr_reader :owner

    def initialize(a_key, an_owner)
      @key = a_key
      @owner = an_owner
    end

    def equals(another)
      equal?(another)
    end

    def getKey
      key
    end

    def getValue
      owner[key]
    end

    def hashCode
      hash
    end

    def setValue(a_value)
      old = owner[key]
      owner[key] = a_value
      old
    end
  end

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

  def entrySet
    s = java.util.HashSet.new
    a_hash.each { |k, v| s.add HashEntry.new(k, self) }
    s
  end
end
