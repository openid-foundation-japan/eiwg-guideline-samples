# Sample OpenID Connect Implicit Flow Relaying Party
[![Build Status](https://travis-ci.org/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp.svg?branch=master)](https://travis-ci.org/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp)
[![Coverage Status](https://img.shields.io/coveralls/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp.svg)](https://coveralls.io/r/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp?branch=master)
[![Code Climate](https://codeclimate.com/github/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp/badges/gpa.svg)](https://codeclimate.com/github/openid-foundation-japan/eiwg-guideline-samples/sample-ruby-rp)


*このアプリケーションは 'https://github.com/nov/openid_connect_sample_rp' を参考にしています。*


OpenID Connect Implicit Flow を実装した Relaying Party のサンプルアプリケーションです。

このアプリケーションは OP として ```sample-ruby-op``` と連携するためのサンプルを目的としていますので、
その他OPと連携した場合に正常に動作しない場合があります。


## インストール
````.shell
$ git clone https://github.com/openid-foundation-japan/eiwg-guideline-samples.git

$ cd eiwg-guideline-samples/sample-ruby-rp
 
$ bundle install --path=vendor/bundle
 
````

## 動作確認

1. Rails アプリケーションの起動
````.shell
$ CLIENT_ID=<YOUR CLIENT_ID> CALLBACK_URL=<YOUR CALLBACK URL> bundle exec rails s -p 5000 
````

2. ```http://localhost:5000/``` にアクセスして下さい。
