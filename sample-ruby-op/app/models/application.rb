class Application < ActiveRecord::Base
  def self.has_redirect_uri(client_id, redirect_uri)
    Application.exists?({client_id: client_id, redirect_uri: redirect_uri})
  end
end
