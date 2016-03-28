# Sample OpenID Connect Provider
[![Build Status](https://travis-ci.org/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op.svg?branch=master)](https://travis-ci.org/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op)
[![Coverage Status](https://img.shields.io/coveralls/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op.svg)](https://coveralls.io/r/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op?branch=master)
[![Code Climate](https://codeclimate.com/github/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op/badges/gpa.svg)](https://codeclimate.com/github/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-op)

OpenID Connect Implicit Flow を実装したプロバイダのサンプルアプリケーションです。

http://openid-foundation-japan.github.io/openid-connect-core-1_0.ja.html

## インストール

````
 $ git clone https://github.com/openid-foundation-japan/eiwg-guideline-samples.git

 $ cd eiwg-guideline-samples/sample-ruby-op
 
 $ bundle install --path=vendor/bundle
````

## 必要な事前準備
- 鍵の生成

````
 $ mkdir config/keys

 $ cd config/keys

 $ openssl genrsa 2048 > test.pem

````

- 設定値の指定 (以下サンプル)

````.rb
# config/environments/development.rb

require './lib/oidc'

Rails.application.configure do

  OIDC::Config.issuer = 'op.example.com'
  OIDC::Config.scope = %w(openid profile email)
  OIDC::Config.id_token_expire = 3600
  OIDC::Config.rsa_key = './config/keys/test.pem'
end

````

## 動作確認

1. Rails アプリケーションの起動

````
 $ bundle exec rake db:migrate
 $ bundle exec rails s
````

2. ``` http://localhost:3000/applications ``` にアクセスして RP を登録して下さい。

3. sign_in 画面にリダイレクトされるので、その画面から sign_up のリンクをクリックしユーザを登録して下さい。

4. ユーザ登録完了後、RP の登録ページに戻ると思いますので、自身のRPを登録して下さい。


## OPの仕様

### Authorization リクエストパラメータ
このプロバイダでは以下のリクエストパラメータをサポートしています。


|パラメータ    |内容|
|:-------------|:---|
|scope         |[必須] `openid`を含むこと。openid,profile,email をサポート|
|response_type |[必須] `id_token`のみサポート。|
|client_id     |[必須] RPのクライアント識別子。|
|redirect_uri  |[必須] レスポンスが返されるリダイレクト先URI文字列。RP登録時のURI群のいずれかに*完全一致*する必要がある。|
|state         |[必須] CSRF対策の文字列。|
|nonce         |[必須] リプレイ攻撃対策の文字列。指定した文字列がIDトークンのnonceフィールドに格納される。|


### Authorization レスポンスパラメータ

Authorization リクエストに成功すると、以下のパラメータがリダイレクトURLの*フラグメント*として返されます。

|パラメータ    |内容|
|:-------------|:---|
|token_type    |`Bearer`固定|
|id_token      |IDトークン文字列。|
|state         |認可リクエストで`state`を指定していた場合のみ、その値が格納される。|


### IDトークンに含まれるクレーム
このプロバイダ提供するIDトークンには、下記のクレームが含まれます。

|クレーム名 |内容  |
|:----------|:-----|
|iss        |IDトークンを発行したOPの識別子。文字列。|
|sub        |ユーザの識別子。|
|aud        |このIDトークンの発行対象の識別子の配列。 |
|exp        |IDトークンが執行する時刻。整数値。 |
|iat        |IDトークンが発行された時刻。整数値。 |
|nonce      |[`nonce`を指定したリクエストの場合のみ] リプレイ攻撃対策として使われる文字列。 |
|userinfo   |UserInfoから返却される結果と同等のclaimsをIDトークン内に含めています |
