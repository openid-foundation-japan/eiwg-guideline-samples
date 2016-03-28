require 'test_helper'

class UserTest < ActiveSupport::TestCase

  setup do
    @user = FactoryGirl.create(:test_user)
  end

  %w(openid profile email).each do |attr|
    arg = []
    test "as_json with arguments=[#{attr}] should return Hash value" do
      arg << attr
      result = @user.as_json arg
      size = @user.send("#{attr}_as_keys").size

      assert_equal Hash, result.class
      assert_equal size, result.size
    end
  end

  test 'as_json with some Array value should return Hash value' do
    attr = %w(openid profile email)
    result = @user.as_json attr
    assert_equal Hash, result.class
  end

end
