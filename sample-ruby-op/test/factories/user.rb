# -*- coding: utf-8 -*-

FactoryGirl.define do
  factory :test_user, class: User do
    sequence(:email) { |n| "test_#{n}@example.com"}
    password 'password'
    sub 'sub'
    given_name 'Test User'
    family_name 'User'
    given_name_kana 'テスト'
    family_name_kana 'ユーザ'
    gender 'male'
  end
end
