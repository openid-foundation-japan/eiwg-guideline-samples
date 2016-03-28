/**
 *
 * クラス名
 *          Users
 *
 * 概要
 *          ユーザ情報処理クラス
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
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;
import jp.or.openid.eiwg.scim.operation.Operation;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Servlet implementation class Users
 */
@WebServlet("/scim/Users/*")
public class Users extends HttpServlet {

    /**
     * サービス処理
     * (PATCH メソッドに対応するためには HttpServlet.service() をオーバーライト)
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
        else if ( method.equals( "POST" ) ) {
            doPost( request, response );
        }
        else if ( method.equals( "PUT" ) ) {
            doPut( request, response );
        }
        else if ( method.equals( "PATCH" ) ) {
            doPatch( request, response );
        }
        else if ( method.equals( "DELETE" ) ) {
            doDelete( request, response );
        }
        else {
            this.errorResponse( response, HttpServletResponse.SC_FORBIDDEN, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
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
            this.errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
        }
        else {
            // パラメータ取得
            String targetId = request.getPathInfo();
            String attributes = request.getParameter( "attributes" );
            String filter = request.getParameter( "filter" );
            String sortBy = request.getParameter( "sortBy" );
            String sortOrder = request.getParameter( "sortOrder" );
            String startIndex = request.getParameter( "startIndex" );
            String count = request.getParameter( "count" );

            if ( targetId != null && !targetId.isEmpty() ) {
                // 先頭の'/'文字を取り除く
                targetId = targetId.substring( 1 );
            }

            // ユーザ情報検索
            ArrayList< LinkedHashMap< String, Object > > resultList = op.searchUserInfo( context, request, targetId, attributes, filter, sortBy, sortOrder, startIndex, count );
            if ( resultList != null ) {
                ObjectMapper mapper = new ObjectMapper();
                StringWriter writer = new StringWriter();

                // 結果メッセージ作成
                if ( targetId != null && !targetId.isEmpty() ) {
                    if ( !resultList.isEmpty() ) {
                        LinkedHashMap< String, Object > resultObject = resultList.get( 0 );
                        // javaオブジェクト→JSONに変換
                        mapper.writeValue( writer, resultObject );
                        response.setContentType( "application/scim+json;charset=UTF-8" );
                        response.setHeader( "Location", request.getRequestURL().toString() );
                        PrintWriter out = response.getWriter();
                        out.println( writer );
                    }
                    else {
                        // id指定で見つからなかった場合はエラー
                        this.errorResponse( response, HttpServletResponse.SC_NOT_FOUND, null, MessageConstants.ERROR_NOT_FOUND );
                    }
                }
                else {
                    // javaオブジェクト→JSONに変換
                    mapper.writeValue( writer, resultList );
                    String listResponse = "{\"schemas\":[\"urn:ietf:params:scim:api:messages:2.0:ListResponse\"],";
                    listResponse += "\"totalResults\":" + Integer.toString( resultList.size() );
                    if ( resultList.size() > 0 ) {
                        listResponse += ",\"Resources\":";
                        listResponse += writer.toString();
                    }
                    listResponse += "}";
                    response.setContentType( "application/scim+json;charset=UTF-8" );
                    PrintWriter out = response.getWriter();
                    out.println( listResponse );
                }
            }
            else {
                // エラー
                this.errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
            }
        }

    }

    /**
     * POSTリクエスト処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            // パラメータ取得
            String targetId = request.getPathInfo();
            String attributes = request.getParameter( "attributes" );

            if ( targetId != null && !targetId.isEmpty() ) {
                // 先頭の'/'文字を取り除く
                targetId = targetId.substring( 1 );
            }

            if ( targetId == null || targetId.isEmpty() ) {
                // POSTデータ(JSON)取得
                request.setCharacterEncoding( "UTF-8" );
                String body = IOUtils.toString( request.getReader() );

                // ユーザ情報作成
                LinkedHashMap< String, Object > resultObject = op.createUserInfo( context, request, attributes, body );
                if ( resultObject != null ) {
                    // javaオブジェクト→JSONに変換
                    ObjectMapper mapper = new ObjectMapper();
                    StringWriter writer = new StringWriter();
                    mapper.writeValue( writer, resultObject );

                    // Locationヘッダ用のURLを作成
                    String location = request.getScheme() + "://" + request.getServerName();
                    int serverPort = request.getServerPort();
                    if ( serverPort != 80 && serverPort != 443 ) {
                        location += ":" + Integer.toString( serverPort );
                    }
                    location += request.getContextPath();
                    location += "/scim/Users/";
                    if ( resultObject.get( "id" ) != null  ) {
                        location += resultObject.get( "id" ).toString();
                    }

                    // 結果メッセージ作成
                    response.setStatus( HttpServletResponse.SC_CREATED );
                    response.setContentType( "application/scim+json;charset=UTF-8" );
                    response.setHeader( "Location", location );

                    PrintWriter out = response.getWriter();
                    out.println( writer.toString() );
                }
                else {
                    // エラー
                    errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
                }
            }
            else {
                errorResponse( response, HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
            }
        }
    }

    /**
     * PUTリクエスト処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            // パラメータ取得
            String targetId = request.getPathInfo();
            String attributes = request.getParameter( "attributes" );

            if ( targetId != null && !targetId.isEmpty() ) {
                // 先頭の'/'文字を取り除く
                targetId = targetId.substring( 1 );
            }

            if ( targetId != null && !targetId.isEmpty() ) {
                // PUTデータ(JSON)取得
                request.setCharacterEncoding( "UTF-8" );
                String body = IOUtils.toString( request.getReader() );

                // ユーザ情報更新
                LinkedHashMap< String, Object > resultObject = op.updateUserInfo( context, request, targetId, attributes, body );
                if ( resultObject != null ) {
                    // javaオブジェクト→JSONに変換
                    ObjectMapper mapper = new ObjectMapper();
                    StringWriter writer = new StringWriter();
                    mapper.writeValue( writer, resultObject );

                    // Locationヘッダ用のURLを作成
                    String location = request.getScheme() + "://" + request.getServerName();
                    int serverPort = request.getServerPort();
                    if ( serverPort != 80 && serverPort != 443 ) {
                        location += ":" + Integer.toString( serverPort );
                    }
                    location += request.getContextPath();
                    location += "/scim/Users/";
                    if ( resultObject.get( "id" ) != null  ) {
                        location += resultObject.get( "id" ).toString();
                    }

                    // 結果メッセージ作成
                    response.setStatus( HttpServletResponse.SC_OK );
                    response.setContentType( "application/scim+json;charset=UTF-8" );
                    response.setHeader( "Location", location );

                    PrintWriter out = response.getWriter();
                    out.println( writer.toString() );
                }
                else {
                    // エラー
                    errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
                }
            }
            else {
                errorResponse( response, HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
            }
        }
    }

    /**
     * PATCHリクエスト処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 未サポート
        errorResponse( response, HttpServletResponse.SC_FORBIDDEN, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
    }

    /**
     * DELETEリクエスト処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @throws ServletException
     * @throws IOException
     */
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            // パラメータ取得
            String targetId = request.getPathInfo();

            if ( targetId != null && !targetId.isEmpty() ) {
                // 先頭の'/'文字を取り除く
                targetId = targetId.substring( 1 );
            }

            if ( targetId != null && !targetId.isEmpty() ) {

                // ユーザ情報削除
                boolean deleteResult = op.deleteUserInfo( context, request, targetId );
                if ( deleteResult ) {
                    response.setStatus( HttpServletResponse.SC_NO_CONTENT );
                }
                else {
                    // エラー
                    errorResponse( response, op.getErrorCode(), op.getErrorType(), op.getErrorMessage() );
                }
            }
            else {
                errorResponse( response, HttpServletResponse.SC_BAD_REQUEST, null, MessageConstants.ERROR_NOT_SUPPORT_OPERATION );
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
