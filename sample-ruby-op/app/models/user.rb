class User < ActiveRecord::Base
  devise :database_authenticatable, :registerable,
         :recoverable, :rememberable, :trackable, :validatable

  def as_json(attr)

    keys = []
    attr.each do |scope|
      keys.concat self.send("#{scope.to_s}_as_keys")
    end

    keys.inject({}) do |hash, key|
      hash.merge!(
          key => self.send(key)
      )
    end
  end

  private

  def openid_as_keys
    [:sub]
  end

  def profile_as_keys
    [:name, :given_name, :family_name, :given_name_kana, :family_name_kana, :gender]
  end

  def email_as_keys
    [:email]
  end

end
