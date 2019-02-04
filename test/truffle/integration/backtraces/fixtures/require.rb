module TestModuleBacktrace
  def self.raising_method
    raise 'message'
  end

  raising_method
end
