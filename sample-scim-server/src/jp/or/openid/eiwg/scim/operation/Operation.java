/**
 *
 * クラス名
 *          Operation
 *
 * 概要
 *          各種処理クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.scim.operation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;
import jp.or.openid.eiwg.scim.util.SCIMUtil;
import jp.or.openid.eiwg.scim.util.SCIMUtilException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Operation {
    private int errorCode = 0;
    private String errorType = "";
    private String errorMessage = "";

    /**
     * 認証処理
     *
     * @param context コンテキスト
     * @param request リクエスト
     */
    public boolean Authentication( ServletContext context, HttpServletRequest request ) throws IOException {
        boolean result = false;

        // Authorizationヘッダ取得
        String authorization = request.getHeader( "Authorization" );

        if ( authorization == null || StringUtils.isEmpty( authorization ) ) {
            // エラー
            setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_UNAUTHORIZED );
            //return false;
            return true;
        }

        String[] values = authorization.split( " " );

        // エラーコードエラーメッセージ初期化
        setError( 0, null, null );

        if ( values.length < 2 ) {
            // エラー
            setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_UNAUTHORIZED );
        }
        else {
            // 認証方式に応じて認証処理を行う
            if ( "Basic".equalsIgnoreCase( values[0] ) ) {
                // HTTP Basic 認証
                String userID;
                String password;
                String[] loginInfo = SCIMUtil.decodeBase64( values[1] ).split( ":" );

                if ( loginInfo.length < 2 ) {
                    // エラー
                    setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                }
                else {
                    // ユーザ名
                    userID = loginInfo[0];
                    // パスワード
                    password = loginInfo[1];

                    if ( StringUtils.isEmpty( userID ) || StringUtils.isEmpty( password ) ) {
                        // エラー
                        setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                    }
                    else {
                        // ユーザ名＆パスワードの検証処理

                        // 管理者情報読み込み
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayList < LinkedHashMap < String, Object > > adminInfoList = null;
                        try {
                            adminInfoList = mapper.readValue( new File( context.getRealPath( "/WEB-INF/Admin.json" ) ),
                                new TypeReference < ArrayList < LinkedHashMap < String, Object > > >() {} );
                        } catch ( IOException e ) {
                            // エラー
                            setError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, MessageConstants.ERROR_UNKNOWN );
                            e.printStackTrace();
                            return result;
                        }

                        if ( adminInfoList != null && !adminInfoList.isEmpty() ) {
                            Iterator < LinkedHashMap < String, Object > > adminInfoIt = adminInfoList.iterator();
                            while ( adminInfoIt.hasNext() ) {
                                LinkedHashMap< String, Object > adminInfo = adminInfoIt.next();
                                Object adminID = SCIMUtil.getAttribute( adminInfo , "id" );
                                Object adminPassword = SCIMUtil.getAttribute( adminInfo , "password" );
                                // idが一致するかチェック
                                if ( adminID != null && adminID instanceof String ) {
                                    if ( userID.equals( adminID.toString() ) ) {
                                        // passwordが一致するかチェック
                                        if ( adminID != null && adminID instanceof String ) {
                                            if ( password.equals( adminPassword.toString() ) ) {
                                                // 認証成功
                                                result = true;
                                            }
                                        }

                                        break;
                                    }
                                }
                            }

                            if ( result != true ) {
                                // エラー
                                setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                            }
                        }
                        else {
                            // エラー
                            setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                        }
                    }
                }
            }
            else if ( "Bearer".equalsIgnoreCase( values[0] ) ) {
                // OAuth2 Bearer トークン
                String token = values[1];

                if ( StringUtils.isEmpty( token ) ) {
                    // エラー
                    setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                }
                else {
                    // アクセストークンの検証処理

                    // 管理者情報読み込み
                    ObjectMapper mapper = new ObjectMapper();
                    ArrayList < LinkedHashMap < String, Object > > adminInfoList = null;
                    try {
                        adminInfoList = mapper.readValue( new File( context.getRealPath( "/WEB-INF/Admin.json" ) ),
                            new TypeReference < ArrayList < LinkedHashMap < String, Object > > >() {} );
                    } catch ( IOException e ) {
                        // エラー
                        setError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, MessageConstants.ERROR_UNKNOWN );
                        e.printStackTrace();
                        return result;
                    }

                    if ( adminInfoList != null && !adminInfoList.isEmpty() ) {
                        Iterator < LinkedHashMap < String, Object > > adminInfoIt = adminInfoList.iterator();
                        while ( adminInfoIt.hasNext() ) {
                            LinkedHashMap< String, Object > adminInfo = adminInfoIt.next();
                            Object adminToken = SCIMUtil.getAttribute( adminInfo , "bearer" );
                            // tokenが一致するかチェック
                            if ( adminToken != null && adminToken instanceof String ) {
                                if ( token.equals( adminToken.toString() ) ) {
                                    // 認証成功
                                    result = true;

                                    break;
                                }
                            }
                        }

                        if ( result != true ) {
                            // エラー
                            setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                        }
                    }
                    else {
                        // エラー
                        setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
                    }
                }
            }
            else {
                // エラー
                setError( HttpServletResponse.SC_UNAUTHORIZED, null, MessageConstants.ERROR_INVALID_CREDENTIALS );
            }
        }

        return result;
    }

    /**
     * ユーザ情報検索
     *
     * @param context
     * @param request
     * @param targetId
     * @param attributes
     * @param filter
     * @param sortBy
     * @param sortOrder
     * @param startIndex
     * @param count
     */
    public ArrayList< LinkedHashMap< String, Object > > searchUserInfo( ServletContext context, HttpServletRequest request, String targetId, String attributes, String filter, String sortBy, String sortOrder, String startIndex, String count ) {
        ArrayList< LinkedHashMap< String, Object > > result = null;

        Set<String> returnAttributeNameSet = new HashSet<>();

        // エラーコードエラーメッセージ初期化
        setError( 0, null, null );

        // 取得属性チェック
        if ( attributes != null && !attributes.isEmpty() ) {
            // カンマ区切り
            String[] tempList = attributes.split( "," );
            for ( int i = 0; i < tempList.length; i++ ) {
                String attributeName = tempList[ i ].trim();
                // 定義されている属性名かチェック
                LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
                if ( attributeSchema != null && !attributeSchema.isEmpty() ) {
                    returnAttributeNameSet.add( attributeName );
                }
                else {
                    // 定義されていない属性名
                    String message = String.format( MessageConstants.ERROR_INVALID_ATTRIBUTES, attributeName );
                    setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                    return result;
                }
            }
        }

        // ソート＆ページングを実装する場合には以下のチェックが必要
        // ・ソートキー(sortBy)チェック
        // ・ソート順(sortOrder)チェック
        // ・ページ番号(startIndex)チェック
        // ・1ページ中の検索結果数(count)チェック
        //
        // (未実装)

        // 検索
        result = new ArrayList< LinkedHashMap< String, Object > >();

        // ユーザ情報取得
        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > users = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Users" );
        if ( users != null && !users.isEmpty() ) {
            Iterator < LinkedHashMap < String, Object > > usersIt = users.iterator();
            while ( usersIt.hasNext() ) {
                boolean isMatched = false;
                LinkedHashMap< String, Object > userInfo = usersIt.next();

                // idの指定の有無によって処理を分岐
                if ( targetId != null && !targetId.isEmpty() ) {
                    Object id = SCIMUtil.getAttribute( userInfo , "id" );
                    if ( id != null && id instanceof String ) {
                        // idが一致するかチェック
                        if ( targetId.equals( id.toString() ) ) {
                            if ( filter != null && !filter.isEmpty() ) {
                                // フィルタが一致するかチェック
                                boolean matched = false;
                                try {
                                    matched = SCIMUtil.checkUserSimpleFilter( context, userInfo, filter );
                                }
                                catch ( SCIMUtilException e ) {
                                    result = null;
                                    setError( e.getCode(), e.getType(), e.getMessage() );
                                    break;
                                }

                                if ( matched ) {
                                    isMatched = true;
                                }
                            }
                            else {
                                isMatched = true;
                            }
                        }
                    }
                }
                else {
                    if ( filter != null && !filter.isEmpty() ) {
                        // フィルタが一致するかチェック
                        boolean matched = false;
                        try {
                            matched = SCIMUtil.checkUserSimpleFilter( context, userInfo, filter );
                        }
                        catch ( SCIMUtilException e ) {
                            result = null;
                            setError( e.getCode(), e.getType(), e.getMessage() );
                            break;
                        }

                        if ( matched ) {
                            isMatched = true;
                        }
                    }
                    else {
                        isMatched = true;
                    }
                }

                if ( isMatched ) {
                    // 返却情報を作成
                    LinkedHashMap < String, Object > resultInfo = new LinkedHashMap < String, Object >();
                    Iterator < String > attributeIt = userInfo.keySet().iterator();
                    while( attributeIt.hasNext() ) {
                        // 属性名取得
                        String attributeName = attributeIt.next();

                        // スキーマ情報取得
                        LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
                        Object returned = attributeSchema.get( "returned" );

                        if ( returned != null && returned.toString().equalsIgnoreCase( "never" ) ) {
                            continue;
                        }

                        // 属性値取得
                        Object attributeValue = userInfo.get( attributeName );

                        resultInfo.put( attributeName, attributeValue );
                    }

                    result.add( resultInfo );
                }
            }
        }

        return result;
    }

    /**
     * ユーザ情報作成
     *
     * @param context
     * @param request
     * @param attributes
     * @param requestJson
     */
    public LinkedHashMap < String, Object > createUserInfo( ServletContext context, HttpServletRequest request, String attributes, String requestJson ) {
        LinkedHashMap < String, Object > result = null;

        Set<String> returnAttributeNameSet = new HashSet<>();

        // エラーコードエラーメッセージ初期化
        setError( 0, null, null );

        // 取得属性チェック
        if ( attributes != null && !attributes.isEmpty() ) {
            // カンマ区切り
            String[] tempList = attributes.split( "," );
            for ( int i = 0; i < tempList.length; i++ ) {
                String attributeName = tempList[ i ].trim();
                // 定義されている属性名かチェック
                LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
                if ( attributeSchema != null && !attributeSchema.isEmpty() ) {
                    returnAttributeNameSet.add( attributeName );
                }
                else {
                    // 定義されていない属性名
                    String message = String.format( MessageConstants.ERROR_INVALID_ATTRIBUTES, attributeName );
                    setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                    return result;
                }
            }
        }

        // リクエストボディ存在チェック
        if ( requestJson == null || requestJson.isEmpty() ) {
            // エラー
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST );
            return result;
        }

        // リクエスト(JSON)解析
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap < String, Object > requestObject = null;
        try {
            requestObject = mapper.readValue( requestJson, new TypeReference< LinkedHashMap < String, Object > >() {} );
        }
        catch ( JsonParseException e ) {
            String datailMessage = e.getMessage();
            datailMessage = datailMessage.substring( 0, datailMessage.indexOf( '\n' ) );
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST + "(" + datailMessage + ")" );
            return result;
        }
        catch ( JsonMappingException e ) {
            String datailMessage = e.getMessage();
            datailMessage = datailMessage.substring( 0, datailMessage.indexOf( '\n' ) );
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST + "(" + datailMessage + ")" );
            return result;
        }
        catch ( IOException e ) {
            setError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, MessageConstants.ERROR_UNKNOWN );
            return result;
        }

        // スキーマチェック
        if ( requestObject != null && !requestObject.isEmpty() ) {
            Iterator < String > attributeIt = requestObject.keySet().iterator();
            while( attributeIt.hasNext() ) {
                // 属性名取得
                String attributeName = attributeIt.next();
                // スキーマ情報取得
                LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
                if ( attributeSchema != null ) {
                    // 追加可能な属性かチェック
                    Object mutability = attributeSchema.get( "mutability" );
                    if ( mutability != null && mutability.toString().equalsIgnoreCase( "readOnly" ) ) {
                        // readOnly 属性
                        String message = String.format( MessageConstants.ERROR_READONLY_ATTRIBUTE, attributeName );
                        setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                        return result;
                    }

                    // 属性値の型チェック
                    // (未実装)
                }
                else {
                    // 不明な属性名
                    String message = String.format( MessageConstants.ERROR_UNKNOWN_ATTRIBUTE, attributeName );
                    setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                    return result;
                }
            }
        }
        else {
            // エラー
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST );
            return result;
        }

        // 必須属性チェック
        // (未実装)

        LinkedHashMap < String, Object > newUserInfo = new LinkedHashMap < String, Object >();

        // id属性生成
        UUID uuid = UUID.randomUUID();
        newUserInfo.put( "id", uuid.toString() );

        Iterator < String > attributeIt = requestObject.keySet().iterator();
        while( attributeIt.hasNext() ) {
            // 属性名取得
            String attributeName = attributeIt.next();
            // 属性値取得
            Object attributeValue = requestObject.get( attributeName );

            newUserInfo.put( attributeName, attributeValue );
        }

        // meta属性生成
        LinkedHashMap< String, Object > metaValues = new LinkedHashMap< String, Object >();
        // meta.resourceType 属性
        metaValues.put( "resourceType", "User" );
        // meta.created 属性
        SimpleDateFormat xsdDateTime = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S'Z'" );
        xsdDateTime.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        metaValues.put( "created", xsdDateTime.format( new Date() ) );
        // meta.location 属性
        String location = request.getScheme() + "://" + request.getServerName();
        int serverPort = request.getServerPort();
        if ( serverPort != 80 && serverPort != 443 ) {
            location += ":" + Integer.toString( serverPort );
        }
        location += request.getContextPath();
        location += "/scim/Users/" + uuid.toString();
        metaValues.put( "location", location );
        newUserInfo.put( "meta", metaValues );

        // ユーザ情報追加(排他が必要)
        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > users = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Users" );
        if ( users == null ) {
            users = new ArrayList < LinkedHashMap < String, Object > >();
        }
        users.add( newUserInfo );
        context.setAttribute( "Users", users );

        // 返却情報作成
        result = new LinkedHashMap < String, Object >();
        attributeIt = newUserInfo.keySet().iterator();
        while( attributeIt.hasNext() ) {
            // 属性名取得
            String attributeName = attributeIt.next();

            // スキーマ情報取得
            LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
            Object returned = attributeSchema.get( "returned" );

            if ( returned != null && returned.toString().equalsIgnoreCase( "never" ) ) {
                continue;
            }

            // 属性値取得
            Object attributeValue = newUserInfo.get( attributeName );

            result.put( attributeName, attributeValue );
        }

        return result;
    }

    /**
     * ユーザ情報更新
     *
     * @param context
     * @param request
     * @param attributes
     * @param requestJson
     */
    public LinkedHashMap < String, Object > updateUserInfo( ServletContext context, HttpServletRequest request, String targetId, String attributes, String requestJson ) {
        LinkedHashMap < String, Object > result = null;

        Set<String> returnAttributeNameSet = new HashSet<>();

        // エラーコードエラーメッセージ初期化
        setError( 0, null, null );

        // 取得属性チェック
        if ( attributes != null && !attributes.isEmpty() ) {
            // カンマ区切り
            String[] tempList = attributes.split( "," );
            for ( int i = 0; i < tempList.length; i++ ) {
                String attributeName = tempList[ i ].trim();
                // 定義されている属性名かチェック
                LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
                if ( attributeSchema != null && !attributeSchema.isEmpty() ) {
                    returnAttributeNameSet.add( attributeName );
                }
                else {
                    // 定義されていない属性名
                    String message = String.format( MessageConstants.ERROR_INVALID_ATTRIBUTES, attributeName );
                    setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                    return result;
                }
            }
        }

        // id存在チェック
        LinkedHashMap< String, Object > targetInfo = null;
        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > users = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Users" );
        Iterator < LinkedHashMap < String, Object > > usersIt = null;
        if ( users != null && !users.isEmpty() ) {
            usersIt = users.iterator();
            while ( usersIt.hasNext() ) {
                LinkedHashMap< String, Object > userInfo = usersIt.next();
                Object id = SCIMUtil.getAttribute( userInfo , "id" );
                if ( id != null && id instanceof String ) {
                    if ( targetId.equals( id.toString() ) ) {
                        targetInfo = userInfo;
                        break;
                    }
                }
            }
        }

        if ( targetInfo == null ) {
            setError( HttpServletResponse.SC_NOT_FOUND, null, MessageConstants.ERROR_NOT_FOUND );
            return result;
        }

        // リクエストボディ存在チェック
        if ( requestJson == null || requestJson.isEmpty() ) {
            // エラー
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST );
            return result;
        }

        // リクエスト(JSON)解析
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap < String, Object > requestObject = null;
        try {
            requestObject = mapper.readValue( requestJson, new TypeReference< LinkedHashMap < String, Object > >() {} );
        }
        catch ( JsonParseException e ) {
            String datailMessage = e.getMessage();
            datailMessage = datailMessage.substring( 0, datailMessage.indexOf( '\n' ) );
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST + "(" + datailMessage + ")" );
            return result;
        }
        catch ( JsonMappingException e ) {
            String datailMessage = e.getMessage();
            datailMessage = datailMessage.substring( 0, datailMessage.indexOf( '\n' ) );
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST + "(" + datailMessage + ")" );
            return result;
        }
        catch ( IOException e ) {
            setError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, MessageConstants.ERROR_UNKNOWN );
            return result;
        }

        // スキーマチェック
        if ( requestObject != null && !requestObject.isEmpty() ) {
            Iterator < String > attributeIt = requestObject.keySet().iterator();
            while( attributeIt.hasNext() ) {
                // 属性名取得
                String attributeName = attributeIt.next();
                // スキーマ情報取得
                LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, false );
                if ( attributeSchema != null ) {
                    // 更新可能な属性かチェック
                    Object mutability = attributeSchema.get( "mutability" );
                    if ( mutability != null && mutability.toString().equalsIgnoreCase( "readOnly" ) ) {
                        // readOnly 属性
                        String message = String.format( MessageConstants.ERROR_READONLY_ATTRIBUTE, attributeName );
                        setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                        return result;
                    }

                    // 属性値の型チェック
                    // (未実装)
                }
                else {
                    if ( !attributeName.equalsIgnoreCase( "schemas" ) && !attributeName.equalsIgnoreCase( "id" ) && !attributeName.equalsIgnoreCase( "meta" ) ) {
                        // 不明な属性名
                        String message = String.format( MessageConstants.ERROR_UNKNOWN_ATTRIBUTE, attributeName );
                        setError( HttpServletResponse.SC_BAD_REQUEST, null, message );
                        return result;
                    }
                }
            }
        }
        else {
            // エラー
            setError( HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_INVALID_REQUEST );
            return result;
        }

        // 必須属性チェック
        // (未実装)

        LinkedHashMap < String, Object > updateUserInfo = new LinkedHashMap < String, Object >();

        // 属性設定
        updateUserInfo.put( "id", targetId );

        Iterator < String > attributeIt = requestObject.keySet().iterator();
        while( attributeIt.hasNext() ) {
            // 属性名取得
            String attributeName = attributeIt.next();
            // 属性値取得
            Object attributeValue = requestObject.get( attributeName );

            updateUserInfo.put( attributeName, attributeValue );
        }

        // meta属性更新
        Object metaObject = targetInfo.get( "meta" );
        @SuppressWarnings("unchecked")
        LinkedHashMap< String, Object > metaValues = ( LinkedHashMap<String, Object> ) metaObject;
        // meta.lastModified 属性
        SimpleDateFormat xsdDateTime = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S'Z'" );
        xsdDateTime.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        metaValues.put( "lastModified", xsdDateTime.format( new Date() ) );

        updateUserInfo.put( "meta", metaValues );

        // ユーザ情報更新(排他が必要)
        usersIt.remove();
        users.add( updateUserInfo );
        context.setAttribute( "Users", users );

        // 返却情報作成
        result = new LinkedHashMap < String, Object >();
        attributeIt = updateUserInfo.keySet().iterator();
        while( attributeIt.hasNext() ) {
            // 属性名取得
            String attributeName = attributeIt.next();

            // スキーマ情報取得
            LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
            Object returned = attributeSchema.get( "returned" );

            if ( returned != null && returned.toString().equalsIgnoreCase( "never" ) ) {
                continue;
            }

            // 属性値取得
            Object attributeValue = updateUserInfo.get( attributeName );

            result.put( attributeName, attributeValue );
        }

        return result;
    }

    /**
     * ユーザ情報削除
     *
     * @param context
     * @param request
     * @param attributes
     * @param requestJson
     */
    public boolean deleteUserInfo( ServletContext context, HttpServletRequest request, String targetId ) {

        // エラーコードエラーメッセージ初期化
        setError( 0, null, null );

        // id存在チェック
        LinkedHashMap< String, Object > targetInfo = null;

        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > users = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Users" );
        Iterator < LinkedHashMap < String, Object > > usersIt = null;
        if ( users != null && !users.isEmpty() ) {
            usersIt = users.iterator();
            while ( usersIt.hasNext() ) {
                LinkedHashMap< String, Object > userInfo = usersIt.next();
                Object id = SCIMUtil.getAttribute( userInfo , "id" );
                if ( id != null && id instanceof String ) {
                    if ( targetId.equals( id.toString() ) ) {
                        targetInfo = userInfo;
                        break;
                    }
                }
            }
        }

        if ( targetInfo == null ) {
            // 存在しない
            setError( HttpServletResponse.SC_NOT_FOUND, null, MessageConstants.ERROR_NOT_FOUND );
            return false;
        }

        // ユーザ情報削除(排他が必要)
        usersIt.remove();
        context.setAttribute( "Users", users );

        return true;
    }

    /**
     * エラーコードおよびエラーメッセージを設定
     *
     * @param code HTTPステータスコード
     * @param message エラーメッセージ
     */
    private void setError( int code, String type, String message ) {
        errorCode = code;
        errorType = type;
        errorMessage = message;
    }

    /**
     * エラーコード取得
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * エラー種別取得
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * エラーメッセージ取得
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
