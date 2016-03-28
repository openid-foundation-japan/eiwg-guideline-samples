require 'test_helper'

class OIDC::RequestTest < Minitest::Test

  require 'oidc/request'

  def setup
    DatabaseCleaner.strategy = :truncation
    DatabaseCleaner.clean

    @application =FactoryGirl.create(:test_application)
    @params = {
      :response_type => 'id_token',
      :client_id => @application.client_id,
      :redirect_uri => @application.redirect_uri,
      :scope => 'openid',
      :state => 'state_123',
      :nonce => 'nonce_xyz',
    }
    super
  end

  # when class be initialize, instance variable should have been set
  def test_initialize
    request = OIDC::Request.new(@params)
    assert_equal @params[:response_type], request.response_type
    assert_equal @params[:client_id], request.client_id
    assert_equal @params[:redirect_uri], request.redirect_uri
    assert_equal @params[:scope], request.scope
    assert_equal @params[:state], request.state
    assert_equal @params[:nonce], request.nonce
  end

  def test_scopes
    request = OIDC::Request.new(@params)
    assert_equal Array, request.scopes.class
  end

  def test_scopes_with_some_scope_value
    @params[:scope] = 'openid profile email'
    request = OIDC::Request.new(@params)
    assert_equal Array, request.scopes.class
  end

  def test_valid_should_be_true
    request = OIDC::Request.new(@params)
    assert request.valid?
  end

  def test_valid_should_be_false_when_invalid_response_type
    @params[:response_type] = 'token'
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    assert_equal 'invalid response_type', request.error.description
  end

  def test_valid_should_be_false_when_response_type_is_nil
    @params[:response_type] = nil
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    assert_equal 'invalid response_type', request.error.description
  end

  def test_valid_should_be_false_when_invalid_client_id
    @params[:client_id] = 'client_id'
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    # because redirect_uri does not match client_id.
    assert_equal 'invalid redirect_uri', request.error.description
  end

  def test_valid_should_be_false_when_invalid_redirect_uri
    @params[:redirect_uri] = 'redirect_uri'
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    assert_equal 'invalid redirect_uri', request.error.description
  end

  def test_valid_should_be_false_when_invalid_scope
    @params[:scope] = 'foo'
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_scope', request.error.name
    assert_equal 'invalid scope', request.error.description
  end

  def test_valid_should_be_false_when_state_is_nil
    @params[:state] = nil
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    assert_equal 'state parameter is required', request.error.description
  end

  def test_valid_should_be_false_when_nonce_is_nil
    @params[:nonce] = nil
    request = OIDC::Request.new(@params)
    assert_equal false, request.valid?
    assert_equal 'invalid_request', request.error.name
    assert_equal 'nonce parameter is required', request.error.description
  end

end
