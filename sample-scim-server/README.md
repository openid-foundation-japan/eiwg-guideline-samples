# Sample SCIM Server

SCIM 2.0を実装したサンプルアプリケーションです。

## 概要

* 開発環境はeclipseによるTomcatプロジェクトのJavaサーブレット
* 動作環境はTomcat7 + Java7
* JSONの解析にはJACKSONを利用
* 1アプリケーション1テナント
* リソース情報はローカルファイルから読み込みオンメモリで保持
* ユーザーリソースの検索 (GET) / 追加 (POST) / 更新 (PUT) / 削除 (DELETE) を実装

## eclipseへのプロジェクトインポート方法

1. eclipseのメニューから [ファイル]→[インポート] を選択する。

2. 選択画面から [一般]→[既存プロジェクトをワークスペースへ] を選択する。

3. インポート画面の [ルートディレクトリの選択] テキストボックスに、sample-scim-serverフォルダを入力する。

4. インポート画面の [プロジェクト] に表示されるscimプロジェクトを選択する。

5. [完了] ボタンを押下してプロジェクトをインポートする。

利用するeclipseのバージョンによっては画面の表示名称が異なる場合がある。使用しているeclipseに合わせて適宜読み替えていただきたい。

## SCIMサーバの利用法

1. eiwg.warファイルをTomcatに配備する。(warファイルの配備方法はTomcatのドキュメント等を参照)

2. WEB-INFフォルダ直下に、各種設定ファイルが配置される。管理者の情報や、スキーマ情報、初期ユーザー情報等を変更する場合は、設定ファイル変更後にWeb アプリケーションを再起動する。

3. BaseURLは「http://<ご利用のサーバー名>:<ご利用のポート番号>/eiwg/scim」である。curlコマンド等のHTTPクライアントを利用して各種エンドポイントへアクセスして利用する。

### curl コマンドによるユーザーリソース取得の例

~~~
$ curl http://scim.svc.example.net:8080/eiwg/scim/Users -H "Authorization: Bearer MDNhNTlhN2ItYmE1NC00OWQ3LWFkMzQtODliYjhhN2Q0Mjlh"
~~~
