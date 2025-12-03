/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util

/**
 * YukiKanaConverterとGoogleIMEを使用して「ローマ字」を日本語に変換するクラス
 * @author Rei0925
 */
class Japaniser(
    private val googleIME: GoogleIME
) {
    /**
     * 「ローマ字」を受け取り任意の形態にして返す
     * @param original 変換元のローマ字
     * @param iME 漢字変換を行うかどうか
     * @return かな文字または漢字の変換後の文字
     * @since 0.0.1
     */
    fun conv(original: String, iME: Boolean): String{
        val kana: String = YukiKanaConverter.conv(original) ?: ""
        return if (iME){
            googleIME.convert(kana)
        }else{
            kana
        }
    }
}