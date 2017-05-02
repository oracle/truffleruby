require_relative 'asciidoctor/lib/asciidoctor'

sample_file = File.expand_path(__FILE__, '../data/userguide.adoc')
sample_file_io = File.open(sample_file)
sample_file_text = File.read(sample_file)
sample_file_lines = sample_file_text.lines

benchmark 'file-lines' do
  sample_file_io.readlines
  sample_file_io.seek 0
end

benchmark 'string-lines' do
  sample_file_text.lines
end

bigger_sample_file_lines = sample_file_lines * 100

benchmark 'read-line' do
  reader = Asciidoctor::Reader.new(bigger_sample_file_lines)
  reader.read_line while reader.has_more_lines?
end

benchmark 'restore-line' do
  reader = Asciidoctor::Reader.new
  100.times do
    reader.restore_line 'line'
  end
end

benchmark 'load-string' do
  Asciidoctor.load sample_file_text
end

benchmark 'load-file' do
  Asciidoctor.load sample_file_io
  sample_file_io.seek 0
end

document = Asciidoctor.load(sample_file_text)
converted = document.convert
lines = converted.split('\n')

class BacktickSubstitutor
  
  include Asciidoctor
  include Asciidoctor::Substitutors
  
  PATTERN = /(^|[^#{CC_WORD};:"'`}])(?:\[([^\]]+?)\])?`(\S|\S#{CC_ALL}*?\S)`(?![#{CC_WORD}"'`])/m
  
  def initialize(document)
    @document = document
  end
  
  def match(result)
    PATTERN.match(result)
  end
  
  def sub(result)
    result.gsub!(PATTERN) do
      convert_quoted_text $~, :monospaced, :constrained
    end
  end
  
  def document
    @document
  end

end

backtick_substitutor = BacktickSubstitutor.new(document)

benchmark 'quote-match' do
  backtick_substitutor.match(sample_file_text)
end

benchmark 'quote-sub' do
  backtick_substitutor.sub(sample_file_text.dup)
end

benchmark 'join-lines' do
  lines * '\n'
end

benchmark 'convert' do
  document.convert
end
