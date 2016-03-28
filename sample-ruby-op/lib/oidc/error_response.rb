module OIDC
  class ErrorResponse

    attr_reader :name, :description

    def initialize(name: 'error', description: '')
      @name = name
      @description = description
    end

    def with_fragment(redirect_uri)
      uri = URI.parse(redirect_uri)
      uri.fragment=(response.to_param)
      uri.to_s
    end

    def response
      {
        :error => name,
        :error_description => description
      }
    end

    def self.build_response_type_error
      new(name: 'invalid_request',
          description: 'invalid response_type')
    end

    def self.build_redirect_uri_error
      new(name: 'invalid_request',
          description: 'invalid redirect_uri')
    end

    def self.build_scope_error
      new(name: 'invalid_scope',
          description: 'invalid scope')
    end

    def self.build_state_error
      new(name: 'invalid_request',
          description: 'state parameter is required')
    end

    def self.build_nonce_error
      new(name: 'invalid_request',
          description: 'nonce parameter is required')
    end
  end
end
