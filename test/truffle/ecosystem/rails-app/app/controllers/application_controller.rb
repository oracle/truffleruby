class ApplicationController < ActionController::Base
  PLATFORM = if defined? Truffle
               Truffle::Graal.graal? ? :graal : :truffle
             else
               :jruby
             end

  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception
end
