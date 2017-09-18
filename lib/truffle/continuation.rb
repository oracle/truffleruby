warn "#{File.basename(__FILE__)}: warning: callcc is obsolete; use Fiber instead"

class Continuation
  def initialize
    @fiber = Fiber.current
  end

  def call
    if Fiber.current != @fiber
      raise 'continuation called across fiber'
    end
    raise 'Continuations are unsupported on TruffleRuby'
  end
end

module Kernel
  def callcc
    yield Continuation.new
  end
  module_function :callcc
end
