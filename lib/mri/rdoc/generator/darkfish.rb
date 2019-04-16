class RDoc::Generator::Darkfish

  RDoc::RDoc.add_generator self

  def initialize store, options
    raise 'the Darkfish RDoc theme is not supported on TruffleRuby'
  end

end
