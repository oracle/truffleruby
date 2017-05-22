class PostsController < ApplicationController

  # stubbed, will not work
  skip_before_filter :verify_authenticity_token

  def index
    @posts = Post.order(:created_at).all
  end

  def show
    @post = find_post
  end

  def new
    @post = Post.new get_attributes
    @post.valid?
  end

  def create
    post = Post.create get_attributes
    if post.valid?
      redirect_to action: :show, id: post.id
    else
      @post = post
      render action: :new
    end
  end

  def destroy
    Post.find(params['id']).destroy
    redirect_to action: :index
  end

  def edit
    @post = find_post
    @post.assign_attributes get_attributes if params.key? :body
    @post.valid?
  end

  def update
    post = find_post
    if post.update get_attributes
      redirect_to action: :show, id: post.id
    else
      @post = post
      render action: :edit
    end
  end

  def destroy_all
    Post.destroy_all
    redirect_to action: :index
  end

  private

  def find_post
    Post.find(params['id'])
  end

  def get_attributes
    { body: params[:body] }
  end


end
