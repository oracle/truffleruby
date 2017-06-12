require File.expand_path('../../../../spec_helper', __FILE__)
require File.expand_path('../../fixtures/classes', __FILE__)

platform_is_not :windows do
  describe "UNIXServer#accept" do
    before :each do
      @path = SocketSpecs.socket_path
    end

    after :each do
      SocketSpecs.rm_socket @path
    end

    it "accepts what is written by the client" do
      server = UNIXServer.open(@path)
      client = UNIXSocket.open(@path)

      client.send('hello', 0)

      sock = server.accept
      data, info = sock.recvfrom(5)

      data.should == 'hello'
      info.should_not be_empty

      server.close
      client.close
      sock.close
    end

    it "can be interrupted by Thread#kill" do
      server = UNIXServer.new(@path)
      t = Thread.new {
        server.accept
      }
      Thread.pass while t.status and t.status != "sleep"

      # kill thread, ensure it dies in a reasonable amount of time
      t.kill
      a = 0
      while t.alive? and a < 5000
        sleep 0.001
        a += 1
      end
      a.should < 5000

      server.close
    end

    it "can be interrupted by Thread#raise" do
      server = UNIXServer.new(@path)
      t = Thread.new {
        server.accept
      }
      Thread.pass while t.status and t.status != "sleep"

      # raise in thread, ensure the raise happens
      ex = Exception.new
      t.raise ex
      lambda { t.join }.should raise_error(Exception)
      server.close
    end
  end
end
