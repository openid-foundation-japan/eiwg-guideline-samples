/**
 *
 * クラス名
 *          InitFilter
 *
 * 概要
 *          サーブレット実行前の初期処理・終了処理クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.or.openid.eiwg.constants.MessageConstants;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Filter implementation class InitFilter
 */
@WebFilter( urlPatterns="*" )
public class InitFilter implements Filter {

    /**
     * 初期処理
     *
     * @param conf フィルタコンフィグ
     * @throws ServletException
     */
    public void init( FilterConfig conf ) throws ServletException {
    }

    /**
     * 終了処理
     */
    public void destroy() {
    }

    /**
     * サーブレット実行前のチェック処理
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param chain フィルタチェイン
     * @throws ServletException
     * @throws IOException
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {

        try {

            // リクエストパスのチェック
            String path = ((HttpServletRequest) request).getServletPath();
            if ( path == null || StringUtils.isEmpty( path ) ) {
                // エラー
                this.errorResponse( ( HttpServletResponse ) response, HttpServletResponse.SC_NOT_FOUND, null, MessageConstants.ERROR_NOT_FOUND );
            }
            else {
                // サポートしているエンドポイントに対するリクエストかチェック
                if ( !"/scim/ServiceProviderConfigs".equalsIgnoreCase( path ) &&
                     !"/scim/ResourceTypes".equalsIgnoreCase( path ) &&
                     !"/scim/Schemas".equalsIgnoreCase( path ) &&
                     !"/scim/Users".equalsIgnoreCase( path ) ) {
                    // エラー
                    this.errorResponse( ( HttpServletResponse ) response, HttpServletResponse.SC_NOT_FOUND, null, MessageConstants.ERROR_NOT_FOUND );
                }
                else {
                    // 処理を続行する
                    chain.doFilter( request, response );
                }
            }
        }
        catch ( Throwable e ) {
            // エラー
            e.printStackTrace();
            this.errorResponse( ( HttpServletResponse ) response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, MessageConstants.ERROR_UNKNOWN );
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
