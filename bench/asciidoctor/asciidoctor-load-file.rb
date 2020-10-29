require_relative 'asciidoctor/lib/asciidoctor'

sample_file = File.expand_path('../data/userguide.adoc', __FILE__)

benchmark 'load-file' do
  File.open(sample_file) do |io|
    Asciidoctor.load io
  end
end
