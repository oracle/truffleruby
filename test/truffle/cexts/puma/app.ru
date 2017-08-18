require 'rack'

app = Proc.new do |_env|
  ['200', {'Content-Type' => 'text/html'}, ['Hello from Puma!']]
end

run app
