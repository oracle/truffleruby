# Copyright (c) 2007-2017 The JRuby project. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# This is a shim based on JRuby's implementation with stty

# This implementation of io/console is a little hacky. It shells out to `stty`
# for most operations, which does not work on Windows, in secured environments,
# and so on.
#
# Finally, since we're using stty to shell out, we can only manipulate stdin/
# stdout tty rather than manipulating whatever terminal is actually associated
# with the IO we're calling against. This will produce surprising results if
# anyone is actually using io/console against non-stdio ttys...but that case
# seems like it would be pretty rare.

require 'rbconfig'

# Methods common to all backend impls
class IO
  def getch(*)
    raw do
      getc
    end
  end

  def getpass(prompt = nil)
    wio = self == $stdin ? $stderr : self
    wio.write(prompt) if prompt
    begin
      str = nil
      noecho do
        str = gets
      end
    ensure
      puts($/)
    end
    str.chomp
  end
end


class IO
  CONSOLE_MUTEX = Mutex.new

  def self.console(sym=nil, *args)
    raise TypeError, "expected Symbol, got #{sym.class}" unless sym.nil? || sym.kind_of?(Symbol)

    console = nil

    CONSOLE_MUTEX.synchronize do
      if defined?(@console) # using ivar instead of hidden const as in MRI
        console = @console

        # MRI checks IO internals : (!RB_TYPE_P(con, T_FILE) || (!(fptr = RFILE(con)->fptr) || GetReadFD(fptr) == -1))
        if !console.kind_of?(File) || (console.closed? || !FileTest.readable?(console))
          remove_instance_variable :@console
          console = nil
        end
      end

      if sym == :close
        if console
          console.close
          remove_instance_variable :@console if defined?(@console)
        end

        return nil
      end

      if !console && $stdin.tty?
        console = File.open('/dev/tty', 'r+')
        console.sync = true
        @console = console
      end
    end

    sym ? console.send(sym, *args) : console
  end

  if RbConfig::CONFIG['host_os'].downcase =~ /linux/ && File.exist?("/proc/#{Process.pid}/fd")
    private def stty(*args)
      `stty #{args.join(' ')} < /proc/#{Process.pid}/fd/#{fileno}`
    end
  else
    private def stty(*args)
      `stty #{args.join(' ')}`
    end
  end

  def raw(*)
    saved = stty('-g')
    stty('raw -echo')
    yield self
  ensure
    stty(saved)
  end

  def raw!(*)
    stty('raw -echo')
  end

  def cooked(*)
    saved = stty('-g')
    stty('-raw')
    yield self
  ensure
    stty(saved)
  end

  def cooked!(*)
    stty('-raw')
  end

  def echo=(echo)
    stty(echo ? 'echo' : '-echo')
  end

  def echo?
    (stty('-a') =~ / -echo /) ? false : true
  end

  def noecho
    saved = stty('-g')
    stty('-echo')
    yield self
  ensure
    stty(saved)
  end

  # Not all systems return same format of stty -a output
  IEEE_STD_1003_2 = '(?<rows>\d+) rows; (?<columns>\d+) columns'
  UBUNTU = 'rows (?<rows>\d+); columns (?<columns>\d+)'

  def winsize
    match = stty('-a').match(/#{IEEE_STD_1003_2}|#{UBUNTU}/)

    if $?.success?
      [match[:rows].to_i, match[:columns].to_i]
    else
      raise Errno::ENOTTY # This isn't guaranteed to be ENOTTY, but unless we invoke ioctl(2) instead of stty(1), we can't tell what the error is for certain.
    end
  end

  def winsize=(size)
    stty("rows #{size[0]} cols #{size[1]}")
  end

  def iflush
  end

  def oflush
  end

  def ioflush
  end
end
