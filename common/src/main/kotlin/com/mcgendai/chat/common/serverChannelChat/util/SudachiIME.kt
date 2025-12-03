/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer

/**
 * Sudachiを利用して変換するクラス
 * @author Rei0925
 * @since 0.0.1
 */
@Deprecated(
    "Sudachiの使用は辞書ファイルがないといけないが、用意できないことや実装の面倒さからGoogleIMEに変更。",
    ReplaceWith("GoogleIME")
)
class SudachiIME {
    private val tokenizer: Tokenizer by lazy {
        // resources 内の sudachi.json を文字列として取得
        val json = this::class.java.getResource("/sudachi.json")!!
            .readText(Charsets.UTF_8)

        val config = Config.fromJsonString(json,null)
        DictionaryFactory().create(config).create()
    }


    /**
     * 「かな文字」から「漢字」に変換する
     * @param text 変換元の「かな文字」
     * @return 変換リスト
     * @since 0.0.1
     */
    fun convert(text: String): List<String> {
        val tokens = tokenizer.tokenize(Tokenizer.SplitMode.C, text)
        return tokens.map { it.normalizedForm() }
    }
}