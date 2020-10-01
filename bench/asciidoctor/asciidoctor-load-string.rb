require_relative 'asciidoctor/lib/asciidoctor'

sample_file = File.expand_path('../data/userguide.adoc', __FILE__)
sample_file_text = File.read(sample_file)

benchmark 'load-string' do
  Asciidoctor.load sample_file_text
end
