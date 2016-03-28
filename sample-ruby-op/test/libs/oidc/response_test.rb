require 'test_helper'

class OIDC::ResponseTest < Minitest::Test

  require 'uri'
  require 'cgi'
  require 'url_safe_base64'
  require 'oidc/response'

  OIDC::Config.issuer = 'op.example.com'
  OIDC::Config.scope = %w(openid profile email)
  OIDC::Config.id_token_expire = 3600
  OIDC::Config.rsa_private_key = './test/fixtures/keys/test.pem'

  def setup
    DatabaseCleaner.strategy = :truncation
    DatabaseCleaner.clean

    @application = FactoryGirl.create(:test_application)
    @user = FactoryGirl.create(:test_user)
    @params = {
      :client_id => @application.client_id,
      :redirect_uri => @application.redirect_uri,
      :state => 'state_123',
      :nonce => 'nonce_xyz',
      :scope => 'openid',
    }
    super
  end

  def test_initialize
    response = OIDC::Response.new(@params)
    assert_equal @params[:client_id], response.client_id
    assert_equal @params[:redirect_uri], response.redirect_uri
    assert_equal 'Bearer', response.token_type
    assert_equal @params[:state], response.state
    assert_equal @params[:nonce], response.nonce
  end

  def test_scopes
    response = OIDC::Response.new(@params)
    assert_equal Array, response.scopes.class
  end

  def test_scopes_with_some_scope_value
    @params[:scope] = 'openid profile email'
    response = OIDC::Response.new(@params)
    assert_equal Array, response.scopes.class
  end

  def test_build_response
    response = OIDC::Response.new(@params)
    response.owner = @user
    uri = URI.parse(response.build_response)
    res_params = CGI.parse(uri.fragment)

    assert res_params['token_type'].present?
    assert_equal res_params['token_type'].first, 'Bearer'
    assert res_params['id_token'].present?
    assert res_params['state'].present?
  end

  def test_id_token_veryfy
    response = OIDC::Response.new(@params)
    response.owner = @user
    uri = URI.parse(response.build_response)
    res_params = CGI.parse(uri.fragment)

    id_token = res_params['id_token'].first
    public_key = OpenSSL::PKey::RSA.new(File.read(OIDC::Config.rsa_private_key)).public_key

    jwt = id_token.split('.')
    header = JSON.parse(UrlSafeBase64.decode64(jwt[0])).with_indifferent_access
    payload = JSON.parse(UrlSafeBase64.decode64(jwt[1])).with_indifferent_access
    input = jwt[0] + '.' + jwt[1]
    signature = UrlSafeBase64.decode64(jwt[2])

    assert header[:typ].present?
    assert header[:alg].present?
    assert payload[:sub].present?
    assert payload[:userinfo].present?
    assert public_key.verify('sha256', signature, input)
  end
end
