$LOAD_PATH.unshift "#{__dir__}/bundle"
require 'bundler/setup'

require 'sinatra/base'

class App < Sinatra::Base
  set :environment, :production

  get '/' do
    'apples, oranges & bananas'
  end
end

app = App
env = Rack::MockRequest.env_for('/', { method: Rack::GET })

benchmark do
  app.call(env.dup)
end
