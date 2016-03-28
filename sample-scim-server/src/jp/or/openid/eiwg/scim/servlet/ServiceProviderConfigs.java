/**
 *
 * クラス名
 *          ServiceProviderConfigs
 *
 * 概要
 *          サービスプロバイダ設定処理クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.scim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;
import jp.or.openid.eiwg.scim.operation.Operation;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Servlet implementation class ServiceProviderConfigs
 */
@WebServlet("/scim/ServiceProviderConfigs")
public class ServiceProviderConfigs extends HttpServlet {

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
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
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
            // [draft-ietf-scim-api-13 3.2.2.1. Query Endpoints]
            //  Queries MAY be performed against a SCIM resource object, a resource
            //  type endpoint, or a SCIM server root.

            // ServiceProviderConfigs エンドポイントでは検索パラメータは動作しないので無視する。

            // location属性用のURLを作成
            String location = request.getScheme() + "://" + request.getServerName();
            int serverPort = request.getServerPort();
            if ( serverPort != 80 && serverPort != 443 ) {
                location += ":" + Integer.toString( serverPort );
            }
            location += request.getContextPath();

            // サービスプロバイダ設定取得
            @SuppressWarnings("unchecked")
            Map < String, Object > serviceProviderConfigsObject = ( Map < String, Object > )context.getAttribute( "ServiceProviderConfigs" );

            try {
                ObjectMapper mapper = new ObjectMapper();
                StringWriter writer = new StringWriter();
                mapper.writeValue( writer, serviceProviderConfigsObject );
                String serviceProviderConfigs = writer.toString();
                serviceProviderConfigs = String.format( serviceProviderConfigs, location );

                response.setContentType( "application/scim+json;charset=UTF-8" );
                response.setHeader( "Location", request.getRequestURL().toString() );
                PrintWriter out = response.getWriter();
                out.println( serviceProviderConfigs );
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
