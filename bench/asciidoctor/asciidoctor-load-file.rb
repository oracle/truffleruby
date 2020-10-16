require_relative 'asciidoctor/lib/asciidoctor'

sample_file = File.expand_path('../data/userguide.adoc', __FILE__)
sample_file_io = File.open(sample_file)

benchmark 'load-file' do
  Asciidoctor.load sample_file_io
  sample_file_io.seek 0
end
