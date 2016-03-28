class ActiveModelBase
  include ActiveModel::Model

  def initialize(attr = {})
    attr.each do |key,value|
      send("#{key}=", value) rescue nil
    end
  end
end