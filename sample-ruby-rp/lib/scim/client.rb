module SCIM
  module Client

    class User
      def self.find_by_email(email, tenant)
        server = Settings.scim.server
        path = "#{tenant}/scim/Users"
        endpoint = server + path
        filter = "externalId eq #{email}"

        client = HTTPClient.new
        client.set_auth(server, Settings.scim.auth.user, Settings.scim.auth.password)

        begin
          response = client.get_content(endpoint, {:filter => filter})
          return JSON.parse(response)
        rescue => e
          # FIXME: handle error
          raise e
        end
      end
    end

  end
end
