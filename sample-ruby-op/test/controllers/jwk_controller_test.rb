require 'test_helper'

class JwkControllerTest < ActionController::TestCase

  test 'controller has index action' do
    get :index
    assert_equal 200, response.status
  end

  test 'controller should return JSON' do
    get :index
    res = JSON.parse(response.body).with_indifferent_access
    assert res[:kty].present?
    assert res[:e].present?
    assert res[:n].present?
  end
end

