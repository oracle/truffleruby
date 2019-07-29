data = File.binread(__FILE__ )

cexts = {
  etc: -> { Etc.getpwnam(Etc.getlogin) },
  nkf: -> { NKF.guess(data) },
  openssl: -> { OpenSSL::Digest::SHA256.new.digest(data) },
  :'rbconfig/sizeof' => -> { RbConfig::SIZEOF["long"] },
  syslog: -> { Syslog.open("test").close },
  yaml: -> { Psych.dump(data) },
  zlib: -> { Zlib::Deflate.deflate(data) },
}

$go = false
q = Queue.new

threads = cexts.each_pair.map do |cext, code|
  Thread.new do
    q << :ready
    Thread.pass until $go
    require cext.to_s
    100.times { code.call }
    :success
  end
end

threads.each { q.pop }
$go = true
threads.each(&:join)

puts 'success'
