# frozen_string_literal: true

# Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

class Time
  include Comparable

  MonthValue = {
    'JAN' => 1, 'FEB' => 2, 'MAR' => 3, 'APR' => 4, 'MAY' => 5, 'JUN' => 6,
    'JUL' => 7, 'AUG' => 8, 'SEP' => 9, 'OCT' =>10, 'NOV' =>11, 'DEC' =>12
  }

  def inspect
    str = strftime('%Y-%m-%d %H:%M:%S')

    if nsec != 0
      str << sprintf('.%09d', nsec)
      str.chop! while str.end_with?('0')
    end

    str << (gmt? ? ' UTC' : strftime(' %z'))
    str.force_encoding Encoding::US_ASCII
  end

  def to_s
    str = strftime('%Y-%m-%d %H:%M:%S')

    str << (gmt? ? ' UTC' : strftime(' %z'))
    str.force_encoding Encoding::US_ASCII
  end

  def subsec
    if nsec == 0
      0
    else
      Rational(nsec, 1_000_000_000)
    end
  end

  def sunday?
    wday == 0
  end

  def monday?
    wday == 1
  end

  def tuesday?
    wday == 2
  end

  def wednesday?
    wday == 3
  end

  def thursday?
    wday == 4
  end

  def friday?
    wday == 5
  end

  def saturday?
    wday == 6
  end

  def to_f
    tv_sec + tv_nsec * 0.000_000_001
  end

  def to_r
    (tv_sec + subsec).to_r
  end

  def getlocal(offset=nil)
    dup.localtime(offset)
  end

  def zone
    zone = Primitive.time_zone(self)

    if zone && zone.ascii_only?
      zone.encode Encoding::US_ASCII
    elsif zone && Encoding.default_internal
      zone.encode Encoding.default_internal
    else
      zone
    end
  end

  # Random number for hash codes. Stops hashes for similar values in
  # different classes from clashing, but defined as a constant so
  # that hashes will be deterministic.

  CLASS_SALT = 0xf39684d6

  private_constant :CLASS_SALT

  def hash
    val = Primitive.vm_hash_start CLASS_SALT
    val = Primitive.vm_hash_update val, tv_sec
    val = Primitive.vm_hash_update val, tv_nsec
    Primitive.vm_hash_end val
  end

  def eql?(other)
    Primitive.object_kind_of?(other, Time) and tv_sec == other.tv_sec and tv_nsec == other.tv_nsec
  end

  def <=>(other)
    if Primitive.object_kind_of?(other, Time)
      (tv_sec <=> other.tv_sec).nonzero? || (tv_nsec <=> other.tv_nsec)
    else
      r = (other <=> self)
      return nil if Primitive.nil? r
      return -1 if r > 0
      return  1 if r < 0
      0
    end
  end

  def to_a
    [sec, min, hour, day, month, year, wday, yday, dst?, zone]
  end

  def strftime(format)
    Primitive.time_strftime self, StringValue(format)
  end

  def asctime
    strftime('%a %b %e %H:%M:%S %Y')
  end
  alias_method :ctime, :asctime

  def getgm
    dup.gmtime
  end
  alias_method :getutc, :getgm

  def localtime(offset = nil)
    if offset
      to_utc = Time.send(:utc_offset_in_utc?, offset)
      offset = Truffle::Type.coerce_to_utc_offset(offset)
    end

    # the only cases when #localtime is allowed for a frozen time -
    # - to covert from UTC to UTC
    # - to convert from local time to the same local time
    if frozen? && !(utc? && to_utc || !utc? && offset.nil?)
      raise FrozenError, "can't modify frozen Time: #{self}"
    end

    # the only time zone that could be derived from utc_offset now is UTC
    if to_utc
      Primitive.time_utctime(self)
    else
      Primitive.time_localtime(self, offset)
    end
  end

  def succ
    warn 'Time#succ is obsolete', uplevel: 1

    self + 1
  end

  def +(other)
    raise TypeError, 'time + time?' if Primitive.object_kind_of?(other, Time)

    case other = Truffle::Type.coerce_to_exact_num(other)
    when Integer
      other_sec = other
      other_nsec = 0
    else
      other_sec, nsec_frac = other.divmod(1)
      other_nsec = (nsec_frac * 1_000_000_000).to_i
    end

    time = Time.allocate
    time.send(:initialize_copy, self)
    Primitive.time_add(time, other_sec, other_nsec)
  end

  def -(other)
    if Primitive.object_kind_of?(other, Time)
      return (tv_sec - other.tv_sec) + ((tv_nsec - other.tv_nsec) * 0.000_000_001)
    end

    case other = Truffle::Type.coerce_to_exact_num(other)
    when Integer
      other_sec = other
      other_nsec = 0
    else
      other_sec, nsec_frac = other.divmod(1)
      other_nsec = (nsec_frac * 1_000_000_000 + 0.5).to_i
    end

    time = Time.allocate
    time.send(:initialize_copy, self)
    Primitive.time_add(time, -other_sec, -other_nsec)
  end

  def round(places = 0)
    original = to_i + subsec.to_r
    rounded = original.round(places)

    time = Time.allocate
    time.send(:initialize_copy, self)
    time + (rounded - original)
  end

  def floor(places = 0)
    original = to_i + subsec.to_r
    floored = original.floor(places)

    time = Time.allocate
    time.send(:initialize_copy, self)
    time + (floored - original)
  end

  def ceil(places = 0)
    original = to_i + subsec.to_r
    ceiled = original.ceil(places)

    time = Time.allocate
    time.send(:initialize_copy, self)
    time + (ceiled - original)
  end

  def self._load(data)
    # TODO: doesn't load ivars
    raise TypeError, 'marshaled time format differ' unless data.bytesize == 8

    major, minor = data.unpack 'VV'

    if (major & (1 << 31)) == 0 then
      at major, minor
    else
      major &= ~(1 << 31)

      is_gmt =  (major >> 30) & 0x1
      year   = ((major >> 14) & 0xffff) + 1900
      mon    = ((major >> 10) & 0xf) + 1
      mday   =  (major >>  5) & 0x1f
      hour   =  major         & 0x1f

      min   =  (minor >> 26) & 0x3f
      sec   =  (minor >> 20) & 0x3f

      usec = minor & 0xfffff

      time = gm year, mon, mday, hour, min, sec, usec
      time.localtime if is_gmt.zero?
      time
    end
  end
  private_class_method :_load

  def _dump(limit = nil)
    # TODO: doesn't dump ivars
    tm = getgm.to_a

    if (year & 0xffff) != year || year < 1900 then
      raise ArgumentError, "year too big to marshal: #{year}"
    end

    gmt = gmt? ? 1 : 0

    major =  1             << 31 | # 1 bit
             gmt           << 30 | # 1 bit
            (tm[5] - 1900) << 14 | # 16 bits
            (tm[4] - 1)    << 10 | # 4 bits
             tm[3]         <<  5 | # 5 bits
             tm[2]                 # 5 bits
    minor =  tm[1]   << 26 | # 6 bits
             tm[0]   << 20 | # 6 bits
             usec # 20 bits

    [major, minor].pack 'VV'
  end
  private :_dump

  class << self
    def at(sec, sub_sec=undefined, unit=undefined, **kwargs)
      # **kwargs is used here because 'in' is a ruby keyword
      timezone = kwargs[:in]
      offset = timezone ? Truffle::Type.coerce_to_utc_offset(timezone) : nil
      is_utc = utc_offset_in_utc?(timezone) if offset

      result = if Primitive.undefined?(sub_sec)
                 if Primitive.object_kind_of?(sec, Time)
                   copy = allocate
                   copy.send(:initialize_copy, sec)
                   copy
                 elsif Primitive.object_kind_of?(sec, Integer)
                   Primitive.time_at self, sec, 0
                 elsif Primitive.object_kind_of?(sec, Float) and sec >= 0.0
                   ns = (sec % 1.0 * 1e9).round
                   Primitive.time_at self, sec.to_i, ns
                 end
               end
      if result && offset
        result = is_utc ? Primitive.time_utctime(result) : Primitive.time_localtime(result, offset)
      end
      if result
        return result
      end

      if Primitive.object_kind_of?(sec, Time) && Primitive.object_kind_of?(sub_sec, Integer)
        raise TypeError, "can't convert Time into an exact number"
      end

      sub_sec_scale =
        if Primitive.undefined?(unit) || :microsecond == unit || :usec == unit
          1_000
        elsif :millisecond == unit
          1_000_000
        elsif :nanosecond == unit || :nsec == unit
          1
        else
          raise ArgumentError, "unexpected unit: #{unit}"
        end

      sec = Truffle::Type.coerce_to_exact_num(sec)
      sub_sec = Primitive.undefined?(sub_sec) ? 0 : Truffle::Type.coerce_to_exact_num(sub_sec)

      seconds, sec_frac = sec.divmod(1)

      nsec = (sec_frac * 1_000_000_000).round + (sub_sec * sub_sec_scale).to_i

      seconds += nsec / 1_000_000_000
      nsec %= 1_000_000_000

      time = Primitive.time_at self, seconds, nsec

      if offset
        time = is_utc ? Primitive.time_utctime(time) : Primitive.time_localtime(time, offset)
      end

      time
    end

    def from_array(sec, min, hour, mday, month, year, nsec, is_dst, is_utc, utc_offset)
      time = Primitive.time_s_from_array(self, sec, min, hour, mday, month, year, nsec, is_dst, is_utc, utc_offset)
      return time unless Primitive.undefined?(time)

      if Primitive.object_kind_of?(sec, String)
        sec = sec.to_i
      elsif nsec
        sec = Truffle::Type.coerce_to(sec || 0, Integer, :to_int)
      else
        s = Truffle::Type.coerce_to_exact_num(sec || 0)

        sec       = s.to_i
        nsec_frac = s % 1.0

        if s < 0 && nsec_frac > 0
          sec -= 1
        end

        nsec = (nsec_frac * 1_000_000_000 + 0.5).to_i
      end

      nsec ||= 0
      sec += nsec / 1_000_000_000
      nsec %= 1_000_000_000

      from_array(sec, min, hour, mday, month, year, nsec, is_dst, is_utc, utc_offset)
    end
    private :from_array

    def compose(offset, p1, p2=nil, p3=nil, p4=nil, p5=nil, p6=nil, p7=nil,
                yday=undefined, is_dst=undefined, tz=undefined)
      if Primitive.undefined?(tz)
        unless Primitive.undefined?(is_dst)
          raise ArgumentError, 'wrong number of arguments (9 for 1..8)'
        end

        y = p1
        m = p2
        d = p3
        hr = p4
        min = p5
        sec = p6
        usec = p7
        is_dst = -1
      else
        y = p6
        m = p5
        d = p4
        hr = p3
        min = p2
        sec = p1
        usec = 0
        is_dst = is_dst ? 1 : 0
      end

      if Primitive.object_kind_of?(m, String) or m.respond_to?(:to_str)
        m = StringValue(m)
        m = MonthValue[m.upcase] || m.to_i

        raise ArgumentError, 'month argument out of range' unless m
      else
        m = Truffle::Type.coerce_to(m || 1, Integer, :to_int)
      end

      y   = Primitive.object_kind_of?(y, String)   ? y.to_i   : Truffle::Type.coerce_to(y,        Integer, :to_int)
      d   = Primitive.object_kind_of?(d, String)   ? d.to_i   : Truffle::Type.coerce_to(d   || 1, Integer, :to_int)
      hr  = Primitive.object_kind_of?(hr, String)  ? hr.to_i  : Truffle::Type.coerce_to(hr  || 0, Integer, :to_int)
      min = Primitive.object_kind_of?(min, String) ? min.to_i : Truffle::Type.coerce_to(min || 0, Integer, :to_int)

      nsec = nil
      if Primitive.object_kind_of?(usec, String)
        nsec = usec.to_i * 1000
      elsif usec
        nsec = (usec * 1000).to_i
      end

      case offset
      when :utc
        is_dst = -1
        is_utc = true
        offset = nil
      when :local
        is_utc = false
        offset = nil
      else
        is_dst = -1
        is_utc = false
      end

      from_array(sec, min, hr, d, m, y, nsec, is_dst, is_utc, offset)
    end
    private :compose

    def new(year=undefined, month=nil, day=nil, hour=nil, minute=nil, second=nil, utc_offset=nil)
      Truffle::Type.check_null_safe(utc_offset) if utc_offset.instance_of?(String)

      if Primitive.undefined?(year)
        self.now
      elsif Primitive.nil? utc_offset
        compose(:local, year, month, day, hour, minute, second)
      elsif utc_offset.instance_of?(String) && !valid_utc_offset_string?(utc_offset)
        raise ArgumentError, '"+HH:MM", "-HH:MM", "UTC" or "A".."I","K".."Z" expected for utc_offset: ' + utc_offset
      elsif utc_offset == :std
        compose(:local, second, minute, hour, day, month, year, nil, nil, false, nil)
      elsif utc_offset == :dst
        compose(:local, second, minute, hour, day, month, year, nil, nil, true, nil)
      else
        if utc_offset_in_utc?(utc_offset)
          utc_offset = :utc
        else
          utc_offset = Truffle::Type.coerce_to_utc_offset(utc_offset)
        end
        compose(utc_offset, year, month, day, hour, minute, second)
      end
    end

    def valid_utc_offset_string?(utc_offset)
      utc_offset == 'UTC' \
        || (utc_offset.size == 1 && ('A'..'Z') === utc_offset && utc_offset != 'J') \
        || (utc_offset =~ /\A[+-](\d{2})(?::(\d{2})(?::(\d{2}))?)?\z/ && $1.to_i < 24 && $2.to_i < 60 && $3.to_i < 60) \
        || (utc_offset =~ /\A[+-](\d{2})(?:(\d{2})(?:(\d{2}))?)?\z/ && $1.to_i < 24 && $2.to_i < 60 && $3.to_i < 60) # without ":" separators
    end
    private :valid_utc_offset_string?

    def utc_offset_in_utc?(utc_offset)
      utc_offset == 'UTC' || utc_offset == 'Z' || utc_offset == '-00:00'
    end
    private :utc_offset_in_utc?

    def local(*args)
      compose(:local, *args)
    end
    alias_method :mktime, :local

    def gm(*args)
      compose(:utc, *args)
    end
    alias_method :utc, :gm
  end
end
