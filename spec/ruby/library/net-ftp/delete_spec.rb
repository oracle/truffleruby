require_relative '../../spec_helper'
require_relative 'spec_helper'
require_relative 'fixtures/server'

describe "Net::FTP#delete" do
  before :each do
    @server = NetFTPSpecs::DummyFTP.new
    @server.serve_once

    @ftp = Net::FTP.new
    @ftp.connect(@server.hostname, @server.server_port)
  end

  after :each do
    @ftp.quit rescue nil
    @ftp.close
    @server.stop
  end

  it "sends the DELE command with the passed filename to the server" do
    @ftp.delete("test.file")
    @ftp.last_response.should == "250 Requested file action okay, completed. (DELE test.file)\n"
  end

  it "raises a Net::FTPTempError when the response code is 450" do
    @server.should_receive(:dele).and_respond("450 Requested file action not taken.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPTempError)
  end

  it "raises a Net::FTPPermError when the response code is 550" do
    @server.should_receive(:dele).and_respond("550 Requested action not taken.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPPermError)
  end

  it "raises a Net::FTPPermError when the response code is 500" do
    @server.should_receive(:dele).and_respond("500 Syntax error, command unrecognized.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPPermError)
  end

  it "raises a Net::FTPPermError when the response code is 501" do
    @server.should_receive(:dele).and_respond("501 Syntax error in parameters or arguments.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPPermError)
  end

  it "raises a Net::FTPPermError when the response code is 502" do
    @server.should_receive(:dele).and_respond("502 Command not implemented.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPPermError)
  end

  it "raises a Net::FTPTempError when the response code is 421" do
    @server.should_receive(:dele).and_respond("421 Service not available, closing control connection.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPTempError)
  end

  it "raises a Net::FTPPermError when the response code is 530" do
    @server.should_receive(:dele).and_respond("530 Not logged in.")
    -> { @ftp.delete("test.file") }.should raise_error(Net::FTPPermError)
  end
end
