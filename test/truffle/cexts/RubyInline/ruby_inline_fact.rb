require "inline"

# Make sure to not use any cache
cache = Inline.directory
abort unless cache.start_with?("#{Dir.home}/.ruby_inline")
FileUtils::Verbose.rm_rf cache

class Fact
  inline do |builder|
    builder.c <<-EOC
      long fact(int n) {
        long result = 1;
        while (n > 0) {
          result *= n;
          n--;
        }
        return result;
      }
    EOC
  end
end

n = 20
c_factorial = Fact.new.fact(20)
ruby_factorial = (1..20).reduce(:*)

p c_factorial
p ruby_factorial

abort "Wrong result" unless c_factorial == ruby_factorial
