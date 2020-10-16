require_relative 'asciidoctor/lib/asciidoctor'

sample_file = File.expand_path('../data/userguide.adoc', __FILE__)
sample_file_text = File.read(sample_file)
document = Asciidoctor.load(sample_file_text)

benchmark 'convert' do
  document.convert
end
