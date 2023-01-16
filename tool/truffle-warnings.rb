suppress_warnings_line = ARGV.fetch(0)

prefix = "src/main/java/"
files = Dir["#{prefix}**/*.java"].select { |file|
  File.read(file).include? "import com.oracle.truffle.api.dsl.Specialization;"
}
dirs = files.map { File.dirname(_1) }.uniq

dirs.each do |dir|
  package = dir.delete_prefix(prefix).gsub("/", ".")
  contents = <<~PACKAGE_INFO
  #{suppress_warnings_line}
  package #{package};

  import com.oracle.truffle.api.dsl.SuppressPackageWarnings;
  PACKAGE_INFO
  File.write("#{dir}/package-info.java", contents)
end
