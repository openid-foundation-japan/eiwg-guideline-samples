# -*- coding: utf-8 -*-

FactoryGirl.define do
  factory :test_application, class: Application do
    name 'test application'
    client_id '1234'
    client_secret 'abcd'
    redirect_uri 'http://localhost:3000/callback'
  end
end
