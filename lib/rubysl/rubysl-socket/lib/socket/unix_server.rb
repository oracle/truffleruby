class UNIXServer < UNIXSocket
  def initialize(path)
    @no_reverse_lookup = self.class.do_not_reverse_lookup
    @path              = path

    fd = RubySL::Socket::Foreign.socket(Socket::AF_UNIX, Socket::SOCK_STREAM, 0)

    Errno.handle('socket(2)') if fd < 0

    IO.setup(self, fd, 'r+', true)
    binmode

    sockaddr = Socket.sockaddr_un(@path)
    status   = RubySL::Socket::Foreign.bind(descriptor, sockaddr)

    Errno.handle('bind(2)') if status < 0

    listen(Socket::SOMAXCONN)
  end

  def listen(backlog)
    RubySL::Socket.listen(self, backlog)
  end

  def accept
    RubySL::Socket.accept(self, UNIXSocket)[0]
  end

  def accept_nonblock
    RubySL::Socket.accept_nonblock(self, UNIXSocket)[0]
  end

  def sysaccept
    accept.fileno
  end

  def inspect
    "#{super[0...-1]} #{@path}>"
  end
end
