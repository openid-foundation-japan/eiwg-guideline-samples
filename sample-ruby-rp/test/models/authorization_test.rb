require 'test_helper'

class AuthorizationTest < ActiveSupport::TestCase

  require 'uri'
  require 'cgi'

  setup do
    @params = {
      :issuer => 'op.example.com',
      :identifier => 'client_id_12345',
      :jwks_uri => 'http://localhost:3000/jwks',
      :authorization_endpoint => 'http://localhost:3000/authorization',
      :token_endpoint => 'https://localhost:3000/token',
      :userinfo_endpoint => 'https://localhost:3000/userinfo',
      :redirect_uri => 'http://localhost:3001/callback'
    }
  end

  test 'authorization_uri_should_returen_uri_string' do
    authorization = Authorization.new(@params)
    result = authorization.authorization_uri('state_123', 'nonce_123')
    uri = URI.parse(result)
    query = CGI.parse(uri.query)

    assert_equal 'http', uri.scheme
    assert_equal 'localhost', uri.host
    assert_equal 3000, uri.port
    assert_equal '/authorization', uri.path
    assert_equal @params[:identifier], query['client_id'].first
    assert_equal 'id_token token', query['response_type'].first
    assert_equal @params[:redirect_uri], query['redirect_uri'].first
    assert_equal 'state_123', query['state'].first
    assert_equal 'nonce_123', query['nonce'].first
  end

  test 'validate_when_id_token_verify_ok_should_return_true' do
    authorization = Authorization.new(@params)
    stub_request(:any, 'localhost:3000/jwks').to_return(:body => File.read('./test/fixtures/jwks.json'))

    id_token = stub('OpenIDConnect::ResponseObject::IdToken')
    id_token.expects(:verify!).returns(true)
    id_token.expects(:raw_attributes).returns({'userinfo' => {'sub' => '123'}}).at_least_once
    Authorization.any_instance.stubs(:decode_id_token).returns(id_token)

    state = 'state_123'
    nonce = 'nonce_xyz'

    fragment_param = {
      'token_type' => 'Bearer',
      'id_token' => 'sample-id-token-string',
      'state' => state,
    }

    assert_equal true, authorization.validate(fragment_param, state, nonce)
  end

  test 'validate_when_invalid_state_should_raise_exception' do
    authorization = Authorization.new(@params)
    state = 'state_123'
    nonce = 'nonce_xyz'

    fragment_param = {
      'token_type' => 'Bearer',
      'id_token' => 'id_token_sample',
      'state' => state,
    }

    assert_equal false, authorization.validate(fragment_param, 'foo', nonce)
  end

  test 'validate_when_id_token_verify_ng_should_return_false' do
    authorization = Authorization.new(@params)
    stub_request(:any, 'localhost:3000/jwks').to_return(:body => File.read('./test/fixtures/jwks.json'))

    id_token = stub('OpenIDConnect::ResponseObject::IdToken')
    id_token.expects(:verify!).raises(OpenIDConnect::ResponseObject::IdToken::InvalidToken.new)
    Authorization.any_instance.stubs(:decode_id_token).returns(id_token)

    state = 'state_123'
    nonce = 'nonce_xyz'

    fragment_param = {
      'token_type' => 'Bearer',
      'id_token' => 'id_token_sample',
      'state' => state,
    }

    assert_equal false, authorization.validate(fragment_param, state, nonce)
  end
end
