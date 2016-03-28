class RegistrationsController < Devise::RegistrationsController

  def create
    params[:user][:sub] = subject
    super
  end

  private

  def subject
    SecureRandom.uuid
  end

  def sign_up_params
    params.require(:user).permit(:sub,
                                 :name,
                                 :given_name,
                                 :family_name,
                                 :given_name_kana,
                                 :family_name_kana,
                                 :gender,
                                 :email,
                                 :password,
                                 :password_confirmation)
  end

  def account_update_params
    params.require(:user).permit(:name,
                                 :given_name,
                                 :family_name,
                                 :given_name_kana,
                                 :family_name_kana,
                                 :gender,
                                 :email,
                                 :password,
                                 :password_confirmation,
                                 :current_password)
  end

end