# -*- coding: utf-8 -*-
require 'base64'

class Authorization < ActiveModelBase

  include State
  include Nonce

  attr_accessor :issuer, :identifier, :jwks_uri,
                :userinfo_endpoint,
                :authorization_endpoint,
                :redirect_uri,
                :jwk_signing_key,
                :x509_signing_key

  # Authorization Endpoint の URI をリクエストパラメータ付きで返却します。
  # @see http://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#AuthorizationEndpoint
  #
  # @param [String] nonce (number used once) リプレイアタックを軽減するために用いられる文字列。
  # @return [String] authorization_uri
  def authorization_uri(state, nonce)
    client.redirect_uri ||= redirect_uri
    if userinfo_endpoint
      response_type = [:id_token, :token]
    else
      response_type = [:id_token]
    end
    client.authorization_uri(
        response_type: response_type.collect(&:to_s),
        state: state,
        nonce: nonce,
        scope: [:openid, :email, :profile].collect(&:to_s)
    )
  end

  # Authorization Endpoint から Redirection URI フラグメントのパラメータを検証します。
  #
  # @param [Hash] fragment URIフラグメントのパラメータ
  # @param [String] nonce
  def validate(fragment, state, nonce)

    #stateパラメータのチェック
    unless fragment['state'] == state then
      Rails.logger.warn 'invalid state parameter.'
      return false
    end

    begin
      #FIXME: handle verify error
      _id_token = fragment['id_token']
      id_token = decode_id_token(_id_token)
      id_token.verify!(issuer: issuer, client_id: identifier, nonce: nonce)
      oidc.id_token = id_token.raw_attributes.with_indifferent_access

      if fragment['access_token']
        #TODO: validate access_token
        access_token = OpenIDConnect::AccessToken.new(fragment.merge({:client => client}))
        oidc.user_info = access_token.userinfo!
      else
        oidc.user_info = OpenIDConnect::ResponseObject::UserInfo.new(id_token.raw_attributes.with_indifferent_access)
      end
      return true
    rescue => e
      Rails.logger.warn e.message
      return false
    end
  end

  def oidc
    @oidc||= OIDC.new
  end

  private

  def client
    @client ||= OpenIDConnect::Client.new member_to_json
  end

  def member_to_json
    [:issuer,
     :identifier,
     :jwks_uri,
     :authorization_endpoint,
     :userinfo_endpoint].inject({}) do |hash, key|
      hash.merge!(
          key => self.send(key)
      )
    end
  end

  def decode_id_token(id_token)
    if jwks_uri
      header = JSON.parse(Base64.decode64(id_token.split('.').first)).with_indifferent_access
      kid = header[:kid]
      OpenIDConnect::ResponseObject::IdToken.decode id_token, public_keys_with_kid[kid]
    else
      OpenIDConnect::ResponseObject::IdToken.decode id_token, public_key_from_file
    end
  end

  def jwks
    @jwks ||= JSON.parse(OpenIDConnect.http_client.get_content(jwks_uri)).with_indifferent_access
    JSON::JWK::Set.new @jwks[:keys]
  end

  def public_key_from_file
    @public_key_from_file ||= lambda {
      if jwk_signing_key
        parse_jwk_key(jwk_signing_key)
      elsif x509_signing_key
        parse_x509_key(x509_signing_key)
      else
        raise 'could not find neither jwk_signing_key or x509_signing_key.'
      end
    }.call()
  end

  def public_keys_with_kid
    @public_keys_with_kid ||= lambda { |hash|
      jwks.each do |jwk|
        hash.merge!({jwk[:kid] => JSON::JWK.decode(jwk)})
      end
      hash
    }.call({})
  end

  def parse_jwk_key(key)
    json = JSON.parse(key)
    jwk = json['keys'].first
    create_rsa_key(jwk['n'], jwk['e'])
  end

  def create_rsa_key(mod, exp)
    key = OpenSSL::PKey::RSA.new
    exponent = OpenSSL::BN.new decode(exp)
    modulus = OpenSSL::BN.new decode(mod)
    key.e = exponent
    key.n = modulus
    key
  end

  def parse_x509_key(key)
    OpenSSL::X509::Certificate.new(key).public_key
  end
end
