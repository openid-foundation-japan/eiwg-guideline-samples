/**
 *
 * クラス名
 *          SCIMUtilException
 *
 * 概要
 *          ユーティリティ用例外クラス
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.scim.util;

public class SCIMUtilException extends Exception {
    private int code;
    private String type;

    public SCIMUtilException( int code, String type, String message ) {
        super( message );
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
