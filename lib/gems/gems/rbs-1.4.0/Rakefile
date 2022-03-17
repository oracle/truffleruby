require "bundler/gem_tasks"
require "rake/testtask"
require "rbconfig"

$LOAD_PATH << File.join(__dir__, "test")

ruby = ENV["RUBY"] || RbConfig.ruby
racc = ENV.fetch("RACC", "racc")
rbs = File.join(__dir__, "exe/rbs")
bin = File.join(__dir__, "bin")

Rake::TestTask.new(:test) do |t|
  t.libs << "test"
  t.libs << "lib"
  t.test_files = FileList["test/**/*_test.rb"].reject do |path|
    path =~ %r{test/stdlib/}
  end
end

multitask :default => [:test, :stdlib_test, :rubocop, :validate, :test_doc]

task :test_doc => :parser do
  files = Dir.chdir(File.expand_path('..', __FILE__)) do
    `git ls-files -z`.split("\x0").select do |file| Pathname(file).extname == ".md" end
  end

  sh "#{ruby} #{__dir__}/bin/run_in_md.rb #{files.join(" ")}"
end

task :validate => :parser do
  sh "#{ruby} #{rbs} validate --silent"

  FileList["stdlib/*"].each do |path|
    lib = [File.basename(path).to_s]

    if lib == ["bigdecimal-math"]
      lib << "bigdecimal"
    end

    if lib == ["yaml"]
      lib << "dbm"
      lib << "pstore"
    end

    if lib == ["logger"]
      lib << "monitor"
    end

    if lib == ["csv"]
      lib << "forwardable"
    end

    if lib == ["prime"]
      lib << "singleton"
    end

    if lib == ["net-http"]
      lib << "uri"
    end

    if lib == ["resolv"]
      lib << "socket"
      lib << "timeout"
    end

    if lib == ["openssl"]
      lib << "socket"
    end

    sh "#{ruby} #{rbs} #{lib.map {|l| "-r #{l}"}.join(" ")} validate --silent"
  end
end

FileList["test/stdlib/**/*_test.rb"].each do |test|
  task test => :parser do
    sh "#{ruby} -Ilib #{bin}/test_runner.rb #{test}"
  end
  task stdlib_test: test
end

task :rubocop do
  sh "rubocop --parallel"
end

rule ".rb" => ".y" do |t|
  sh "#{racc} -v -o #{t.name} #{t.source}"
end

task :parser => "lib/rbs/parser.rb"
task :test => :parser
task :stdlib_test => :parser
task :build => :parser

task :confirm_parser do
  puts "Testing if parser.rb is updated with respect to parser.y"
  sh "#{racc} -v -o lib/rbs/parser.rb lib/rbs/parser.y"
  sh "git diff --exit-code lib/rbs/parser.rb"
end

namespace :generate do
  desc "Generate a test file for a stdlib class signatures"
  task :stdlib_test, [:class] do |_task, args|
    klass = args.fetch(:class) do
      raise "Class name is necessary. e.g. rake 'generate:stdlib_test[String]'"
    end

    path = Pathname(ENV["RBS_GENERATE_TEST_PATH"] || "test/stdlib/#{klass}_test.rb")
    raise "#{path} already exists!" if path.exist?

    require "erb"
    require "rbs"

    class TestTemplateBuilder
      attr_reader :klass, :env

      def initialize(klass)
        @klass = klass

        loader = RBS::EnvironmentLoader.new
        Dir['stdlib/*'].each do |lib|
          next if lib.end_with?('builtin')

          loader.add(library: File.basename(lib))
        end
        @env = RBS::Environment.from_loader(loader).resolve_type_names
      end

      def call
        ERB.new(<<~ERB, trim_mode: "-").result(binding)
          require_relative "test_helper"

          <%- unless class_methods.empty? -%>
          class <%= klass %>SingletonTest < Test::Unit::TestCase
            include TypeAssertions

            # library "pathname", "set", "securerandom"     # Declare library signatures to load
            testing "singleton(::<%= klass %>)"

          <%- class_methods.each do |method_name, definition| %>
            def test_<%= test_name_for(method_name) %>
          <%- definition.method_types.each do |method_type| -%>
              assert_send_type  "<%= method_type %>",
                                <%= klass %>, :<%= method_name %>
          <%- end -%>
            end
          <%- end -%>
          end
          <%- end -%>

          <%- unless instance_methods.empty? -%>
          class <%= klass %>Test < Test::Unit::TestCase
            include TypeAssertions

            # library "pathname", "set", "securerandom"     # Declare library signatures to load
            testing "::<%= klass %>"

          <%- instance_methods.each do |method_name, definition| %>
            def test_<%= test_name_for(method_name) %>
          <%- definition.method_types.each do |method_type| -%>
              assert_send_type  "<%= method_type %>",
                                <%= klass %>.new, :<%= method_name %>
          <%- end -%>
            end
          <%- end -%>
          end
          <%- end -%>
        ERB
      end

      private

      def test_name_for(method_name)
        {
          :==  => 'double_equal',
          :!=  => 'not_equal',
          :=== => 'triple_equal',
          :[]  => 'square_bracket',
          :[]= => 'square_bracket_assign',
          :>   => 'greater_than',
          :<   => 'less_than',
          :>=  => 'greater_than_equal_to',
          :<=  => 'less_than_equal_to',
          :<=> => 'spaceship',
          :+   => 'plus',
          :-   => 'minus',
          :*   => 'multiply',
          :/   => 'divide',
          :**  => 'power',
          :%   => 'modulus',
          :&   => 'and',
          :|   => 'or',
          :^   => 'xor',
          :>>  => 'right_shift',
          :<<  => 'left_shift',
          :=~  => 'pattern_match',
          :!~  => 'does_not_match',
          :~   => 'tilde'
        }.fetch(method_name, method_name)
      end

      def type_name
        @type_name ||= RBS::TypeName.new(name: klass.to_sym, namespace: RBS::Namespace.new(path: [], absolute: true))
      end

      def class_methods
        @class_methods ||= RBS::DefinitionBuilder.new(env: env).build_singleton(type_name).methods.select {|_, definition|
          definition.implemented_in == type_name
        }
      end

      def instance_methods
        @instance_methods ||= RBS::DefinitionBuilder.new(env: env).build_instance(type_name).methods.select {|_, definition|
          definition.implemented_in == type_name
        }
      end
    end

    path.write TestTemplateBuilder.new(klass).call

    puts "Created: #{path}"
  end
end

task :test_generate_stdlib do
  sh "RBS_GENERATE_TEST_PATH=/tmp/Array_test.rb rake 'generate:stdlib_test[Array]'"
  sh "ruby -c /tmp/Array_test.rb"
end

CLEAN.include("lib/rbs/parser.rb")
