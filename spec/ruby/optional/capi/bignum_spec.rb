require File.expand_path('../spec_helper', __FILE__)

load_extension("bignum")

def ensure_bignum(n)
  raise "Bignum#coerce returned Fixnum" if fixnum_min <= n && n <= fixnum_max
  n
end

full_range_longs = (fixnum_max == 2**(0.size * 8 - 1) - 1)

describe "CApiBignumSpecs" do
  before :each do
    @s = CApiBignumSpecs.new

    if full_range_longs
      @max_long = 2**(0.size * 8 - 1) - 1
      @min_long = -@max_long - 1
      @max_ulong = ensure_bignum(2**(0.size * 8) - 1)
    else
      @max_long = ensure_bignum(2**(0.size * 8 - 1) - 1)
      @min_long = ensure_bignum(-@max_long - 1)
      @max_ulong = ensure_bignum(2**(0.size * 8) - 1)
    end
end

  describe "rb_big2long" do
    unless full_range_longs
      it "converts a Bignum" do
        @s.rb_big2long(@max_long).should == @max_long
        @s.rb_big2long(@min_long).should == @min_long
      end
    end

    it "raises RangeError if passed Bignum overflow long" do
      lambda { @s.rb_big2long(ensure_bignum(@max_long + 1)) }.should raise_error(RangeError)
      lambda { @s.rb_big2long(ensure_bignum(@min_long - 1)) }.should raise_error(RangeError)
    end
  end

  describe "rb_big2ll" do
    unless full_range_longs
      it "converts a Bignum" do
        @s.rb_big2ll(@max_long).should == @max_long
        @s.rb_big2ll(@min_long).should == @min_long
      end
    end

    it "raises RangeError if passed Bignum overflow long" do
      lambda { @s.rb_big2ll(ensure_bignum(@max_long << 40)) }.should raise_error(RangeError)
      lambda { @s.rb_big2ll(ensure_bignum(@min_long << 40)) }.should raise_error(RangeError)
    end
  end

  describe "rb_big2ulong" do
    it "converts a Bignum" do
      @s.rb_big2ulong(@max_ulong).should == @max_ulong
    end

    unless full_range_longs
      it "wraps around if passed a negative bignum" do
        @s.rb_big2ulong(ensure_bignum(@min_long + 1)).should == -(@min_long - 1)
      end
    end

    it "raises RangeError if passed Bignum overflow long" do
      lambda { @s.rb_big2ulong(ensure_bignum(@max_ulong + 1)) }.should raise_error(RangeError)
      lambda { @s.rb_big2ulong(ensure_bignum(@min_long - 1)) }.should raise_error(RangeError)
    end

    unless full_range_longs
      it "wraps around if passed a negative bignum" do
        @s.rb_big2ulong(ensure_bignum(@min_long)).should == -(@min_long)
      end
    end
  end

  describe "rb_big2dbl" do
    it "converts a Bignum to a double value" do
      @s.rb_big2dbl(ensure_bignum(Float::MAX.to_i)).eql?(Float::MAX).should == true
    end

    it "returns Infinity if the number is too big for a double" do
      huge_bignum = ensure_bignum(Float::MAX.to_i * 2)
      @s.rb_big2dbl(huge_bignum).should == infinity_value
    end

    it "returns -Infinity if the number is negative and too big for a double" do
      huge_bignum = -ensure_bignum(Float::MAX.to_i * 2)
      @s.rb_big2dbl(huge_bignum).should == -infinity_value
    end
  end

  describe "rb_big2str" do

    it "converts a Bignum to a string with base 10" do
      @s.rb_big2str(ensure_bignum(2**70), 10).eql?("1180591620717411303424").should == true
    end

    it "converts a Bignum to a string with a different base" do
      @s.rb_big2str(ensure_bignum(2**70), 16).eql?("400000000000000000").should == true
    end
  end

  describe "rb_big_cmp" do
    it "compares a Bignum with a Bignum" do
      @s.rb_big_cmp(bignum_value, bignum_value(1)).should == -1
    end

    it "compares a Bignum with a Fixnum" do
      @s.rb_big_cmp(bignum_value, 5).should == 1
    end
  end

  describe "rb_big_pack" do
    it "packs a Bignum into an unsigned long" do
      val = @s.rb_big_pack(@max_ulong)
      val.should == @max_ulong
    end
  end

  describe "rb_dbl2big" do
    it "returns a Fixnum for a Fixnum input value" do
      val = @s.rb_dbl2big(2)

      val.kind_of?(Fixnum).should == true
      val.should == 2
    end

    it "returns a Fixnum for a Float input value" do
      val = @s.rb_dbl2big(2.5)

      val.kind_of?(Fixnum).should == true
      val.should == 2
    end

    it "returns a Bignum for a large enough Float input value" do
      input = 219238102380912830988.5 # chosen by fair dice roll
      val   = @s.rb_dbl2big(input)

      val.kind_of?(Bignum).should == true

      # This value is based on the output of a simple C extension that uses
      # rb_dbl2big() to convert the above input value to a Bignum.
      val.should == 219238102380912836608
    end

    it "raises FloatDomainError for Infinity values" do
      inf = 1.0 / 0

      lambda { @s.rb_dbl2big(inf) }.should raise_error(FloatDomainError)
    end

    it "raises FloatDomainError for NaN values" do
      nan = 0.0 / 0

      lambda { @s.rb_dbl2big(nan) }.should raise_error(FloatDomainError)
    end
  end

  ruby_version_is ""..."2.2" do
    describe "RBIGNUM_SIGN" do
      it "returns C true if the Bignum has a positive sign" do
        @s.RBIGNUM_SIGN(bignum_value).should be_true
      end

      it "retuns C false if the Bignum has a negative sign" do
        @s.RBIGNUM_SIGN(-bignum_value).should be_false
      end
    end

    describe "RBIGNUM_POSITIVE_P" do
      it "returns C true if the Bignum has a positive sign" do
        @s.RBIGNUM_POSITIVE_P(bignum_value).should be_true
      end

      it "retuns C false if the Bignum has a negative sign" do
        @s.RBIGNUM_POSITIVE_P(-bignum_value).should be_false
      end
    end

    describe "RBIGNUM_NEGATIVE_P" do
      it "returns C false if the Bignum has a positive sign" do
        @s.RBIGNUM_NEGATIVE_P(bignum_value).should be_false
      end

      it "retuns C true if the Bignum has a negative sign" do
        @s.RBIGNUM_NEGATIVE_P(-bignum_value).should be_true
      end
    end

    describe "RBIGNUM_LEN" do
      it "returns the number of BDIGITS needed for the bignum" do
        @s.RBIGNUM_LEN(bignum_value * 2).should == 3
      end
    end
  end
end
