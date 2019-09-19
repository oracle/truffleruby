require 'test_helper'

class PostsControllerTest < ActionDispatch::IntegrationTest
  test "should get index" do
    get '/posts'
    assert_response :success

    assert_match /Post 1/, @response.body
    assert_match /Post 2/, @response.body
  end

  test "should get show" do
    get post_path id: 1
    assert_response :success
    assert_match /Post 1/, @response.body
  end

  test "should get new" do
    get new_post_path
    assert_response :success
  end

  test "should post create" do
    new_body = "= Title\nauthor\n\nBody"
    post '/posts', params: { body: new_body }
    assert_equal 3, Post.count
    assert_redirected_to '/posts/3'
  end

  test "should delete destroy" do
    delete post_path id: 1
    assert_redirected_to action: :index
    assert_equal Post.all, [Post.find(2)]
  end

  test "should get edit" do
    get post_path id: 1
    assert_response :success
  end

  test "should put update" do
    new_body = "= Title\nauthor\n\nBody"
    put post_path id: 1, body: new_body
    assert_redirected_to post_path(1)
    assert_equal Post.find(1).body, new_body
  end

  test "should delete destroy_all" do
    delete destroy_all_posts_path
    assert_redirected_to action: :index
    assert_predicate Post.all, :empty?
  end

end
