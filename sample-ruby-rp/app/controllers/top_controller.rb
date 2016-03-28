# -*- coding: utf-8 -*-
class TopController < ApplicationController

  include SCIM::Client

  def index
    redirect_to '/after_login' if session[:identifier].present?
  end

  def after_login
    # ap session[:identifier]
    redirect_to root_url if session[:identifier].blank?
    @user_name = session[:identifier]
    # FIXME: rescue
    @scim = SCIM::Client::User.find_by_email(@user_name, session[:provider])
  end

  def after_logout
  end

end
