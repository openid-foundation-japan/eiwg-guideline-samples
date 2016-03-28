# -*- coding: utf-8 -*-
class AuthorizationsController < ApplicationController

  require 'oidc/request'
  require 'oidc/response'

  before_action :authenticate_user!

  def new
    @req = OIDC::Request.new(params)
    if @req.valid?
      render :new
      return
    end

    if (params[:redirect_uri].present? &&
        Application.has_redirect_uri(params[:client_id], params[:redirect_uri]))
      redirect_to @req.error.with_fragment(params[:redirect_uri])
    else
      render json: @req.error.response
    end
  end

  # 同意画面からPOSTされる先
  def create
    res = OIDC::Response.new(params)
    res.owner = current_user

    redirect_to res.build_response
   end

end
