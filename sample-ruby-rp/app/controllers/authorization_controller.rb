# -*- coding: utf-8 -*-
class AuthorizationController < ApplicationController

  def authorize
    session[:provider] = params[:provider]
    redirect_to authz.authorization_uri(new_state, new_nonce)
  end

  def callback
    # @see http://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#FragmentNotes
  end

  def validate
    if params['error']
      logger.error "error=#{params['error']}, description=#{params['error_description']}"
      render :nothing => true, :status => 400 and return
    end

    unless authz.validate(params, stored_state, stored_nonce)
      render :nothing => true, :status => 400 and return
    end

    session[:identifier] = get_identifier(authz.oidc.user_info)
  end

  def logout
    delete_session!
    redirect_to root_url
  end

  private

  def authz
    @authz ||= Authorization.new oidc_param
  end

  def oidc_param
    if session[:provider].blank?
      # FIXME: railse error
      return default_param
    else
      return Settings.oidc[session[:provider]]
    end
  end

  def delete_session!
    session.delete(:identifier)
    session.delete(:provider)
  end

  def get_identifier(user_info)
    # 今回は email を identifier とする
    return user_info.email
  end

  def default_param
    Settings.oidc.corpA
  end
end
