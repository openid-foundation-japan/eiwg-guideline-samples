/**
 *
 * クラス名
 *          InitListener
 *
 * 概要
 *          Webアプリケーションの初期処理・終了処理クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Listener implementation class InitListener
 */
@WebListener
public class InitListener implements ServletContextListener {

    /**
     * Webアプリケーションの初期処理
     *
     * @param contextEvent イベント
     */
    @Override
    public void contextInitialized( ServletContextEvent contextEvent ) {

        // コンテキスト取得
        ServletContext context = contextEvent.getServletContext();

        // 実際にはDBやLDAP等をバックエンドに使用する事を推奨する
        // とりあえずローカルファイルをバックエンドにして実装
        ObjectMapper mapper = new ObjectMapper();

        Map < String, Object > serviceProviderConfigs = null;
        ArrayList < LinkedHashMap < String, Object > > resourceTypes = null;
        ArrayList < LinkedHashMap < String, Object > > schemas = null;
        ArrayList < LinkedHashMap < String, Object > > users = null;

        // サービスプロバイダ設定(ServiceProviderConfigs.json)読み込み
        try {
            serviceProviderConfigs = mapper.readValue( new File( context.getRealPath( "/WEB-INF/ServiceProviderConfigs.json" ) ),
                new TypeReference < LinkedHashMap < String, Object > >() {} );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // リソース種別設定(ResourceTypes.json)読み込み
        try {
            resourceTypes = mapper.readValue( new File( context.getRealPath( "/WEB-INF/ResourceTypes.json" ) ),
                new TypeReference < ArrayList < LinkedHashMap < String, Object > > >() {} );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // スキーマ設定(Schemas.json)読み込み
        try {
            schemas = mapper.readValue( new File( context.getRealPath( "/WEB-INF/Schemas.json" ) ),
                new TypeReference < ArrayList < LinkedHashMap < String, Object > > >() {} );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // ユーザ情報読み込み
        try {
            users = mapper.readValue( new File( context.getRealPath( "/WEB-INF/Users.json" ) ),
                new TypeReference < ArrayList < LinkedHashMap < String, Object > > >() {} );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // コンテキストに設定
        context.setAttribute( "ServiceProviderConfigs", serviceProviderConfigs );
        context.setAttribute( "ResourceTypes", resourceTypes );
        context.setAttribute( "Schemas", schemas );
        context.setAttribute( "Users", users );
    }

    /**
     * Webアプリケーションの終了処理
     *
     * @param contextEvent イベント
     */
    @Override
    public void contextDestroyed( ServletContextEvent contextEvent ) {

    }
}

