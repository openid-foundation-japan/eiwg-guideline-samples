Rails.application.routes.draw do

  devise_for :users, :controllers => { registrations: 'registrations' }
  resources :applications

  root 'applications#index'

  get  'authorization' => 'authorizations#new'
  post 'authorization' => 'authorizations#create'

  get 'jwks' => 'jwk#index'

end
