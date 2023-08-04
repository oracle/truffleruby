# frozen_string_literal: true

class Net::IMAP::XOauth2Authenticator
  def process(_data)
    build_oauth2_string(@user, @oauth2_token)
  end

  private

  def initialize(user, oauth2_token)
    @user = user
    @oauth2_token = oauth2_token
  end

  def build_oauth2_string(user, oauth2_token)
    format("user=%s\1auth=Bearer %s\1\1", user, oauth2_token)
  end

  Net::IMAP.add_authenticator 'XOAUTH2', self
end
