require 'url_safe_base64'
require 'oidc/config'

class JwkController < ApplicationController

  def index
    key = OpenSSL::PKey::RSA.new(File.read(OIDC::Config.rsa_private_key))

    jwk = {
        kty: :RSA,
        e: UrlSafeBase64.encode64(key.e.to_s(2)),
        n: UrlSafeBase64.encode64(key.n.to_s(2)),
    }

    render :json => JSON.pretty_generate(jwk)
  end
end
