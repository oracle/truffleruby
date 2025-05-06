lines = ARGF.readlines(chomp: true)
lines.each do |line|
  line.sub!(/^\[lib[^:]+:\d+\]\s+/, '')
  line.sub!(/\s*\\$/, '')
  line.sub!(/^'(.+)'$/, '\1')
  line.sub!(/^(.+?)@(?:--macro|--language|--tool|jar|user|driver)[^=]*=(.*)$/, '\1=\2')
  line.sub!(/^(.+?)@(?:--macro|--language|--tool|jar|user|driver)[^=]*$/, '\1')
end
lines.reject! { _1.start_with?("Apply jar") }
lines.delete("Executing [")
lines.delete("]")

to_merge_in_one_line = %w[
  --add-exports
  --module-path
  --module
  -imagecp
  -imagemp
  -keepalive
]

to_merge_in_one_line.each do |option|
  lines.each_cons(2) do |a, b|
    if a == option
      b.prepend "#{option}="
    end
  end
  lines.delete option
end

lines.sort!
lines.uniq!

fields_to_sort_value = %w[
  --module-path=
  -imagecp=
  -imagemp=

  --add-modules=
  --enable-native-access=
  -Dorg.graalvm.launcher.classpath=
  -Dsvm.modulesupport.addedModules=
  -H:CLibraryPath=
]
lines.each do |line|
  if fields_to_sort_value.any? { |f| line.start_with?(f) }
    key, value = line.split('=', 2)
    # sep = %w[-Dorg.graalvm.launcher.classpath=].include?(key) ? ':' : ','
    values = value.split(/[,:]/)
    if value.include?('/') # contains paths
      values = values.map { File.basename(_1) }
    end
    values = values.sort.uniq
    # value = values.join(',')
    value = "[\n#{values.join("\n")}\n]"

    line.replace "#{key}=#{value}"
  end
end

puts lines
