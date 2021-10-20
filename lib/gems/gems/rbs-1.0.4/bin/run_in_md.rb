require "tmpdir"
require "shellwords"

ARGV.each do |path|
  puts "~~~~~~~ Checking #{path} ~~~~~~"
  content = File.read(path)

  snippets = []
  lines = []
  content.lines.each.with_index do |line, index|
    case line
    when /run-start:/
      lines = [[line, index+1]]
    when /run-end/
      lines << [line, index+1]
      snippets << lines
      lines = []
    else
      lines << [line, index+1]
    end
  end

  snippets.each do |lines|
    puts ">>> Code detected: "
    hd, _, *code_lines, _, _ = lines

    head = hd[0]
    head.gsub!(/^<!-- +/, "").gsub!(/ +-->$/, "")
    _,name,command = head.split(/:/)

    puts "# command=#{Shellwords.split(command).inspect}"
    puts "# name=#{name}"
    puts code_lines.map {|line, i| "#{"%4d" % i}  #{line}" }.join

    code = code_lines.map(&:first).join

    puts ">>> Running..."
    Dir.mktmpdir do |dir|
      File.write(File.join(dir, name), code)
      pid = spawn({ "BUNDLE_GEMFILE" => File.join(__dir__, "../Gemfile") },
                  *Shellwords.split(command),
                  chdir: dir)

      _, status = Process.waitpid2(pid)

      status.success? or raise "Failed to execute code: #{code_lines.join}"
    end
  end
end
