# -*- coding: utf-8 -*-
require 'uri'
require 'oidc/error_response'
require 'oidc/config'

module OIDC
  class Request

    # @return [String] クライアントID
    attr_reader :client_id

    # @return [String] レスポンスタイプ
    attr_reader :response_type

    # @return [String] リダイレクトURI
    attr_reader :redirect_uri

    # @return [String] スコープ値
    attr_reader :scope

    # @return [String] state値
    attr_reader :state

    # @return [String] nonce値
    attr_reader :nonce

    # @return [Hash] error エラーレスポンス
    # @option error[String] error エラーコード
    # @option error[String] error_description エラーの説明
    attr_reader :error

    # @see http://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html#ImplicitAuthRequest
    #
    # @param [Hash] opts
    # @option opts [String] :client_id クライアントID
    # @option opts [String] :response_type レスポンスタイプ
    # @option opts [String] :redirect_uri リダイレクトURI
    # @option opts [String] :scope スコープ値
    # @option opts [String] :state CSRF対策のパラメータ
    # @option opts [String] :nonce ID_Token検証用パラメータ
    def initialize(opts)
      @client_id     = opts[:client_id]
      @response_type = opts[:response_type]
      @redirect_uri  = opts[:redirect_uri]
      @scope         = opts[:scope]
      @state         = opts[:state]
      @nonce         = opts[:nonce]
    end

    # 指定されたscopeを配列にして返却します。
    #
    # @return [Array] scopeを空白スペースで区切った配列
    def scopes
      scope.split(' ')
    end

    # リクエストパラメータを検証します。
    #
    # @return [true] 正しいリクエストパラメータ
    # @return [false] 不正なリクエストパラメータ
    def valid?
      validate
      @error.nil?
    end

    private

    # Authorizaion Request のパラメータに対してそれぞれチェック
    def validate
      @error = nil
      [:response_type, :redirect_uri, :scope, :state, :nonce].each do |key|
        break if @error
        @error = OIDC::ErrorResponse.send("build_#{key}_error") unless send "validate_#{key}"
      end
    end

    # サポートしている response_type か確認
    def validate_response_type
      #MEMO: 今回は Implicit Flow で ID Tokenのみの利用に固定
      response_type == 'id_token'
    end

    # 事前登録済みの redirect_uri であるか確認
    def validate_redirect_uri
      if Application.exists?(:client_id => client_id)
        res = false
        app = Application.where(:client_id => client_id).first
        app.redirect_uri.each_line do |uri|
          break if res
          res = URI.parse(uri.chomp) == URI.parse(redirect_uri)
        end
        res
      else
        return false
      end
    end

    # サポートしている scope の値か確認
    def validate_scope
      res = true
      scopes.each do |key|
        break unless res
        res = OIDC::Config.scope.include? key
      end
      res
    end

    def validate_state
      state.present?
    end

    def validate_nonce
      nonce.present?
    end
  end
end

