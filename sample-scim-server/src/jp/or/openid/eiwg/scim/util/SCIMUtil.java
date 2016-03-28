/**
 *
 * クラス名
 *          SCIMUtil
 *
 * 概要
 *          ユーティリティ(部品)クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.scim.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;

import org.apache.commons.codec.binary.Base64;

public class SCIMUtil {

    /**
     * BASE64デコード
     *
     * @param val デコード対象の文字列
     * @return デーコード後の文字列
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static String decodeBase64( String val ) throws IOException {
        // BASE64のデコード
        val = val.replaceAll( "[\n|\r]", "" );
        String decodeString = new String( Base64.decodeBase64( val ), "UTF-8" );

        // デコードされた値が正しいかチェック
        String encodeString = encodeBase64( decodeString );
        if ( encodeString.compareToIgnoreCase( val ) != 0 ) {
            decodeString = "";
        }

        // BASE64デコード結果を返す
        return decodeString;
    }

    /**
     * BASE64エンコード
     *
     * @param val エンコード対象の文字列
     * @return エンコード後の文字列
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static String encodeBase64( String val ) throws UnsupportedEncodingException {
        return new String( Base64.encodeBase64( val.getBytes( "UTF-8" ) ) );
    }

    /**
     * リソース情報から指定された属性の情報を取得する
     *
     * @param resourceInfo 既存情報
     * @param attributeName 取得対象属性名
     * @return 属性情報
     */
    public static Object getAttribute( Map < String, Object > resourceInfo, String attributeName ) {
        Object result = null;

        Iterator < String > attributeNameIt = resourceInfo.keySet().iterator();
        while ( attributeNameIt.hasNext() ) {
            String name = attributeNameIt.next();
            if ( name.equalsIgnoreCase( attributeName ) ) {
                result = resourceInfo.get( name );
                break;
            }
        }

        return result;
    }

    /**
     * ユーザリソースの指定された属性の定義情報を取得する
     *
     * @param context
     * @param attributeName
     * @param isCore
     * @return 属性の定義情報
     */
    public static LinkedHashMap< String, Object > getUserAttributeInfo( ServletContext context, String attributeName, boolean isCore ) {
        LinkedHashMap< String, Object > result = null;

        Set<String> schemaIdSet = new HashSet<>();

        // リソース種別設定取得
        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > resourceTypes = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "ResourceTypes" );
        Iterator < LinkedHashMap < String, Object > > resourceTypesIt = resourceTypes.iterator();
        while ( resourceTypesIt.hasNext() ) {
            LinkedHashMap < String, Object > resourceTypeInfo = resourceTypesIt.next();
            Object endpoint = SCIMUtil.getAttribute( resourceTypeInfo, "endpoint" );
            if ( endpoint != null && endpoint.toString().equalsIgnoreCase( "/Users" ) ) {
                Object schema = SCIMUtil.getAttribute( resourceTypeInfo, "schema" );
                if ( schema != null ) {
                    schemaIdSet.add( schema.toString() );
                }

                Object schemaExtensions = SCIMUtil.getAttribute( resourceTypeInfo, "schemaExtensions" );
                if ( schemaExtensions != null && schemaExtensions instanceof ArrayList ) {
                    @SuppressWarnings("unchecked")
                    ArrayList < LinkedHashMap < String, Object > > schemaExtensionList = (ArrayList < LinkedHashMap < String, Object > > ) schemaExtensions;
                    Iterator < LinkedHashMap < String, Object > > schemaExtensionListIt = schemaExtensionList.iterator();
                    while ( schemaExtensionListIt.hasNext() ) {
                        LinkedHashMap < String, Object > schemaExtensionInfo = schemaExtensionListIt.next();
                        schema = SCIMUtil.getAttribute( schemaExtensionInfo, "schema" );
                        if ( schema != null ) {
                            schemaIdSet.add( schema.toString() );
                        }
                    }
                }

                break;
            }
        }

        // スキーマ設定取得
        @SuppressWarnings("unchecked")
        ArrayList < LinkedHashMap < String, Object > > schemas = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Schemas" );

        Iterator < LinkedHashMap < String, Object > > schemasIt = schemas.iterator();
        while ( schemasIt.hasNext() ) {
            LinkedHashMap < String, Object > schemaInfo = schemasIt.next();
            // id属性取得
            Object id = SCIMUtil.getAttribute( schemaInfo, "id" );
            if ( id != null && id instanceof String ) {
                // ユーザのスキーマか？
                boolean isUserSchema = false;
                if ( !schemaIdSet.isEmpty() ) {
                    Iterator < String > schemaIdSetIt = schemaIdSet.iterator();
                    while ( schemaIdSetIt.hasNext() ) {
                        if ( id.toString().equalsIgnoreCase( schemaIdSetIt.next() ) ) {
                            isUserSchema = true;
                        }
                    }
                }

                if ( isUserSchema ) {
                    Object attributes = schemaInfo.get( "attributes" );
                    if ( attributes != null && attributes instanceof ArrayList ) {
                        @SuppressWarnings("unchecked")
                        ArrayList < LinkedHashMap < String, Object > > attributeList = ( ArrayList < LinkedHashMap < String, Object > > )attributes;
                        Iterator < LinkedHashMap < String, Object > > attributeListIt = attributeList.iterator();
                        while ( attributeListIt.hasNext() ) {
                            LinkedHashMap < String, Object > attributeInfo = attributeListIt.next();
                            Object name = attributeInfo.get( "name" );
                            // 属性名を比較
                            if ( attributeName.equalsIgnoreCase( name.toString() ) ) {
                                result = attributeInfo;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if ( result == null && isCore ) {
            if ( attributeName.equalsIgnoreCase( "schemas" ) ) {
                result = new LinkedHashMap< String, Object >();
                result.put( "name", "schemas" );
                result.put( "type", "string" );
                result.put( "multiValued", true );
                result.put( "mutability", "readWrite" );
            }
            else if ( attributeName.equalsIgnoreCase( "id" ) ) {
                result = new LinkedHashMap< String, Object >();
                result.put( "name", "id" );
                result.put( "type", "string" );
                result.put( "multiValued", false );
                result.put( "mutability", "readOnly" );
            }
            else if ( attributeName.equalsIgnoreCase( "meta" ) ) {
                result = new LinkedHashMap< String, Object >();
                result.put( "name", "meta" );
                result.put( "type", "complex" );
                result.put( "multiValued", false );
                result.put( "mutability", "readOnly" );
            }
        }

        return result;
    }

    /**
     * ユーザリソースがフィルタに一致するかチェック
     *
     * ※実装検証に必要な部分(Simple属性の equal match)のみ実装。
     *   実際にはバックエンドにDBやLDAPを利用し、
     *   SQLやLDAPフィルタに変換して処理する事を推奨。
     *
     * @param context
     * @param resourceInfo
     * @param filter
     * @return 一致:true 不一致:false
     * @throws SCIMUtilException
     */
    public static boolean checkUserSimpleFilter( ServletContext context, Map < String, Object > resourceInfo, String filter ) throws SCIMUtilException {
        boolean result = false;

        // フィルタ解析
        String work = filter;
        String filterAttribute = null;
        String operator = null;
        String filterValue = null;

        //最初の空白までがフィルタ属性
        int pos = work.indexOf( " " );
        pos = work.indexOf( " " );
        if ( pos > 0 ) {
            // フィルタ属性名取得
            filterAttribute = work.substring( 0, pos );
            // 比較演算子確認
            work = work.substring( pos + 1 ).trim();
            pos = work.indexOf( " " );
            if ( pos > 0 ) {
                operator = work.substring( 0, pos );
                //
                // とりあえず eq だけ実装
                //
                if ( operator.equals( "eq" ) ) {
                    // フィルタ値取得
                    filterValue = work.substring( 2 ).trim();
                    // 前後のダブルクォートカット
                    if ( filterValue.length() >= 2 && filterValue.charAt( 0 ) == '\"' && filterValue.charAt( filterValue.length() - 1 ) == '\"' ) {
                        filterValue = filterValue.substring( 1, filterValue.length() - 1 );
                    }

                }
                else {
                    String message = String.format( MessageConstants.ERROR_INVALID_FILTER_SYNTAX, filter );
                    throw new SCIMUtilException( HttpServletResponse.SC_BAD_REQUEST, null, message );
                }
            }
            else {
                String message = String.format( MessageConstants.ERROR_INVALID_FILTER_SYNTAX, filter );
                throw new SCIMUtilException( HttpServletResponse.SC_BAD_REQUEST, null, message );
            }
        }
        else {
            String message = String.format( MessageConstants.ERROR_INVALID_FILTER_SYNTAX, filter );
            throw new SCIMUtilException( HttpServletResponse.SC_BAD_REQUEST, null, message );
        }

        // 属性名チェック
        String attributeName = null;
        //String subAttributeName = null;
        // サブ属性の指定有り
        if ( filterAttribute.indexOf( "." ) > 0 ) {
            pos = filterAttribute.indexOf( "." );
            attributeName = filterAttribute.substring( 0, pos );
            //subAttributeName = filterAttribute.substring( pos + 1 );
        }
        // サブ属性の指定無し
        else {
            attributeName = filterAttribute;
        }

        // スキーマ情報取得
        LinkedHashMap< String, Object > attributeSchema = SCIMUtil.getUserAttributeInfo( context, attributeName, true );
        if ( attributeSchema == null ) {
            String message = String.format( MessageConstants.ERROR_INVALID_FILTER_SYNTAX, filter );
            throw new SCIMUtilException( HttpServletResponse.SC_BAD_REQUEST, null, message );
        }

        // 属性を探す
        Iterator < String > attributeIt = resourceInfo.keySet().iterator();
        while ( attributeIt.hasNext() ) {
            String name = attributeIt.next();
            if ( name.equalsIgnoreCase( attributeName ) ) {
                Object values = resourceInfo.get( name );
                if( values != null ) {
                    //
                    // とりあえず Simple 型の単一値だけ実装
                    //
                    if ( values instanceof String ) {
                        if ( filterValue.equalsIgnoreCase( values.toString() ) ) {
                            result = true;
                        }
                    }
                    else if ( values instanceof Integer ) {
                        if ( filterValue.equalsIgnoreCase( values.toString() ) ) {
                            result = true;
                        }
                    }
                    else if ( values instanceof Boolean ) {
                        if ( filterValue.equalsIgnoreCase( values.toString() ) ) {
                            result = true;
                        }
                    }
                }

                break;
            }
        }

        return result;
    }
}
