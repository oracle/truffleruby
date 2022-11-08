module RBS
  class CharScanner < StringScanner
    def initialize(string)
      super(string)
      @charpos = 0
    end

    alias original_charpos charpos

    def charpos
      @charpos
    end

    def scan(pattern)
      s = super
      @charpos += s.size if s
      s
    end
  end
end
