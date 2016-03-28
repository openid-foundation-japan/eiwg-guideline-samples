class ApplicationController < ActionController::Base

  include State
  include Nonce

  rescue_from(
      Rack::OAuth2::Client::Error,
      OpenIDConnect::Exception,
      OpenSSL::SSL::SSLError
  ) do |e|
    flash[:error] = if e.message.length > 2000
                      'Unknown Error'
                    else
                      e.message
                    end
    redirect_to root_url
  end

  protect_from_forgery with: :exception

end
