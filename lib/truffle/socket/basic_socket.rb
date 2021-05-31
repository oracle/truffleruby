# truffleruby_primitives: true

# Copyright (c) 2013, Brian Shirai
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of the library nor the names of its contributors may be
#    used to endorse or promote products derived from this software without
#    specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

class BasicSocket < IO
  def self.for_fd(fd)
    sock = allocate

    IO.setup(sock, fd, nil, true)
    sock.binmode
    # TruffleRuby: start
    sock.do_not_reverse_lookup = do_not_reverse_lookup
    # TruffleRuby: end

    sock
  end

  def self.do_not_reverse_lookup=(setting)
    @no_reverse_lookup = setting
  end

  def self.do_not_reverse_lookup
    @no_reverse_lookup = true unless defined?(@no_reverse_lookup)
    @no_reverse_lookup
  end

  def do_not_reverse_lookup=(setting)
    @no_reverse_lookup = setting
  end

  def do_not_reverse_lookup
    @no_reverse_lookup
  end

  def getsockopt(level, optname)
    sockname = Truffle::Socket::Foreign.getsockname(Primitive.io_fd(self))
    family   = Truffle::Socket.family_for_sockaddr_in(sockname)
    level    = Truffle::Socket::SocketOptions.socket_level(level, family)
    optname  = Truffle::Socket::SocketOptions.socket_option(level, optname)
    data     = Truffle::Socket::Foreign.getsockopt(Primitive.io_fd(self), level, optname)

    Socket::Option.new(family, level, optname, data)
  end

  def setsockopt(*args)
    if args.size == 1
      option = args[0]
      level   = option.level
      optname = option.optname
      optval  = option.data
    elsif args.size == 3
      level, optname, optval = args
    else
      Truffle::Type.check_arity(args.size, 3, 3)
    end

    sockname = Truffle::Socket::Foreign.getsockname(Primitive.io_fd(self))
    family   = Truffle::Socket.family_for_sockaddr_in(sockname)
    level    = Truffle::Socket::SocketOptions.socket_level(level, family)
    optname  = Truffle::Socket::SocketOptions.socket_option(level, optname)
    error    = 0

    case optval
    when true  then optval = 1
    when false then optval = 0
    end

    if optval.is_a?(Integer)
      Truffle::Socket::Foreign.memory_pointer(:socklen_t) do |pointer|
        pointer.write_int(optval)

        error = Truffle::Socket::Foreign
          .setsockopt(Primitive.io_fd(self), level, optname, pointer, pointer.total)
      end
    elsif optval.is_a?(String)
      Truffle::Socket::Foreign.memory_pointer(optval.bytesize) do |pointer|
        pointer.write_bytes(optval)

        error = Truffle::Socket::Foreign
          .setsockopt(Primitive.io_fd(self), level, optname, pointer, optval.bytesize)
      end
    else
      raise TypeError, 'socket option should be an Integer, String, true, or false'
    end

    Errno.handle('unable to set socket option') if error < 0

    0
  end

  def getsockname
    Truffle::Socket::Foreign.getsockname(Primitive.io_fd(self))
  end

  def getpeername
    Truffle::Socket::Foreign.getpeername(Primitive.io_fd(self))
  end

  def send(message, flags, dest_sockaddr = nil)
    message    = StringValue(message)
    bytes      = message.bytesize
    bytes_sent = 0

    if dest_sockaddr.is_a?(Addrinfo)
      dest_sockaddr = dest_sockaddr.to_sockaddr
    end

    Truffle::Socket::Foreign.char_pointer(bytes) do |buffer|
      buffer.write_bytes(message)

      if dest_sockaddr.is_a?(String)
        addr = Truffle::Socket.sockaddr_class_for_socket(self)
          .with_sockaddr(dest_sockaddr)

        begin
          bytes_sent = Truffle::Socket::Foreign
            .sendto(Primitive.io_fd(self), buffer, bytes, flags, addr, addr.size)
        ensure
          addr.pointer.free
        end
      else
        bytes_sent = Truffle::Socket::Foreign
          .send(Primitive.io_fd(self), buffer, bytes, flags)
      end
    end

    Truffle::Socket::Error.write_error('send(2)', self) if bytes_sent < 0

    bytes_sent
  end

  private def internal_recv(bytes_to_read, flags, buffer, exception)
    Truffle::Socket::Foreign.memory_pointer(bytes_to_read) do |buf|
      n_bytes = Truffle::Socket::Foreign.recv(Primitive.io_fd(self), buf, bytes_to_read, flags)

      if n_bytes == -1
        if exception
          Truffle::Socket::Error.read_error('recv(2)', self)
        else
          return :wait_readable
        end
      end

      str = buf.read_string(n_bytes)
      buffer ? buffer.replace(str) : str
    end
  end

  def recv(bytes_to_read, flags = 0, buf = nil)
    internal_recv(bytes_to_read, flags, buf, true)
  end

  private def __recv_nonblock(bytes_to_read, flags, buf, exception)
    self.nonblock = true

    internal_recv(bytes_to_read, flags, buf, exception)
  end

  private def internal_recvmsg(max_msg_len, flags, max_control_len, scm_rights, exception)
    socket_type = getsockopt(:SOCKET, :TYPE).int

    if socket_type == Socket::SOCK_STREAM
      grow_msg = false
    else
      grow_msg = max_msg_len.nil?
    end

    flags |= Socket::MSG_PEEK if grow_msg

    msg_len = max_msg_len || 4096

    loop do
      msg_buffer = Truffle::Socket::Foreign.char_pointer(msg_len)
      address    = Truffle::Socket.sockaddr_class_for_socket(self).new
      io_vec     = Truffle::Socket::Foreign::Iovec.with_buffer(msg_buffer)
      header     = Truffle::Socket::Foreign::Msghdr.with_buffers(address, io_vec)

      begin
        need_more = false

        msg_size = Truffle::Socket::Foreign
          .recvmsg(Primitive.io_fd(self), header.pointer, flags)

        if msg_size < 0
          if exception
            Truffle::Socket::Error.read_error('recvmsg(2)', self)
          else
            return :wait_readable
          end
        end

        if grow_msg and header.message_truncated?
          need_more = true
          msg_len *= 2
        end

        next if need_more

        # When a socket is actually connected the address structure is not used.
        if header.address_size > 0
          addr = Addrinfo.new(address.to_s, address.family, socket_type)
        else
          addr = Addrinfo.new([Socket::AF_UNSPEC], nil, socket_type)
        end

        return msg_buffer.read_string(msg_size), addr, header.flags
      ensure
        msg_buffer.free
        address.pointer.free
        io_vec.pointer.free
        header.pointer.free
      end
    end

    nil
  end

  private def __recvmsg(max_msg_len, flags, max_control_len, scm_rights)
    internal_recvmsg(max_msg_len, flags, max_control_len, scm_rights, true)
  end

  private def __recvmsg_nonblock(max_msg_len, flags, max_control_len, scm_rights, exception)
    self.nonblock = true

    internal_recvmsg(max_msg_len, flags | Socket::MSG_DONTWAIT, max_control_len, scm_rights, exception)
  end

  private def internal_sendmsg(message, flags, dest_sockaddr, exception)
    msg_buffer = Truffle::Socket::Foreign.char_pointer(message.bytesize)
    io_vec     = Truffle::Socket::Foreign::Iovec.with_buffer(msg_buffer)
    header     = Truffle::Socket::Foreign::Msghdr.new
    address    = nil

    begin
      msg_buffer.write_bytes(message)

      header.message = io_vec

      if dest_sockaddr.is_a?(Addrinfo)
        dest_sockaddr = dest_sockaddr.to_sockaddr
      end

      if dest_sockaddr.is_a?(String)
        address = Truffle::Socket::Foreign::SockaddrIn
          .with_sockaddr(dest_sockaddr)

        header.address = address
      end

      num_bytes = Truffle::Socket::Foreign
        .sendmsg(Primitive.io_fd(self), header.pointer, flags)

      if num_bytes < 0
        if exception
          Truffle::Socket::Error.read_error('sendmsg(2)', self)
        else
          return :wait_writable
        end
      end

      num_bytes
    ensure
      address.pointer.free if address
      header.pointer.free
      io_vec.pointer.free
      msg_buffer.free
    end
  end

  private def __sendmsg(message, flags, dest_sockaddr, controls)
    internal_sendmsg(message, flags, dest_sockaddr, true)
  end

  private def __sendmsg_nonblock(message, flags, dest_sockaddr, controls, exception)
    self.nonblock = true

    internal_sendmsg(message, flags | Socket::MSG_DONTWAIT, dest_sockaddr, exception)
  end

  def close_read
    ensure_open

    if mode_read_only?
      return close
    end

    Truffle::Socket::Foreign.shutdown(Primitive.io_fd(self), 0)

    force_write_only

    nil
  end

  def close_write
    ensure_open

    if mode_write_only?
      return close
    end

    Truffle::Socket::Foreign.shutdown(Primitive.io_fd(self), 1)

    force_read_only

    nil
  end

  def shutdown(how = Socket::SHUT_RDWR)
    how = Truffle::Socket.shutdown_option(how)
    err = Truffle::Socket::Foreign.shutdown(Primitive.io_fd(self), how)

    Errno.handle('shutdown(2)') unless err == 0

    0
  end

  # MRI defines this method in BasicSocket and stuffs all logic in it. Since
  # inheriting classes behave differently we overwrite this method in said
  # classes. The method here exists so that code such as the following still
  # works: BasicSocket.method_defined?(:local_address).
  def local_address
    raise NotImplementedError,
      'This method must be implemented by classes inheriting from BasicSocket'
  end

  def remote_address
    raise NotImplementedError,
      'This method must be implemented by classes inheriting from BasicSocket'
  end

  def getpeereid
    Truffle::Socket::Foreign.getpeereid(Primitive.io_fd(self))
  end
end
