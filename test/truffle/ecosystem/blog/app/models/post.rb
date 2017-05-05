class Post < ActiveRecord::Base

  validates :title, presence: true
  validates :author, presence: true

  def document
    @document ||= Asciidoctor.load(body || '', safe: :safe, icons: :font)
  end

  def body=(value)
    write_attribute(:body, value)
    @document = nil
  end

  def html_body
    document.convert
  end

  def title
    header = document.header
    header.title if header
  end

  def author
    document.author
  end

  def as_json(options = nil)
    { title: title, author: author, body: body }
  end

end
