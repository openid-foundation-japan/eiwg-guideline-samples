/**
 *
 * クラス名
 *          Schemas
 *
 * 概要
 *          スキーマ処理クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.scim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;
import jp.or.openid.eiwg.scim.operation.Operation;
import jp.or.openid.eiwg.scim.util.SCIMUtil;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Servlet implementation class Schemas
 */
@WebServlet("/scim/Schemas/*")
public class Schemas extends HttpServlet {

	/**
     * サービス処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String method = request.getMethod();
        if ( method.equals( "GET" ) ) {
            doGet( request, response );
        }
        else {
            errorResponse( response, HttpServletResponse.SC_FORBIDDEN, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
        }
    }

    /**
     * GETリクエスト処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // コンテキスト取得
        ServletContext context = getServletContext();

        // 認証処理
        Operation op = new Operation();
        boolean result = op.Authentication( context, request );

        if ( !result ) {
            // エラー
            errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
        }
        else {
            // location属性用のURLを作成
            String location = request.getScheme() + "://" + request.getServerName();
            int serverPort = request.getServerPort();
            if ( serverPort != 80 && serverPort != 443 ) {
                location += ":" + Integer.toString( serverPort );
            }
            location += request.getContextPath();

            // スキーマ設定取得
            @SuppressWarnings("unchecked")
            ArrayList < LinkedHashMap < String, Object > > schemas = ( ArrayList < LinkedHashMap < String, Object > > )context.getAttribute( "Schemas" );

            // location属性置換
            Iterator < LinkedHashMap < String, Object > > schemasIt = schemas.iterator();
            while ( schemasIt.hasNext() ) {
                LinkedHashMap < String, Object > schemaInfo = schemasIt.next();
                // meta属性取得
                Object metaObject = SCIMUtil.getAttribute( schemaInfo, "meta" );
                if ( metaObject != null && metaObject instanceof LinkedHashMap ) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap < String, Object > metaInfo = ( LinkedHashMap < String, Object > ) metaObject;
                    Object locationInfo = SCIMUtil.getAttribute( metaInfo, "location" );
                    if ( locationInfo != null && locationInfo instanceof String ) {
                        String locationValue = String.format( locationInfo.toString(), location );
                        metaInfo.put( "location", locationValue );
                        schemaInfo.put( "meta", metaInfo );
                    }
                }
            }

            try {
                // javaオブジェクト→JSONに変換
                ObjectMapper mapper = new ObjectMapper();
                StringWriter writer = new StringWriter();
                mapper.writeValue( writer, schemas );

                // 結果メッセージ作成
                String listResponse = "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:ListResponse\"],";
                listResponse += "\"totalResults\":" + schemas.size();
                if ( schemas.size() > 0 ) {
                    listResponse += ",\"Resources\":";
                    listResponse += writer.toString();
                }
                listResponse += "}";

                response.setContentType( "application/scim+json;charset=UTF-8" );
                PrintWriter out = response.getWriter();
                out.println( listResponse );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }

    }

    /**
     * エラーレスポンス
     *
     * @param code HTTPステータスコード
     * @param type エラー種別
     * @param message エラーメッセージ
     */
    private void errorResponse( HttpServletResponse response, int code, String type, String message ) throws IOException {
        try {
            // 結果メッセージ作成
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode schemasArray = rootNode.putArray( "schemas" );
            schemasArray.add( "urn:ietf:params:scim:api:messages:2.0:Error" );
            if ( type != null && !type.isEmpty() ) {
                rootNode.put( "scimType", type );
            }
            rootNode.put( "detail", message );
            rootNode.put( "status", code );

            response.setStatus( code );
            response.setContentType( "application/scim+json;charset=UTF-8" );
            PrintWriter out = response.getWriter();
            mapper.writeValue( out, rootNode );
        }
        catch ( JsonGenerationException e ) {
            e.printStackTrace();
        }
        catch ( JsonMappingException e ) {
            e.printStackTrace();
        }
    }
}
