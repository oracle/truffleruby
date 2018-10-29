require 'test_helper'

class PostTest < ActiveSupport::TestCase
  test "title is parsed" do
    assert_equal Post.find(1).title, "Post 1"
  end

  test "author is parsed" do
    assert_equal Post.find(1).author, "An author"
  end

  test "title and author are required" do
    post = Post.new(body: "")
    assert_not post.valid?
    assert_equal post.errors.details,
                 :title => [{ :error => :blank }],
                 :author => [{ :error => :blank }]
  end
end
