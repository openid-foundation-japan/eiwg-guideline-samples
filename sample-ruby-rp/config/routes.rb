Rails.application.routes.draw do

  root 'top#index'
  get  '/after_login'  => 'top#after_login'
  get  '/after_logout' => 'top#after_logout'
  get  '/auth/:provider'    => 'authorization#authorize'
  get  '/callback' => 'authorization#callback'
  get  '/logout'   => 'authorization#logout'
  post '/catch_response' => 'authorization#validate'


end
