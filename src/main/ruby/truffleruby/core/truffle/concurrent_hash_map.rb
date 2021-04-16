# frozen_string_literal: true

module TruffleRuby
  class ConcurrentHashMap
    # To test Java init implementation
    # def initialize(options = nil)
    #   @hash = {}
    # end

    def initialize_copy(other)
      @hash = @hash.dup
      self
    end

    def [](key)
      @hash[key]
    end

    def []=(key, value)
      TruffleRuby.synchronized(self) do
        @hash[key] = value
      end
    end

    def compute_if_absent(key)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key)
          @hash[key]
        else
          @hash[key] = yield
        end
      end
    end

    def compute_if_present(key)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key)
          store_computed_value(key, yield(@hash[key]))
        end
      end
    end

    def compute(key)
      TruffleRuby.synchronized(self) do
        store_computed_value(key, yield(@hash[key]))
      end
    end

    def merge_pair(key, value)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key)
          store_computed_value(key, yield(@hash[key]))
        else
          @hash[key] = value
        end
      end
    end

    def replace_pair(key, old_value, new_value)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key) && @hash[key] == old_value
          @hash[key] = new_value
          return true
        end
        false
      end
    end

    def replace_if_exists(key, new_value)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key)
          old_value = @hash[key]
          @hash[key] = new_value
          old_value
        end
      end
    end

    def get_and_set(key, value)
      TruffleRuby.synchronized(self) do
        old_value = @hash[key]
        @hash[key] = value
        old_value
      end
    end

    def key?(key)
      @hash.key?(key)
    end

    def delete(key)
      TruffleRuby.synchronized(self) do
        @hash.delete(key)
      end
    end

    def delete_pair(key, value)
      TruffleRuby.synchronized(self) do
        if @hash.key?(key) && @hash[key] == value
          @hash.delete(key)
          return true
        end
        false
      end
    end

    def clear
      TruffleRuby.synchronized(self) do
        @hash.clear
        self
      end
    end

    def size
      @hash.size
    end

    def get_or_default(key, default_value)
      @hash.fetch(key, default_value)
    end

    def each_pair
      @hash.each_pair do |key, value|
        yield(key, value)
      end
      self
    end

    private

    def store_computed_value(key, new_value)
      if new_value.nil?
        @hash.delete(key)
        nil
      else
        @hash[key] = new_value
      end
    end
  end
end
