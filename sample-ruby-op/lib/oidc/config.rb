module OIDC
  module Config
    mattr_accessor :issuer, :scope, :id_token_expire, :rsa_private_key
  end
end
