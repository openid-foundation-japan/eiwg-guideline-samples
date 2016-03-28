json.array!(@applications) do |application|
  json.extract! application, :id, :name, :client_id, :client_secret, :redirect_uri
  json.url application_url(application, format: :json)
end
