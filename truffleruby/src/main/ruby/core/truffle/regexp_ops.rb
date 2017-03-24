module Truffle
  class RegexpOps
    def self.match(re, str, pos=0)
      return nil unless str

      str = str.to_s if str.is_a?(Symbol)
      str = StringValue(str)

      m = Rubinius::Mirror.reflect str
      pos = pos < 0 ? pos + str.size : pos
      pos = m.character_to_byte_index pos
      re.search_region(str, pos, str.bytesize, true)
    end
  end
end
