/**
 *
 * クラス名
 *          MessageConstants
 *
 * 概要
 *          メッセージ用定数
 *
 * 著作権
 *          Copyright (c) 2015 OpenID Foundation Japan.
 *          This is released under the MIT License, see LICENSE file.
 */
package jp.or.openid.eiwg.constants;


/**
 * メッセージ定数クラス
 */
public class MessageConstants {

    /* エラー種別 */
    public static final String ERROR_TYPE_INVALIDFILTER = "invalidFilter";
    public static final String ERROR_TYPE_TOOMANY = "tooMany";
    public static final String ERROR_TYPE_UNIQUENESS = "uniqueness";
    public static final String ERROR_TYPE_MUTABILITY = "mutability";
    public static final String ERROR_TYPE_INVALIDSYNTAX = "invalidSyntax";
    public static final String ERROR_TYPE_INVALIDPATH = "invalidPath";
    public static final String ERROR_TYPE_NOTARGET = "noTarget";
    public static final String ERROR_TYPE_INVALIDVALUE = "invalidValue";
    public static final String ERROR_TYPE_INVALIDVERS = "invalidVers";

    /* エラーメッセージ */
    public static final String ERROR_UNKNOWN    = "予期しないエラーが発生しました。";
    public static final String ERROR_NOT_FOUND  = "リクエストされたエンドポイントまたはリソースが見つかりません。";
    public static final String ERROR_NOT_SUPPORT_OPERATION  = "リクエストされた操作はサポートしていません。";
    public static final String ERROR_INVALID_REQUEST = "リクエストされたメッセージを解析できません。構文が不正です。";
    public static final String ERROR_UNAUTHORIZED  = "認証が必要です。";
    public static final String ERROR_INVALID_CREDENTIALS  = "認証エラー。";
    public static final String ERROR_INVALID_ATTRIBUTES  = "attributes に不正な属性名(%s)が指定されています。";
    public static final String ERROR_INVALID_FILTER_SYNTAX  = "フィルタ(%s)の構文が不正です。";
    public static final String ERROR_UNKNOWN_ATTRIBUTE = "不明な属性名(%s)が指定されています。";
    public static final String ERROR_READONLY_ATTRIBUTE = "追加または更新できない属性名(%s)が指定されています。";
}
