# frozen_string_literal: true

module TruffleRuby
  class ConcurrentHashMap
    # To test Java init implementation
    # def initialize(options = nil)
    #   get_internal_hash = {}
    # end

    def initialize_copy(other)
      get_internal_hash.replace other.get_internal_hash
      self
    end

    def [](key)
      get_internal_hash[key]
    end

    def []=(key, value)
      TruffleRuby.synchronized(self) do
        get_internal_hash[key] = value
      end
    end

    def compute_if_absent(key)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key)
          get_internal_hash[key]
        else
          get_internal_hash[key] = yield
        end
      end
    end

    def compute_if_present(key)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key)
          store_computed_value(key, yield(get_internal_hash[key]))
        end
      end
    end

    def compute(key)
      TruffleRuby.synchronized(self) do
        store_computed_value(key, yield(get_internal_hash[key]))
      end
    end

    def merge_pair(key, value)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key)
          store_computed_value(key, yield(get_internal_hash[key]))
        else
          get_internal_hash[key] = value
        end
      end
    end

    def replace_pair(key, old_value, new_value)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key) && get_internal_hash[key] == old_value
          get_internal_hash[key] = new_value
          return true
        end
        false
      end
    end

    def replace_if_exists(key, new_value)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key)
          old_value = get_internal_hash[key]
          get_internal_hash[key] = new_value
          old_value
        end
      end
    end

    def get_and_set(key, value)
      TruffleRuby.synchronized(self) do
        old_value = get_internal_hash[key]
        get_internal_hash[key] = value
        old_value
      end
    end

    def key?(key)
      get_internal_hash.key?(key)
    end

    def delete(key)
      TruffleRuby.synchronized(self) do
        get_internal_hash.delete(key)
      end
    end

    def delete_pair(key, value)
      TruffleRuby.synchronized(self) do
        if get_internal_hash.key?(key) && get_internal_hash[key] == value
          get_internal_hash.delete(key)
          return true
        end
        false
      end
    end

    def clear
      TruffleRuby.synchronized(self) do
        get_internal_hash.clear
        self
      end
    end

    def size
      get_internal_hash.size
    end

    def get_or_default(key, default_value)
      get_internal_hash.fetch(key, default_value)
    end

    def each_pair
      get_internal_hash.each_pair do |key, value|
        yield(key, value)
      end
      self
    end

    private

    def store_computed_value(key, new_value)
      if new_value.nil?
        get_internal_hash.delete(key)
        nil
      else
        get_internal_hash[key] = new_value
      end
    end
  end
end
