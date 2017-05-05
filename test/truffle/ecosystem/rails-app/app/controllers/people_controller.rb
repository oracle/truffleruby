class PeopleController < ApplicationController

  # stubbed, will not work
  skip_before_filter :verify_authenticity_token

  def index
    @people = RedisPerson.all
    @person = RedisPerson.new(name: 'test')

    respond_to do |format|
      format.json { render json: @people }
      format.html
    end
  end

  def create
    person = RedisPerson.create name: params[:name], email: params[:email]

    respond_to do |format|
      format.json { render json: person }
      format.html { redirect_to action: :index }
    end
  end

  def destroy
    person = RedisPerson.find params[:id]
    person.destroy

    respond_to do |format|
      format.json { render json: person }
      format.html { redirect_to action: :index }
    end
  end

  def destroy_all
    RedisPerson.all.each(&:destroy)

    respond_to do |format|
      format.json { render json: true }
      format.html { redirect_to action: :index }
    end
  end

  def platform
    respond_to do |format|
      format.json { render json: { platform: PLATFORM } }
    end
  end

end
