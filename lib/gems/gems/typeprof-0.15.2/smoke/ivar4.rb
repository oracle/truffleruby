def foo
  bot = @undefined_ivar
  unless bot
    bot.nil?
  end
end

foo # There were a bug that type profiling is terminated at this line

def bar
end

__END__
# Classes
class Object
  @undefined_ivar: bot

  private
  def foo: -> bool
  def bar: -> nil
end
