guard 'rspec', cli: '--color' do
  watch(%r{^spec/.+_spec\.rb$})
  watch(%r{^lib/(.+)\.rb$})     { "spec" }
  watch(%r{^ext/(.+)$})     { "spec" }
  watch('spec/spec_helper.rb')  { "spec" }
end
