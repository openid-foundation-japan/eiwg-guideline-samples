# -*- coding: utf-8 -*-
require 'uri'
require 'url_safe_base64'
require 'oidc/config'

module OIDC
  class Response

    #TODO: error handling

    include OIDC::Config

    attr_accessor :owner, :scope
    attr_reader :client_id, :redirect_uri, :token_type, :state, :nonce

    def initialize(opts)
      @client_id    = opts[:client_id]
      @redirect_uri = opts[:redirect_uri]
      @token_type   = 'Bearer'
      @state        = opts[:state]
      @nonce        = opts[:nonce]
      @scope        = opts[:scope]
    end

    def scopes
      scope.split(' ')
    end

    # Authorization Request に対する Response を生成します
    #
    # @return [String] ハッシュフラグメントで構成されたパラメータを含むURI
    def build_response
      uri = URI.parse(@redirect_uri)
      uri.fragment=(build_params)
      uri.to_s
    end

    private

    def build_params
      [:token_type, :id_token,:state].inject('') {|str, key|
        str << "#{key}=#{self.send(key)}&"
      }.chop!
    end

    # ID Token(JWT) を生成します
    def id_token
      header = UrlSafeBase64.encode64({
         :typ => 'JWT',
         :alg => 'RS256',
      }.to_json)
      payload = UrlSafeBase64.encode64(token_data.to_json)
      input = header + '.' + payload
      signature = UrlSafeBase64.encode64(pkey.sign('sha256', input))
      input + '.' + signature
    end

    def pkey
      OpenSSL::PKey::RSA.new(File.read(OIDC::Config.rsa_private_key))
    end

    # ID Token のクレームを定義
    def token_data
      issue_at = Time.now.to_i
      exp = issue_at + id_token_expire
      return {
          :iss => issuer,
          :sub => owner.sub,
          :aud => @client_id,
          :exp => exp,
          :iat => issue_at,
          :nonce => @nonce,
          :userinfo => userinfo

      }
    end

    # ownerの情報に対してscopeで指定された情報のみ返却
    def userinfo
      owner.as_json scopes
    end
  end
end
