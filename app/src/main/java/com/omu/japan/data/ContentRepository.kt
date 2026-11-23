package com.omu.japan.data

import android.content.Context
import com.omu.japan.model.*
import org.json.JSONArray

class ContentRepository(private val context: Context) {
    val hiragana: List<KanaItem> by lazy { loadArray("data/hiragana.json") { it.toKana() } }
    val katakana: List<KanaItem> by lazy { loadArray("data/katakana.json") { it.toKana() } }
    val vocabulary: List<VocabItem> by lazy { loadArray("data/vocabulary.json") { it.toVocab() } }
    val kanji: List<KanjiItem> by lazy { loadArray("data/kanji.json") { it.toKanji() } }
    val grammar: List<GrammarItem> by lazy { loadArray("data/grammar.json") { it.toGrammar() } }
    val teachers: List<TeacherItem> by lazy { loadArray("data/teachers.json") { it.toTeacher() } }

    private fun <T> loadArray(path: String, map: (org.json.JSONObject) -> T): List<T> = try {
        context.assets.open(path).bufferedReader().use { it.readText() }.let { text ->
            val arr = JSONArray(text); (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(map) }
        }
    } catch (_: Exception) { emptyList() }

    fun allSearchable(): List<SearchHit> = buildList {
        hiragana.forEach { add(SearchHit("hiragana", it.id, it.char, "${it.romaji} · ${it.example}")) }
        katakana.forEach { add(SearchHit("katakana", it.id, it.char, "${it.romaji} · ${it.example}")) }
        vocabulary.forEach { add(SearchHit("vocab", it.id, it.word, "${it.meaning} · ${it.romaji}")) }
        kanji.forEach { add(SearchHit("kanji", it.id, it.kanji, "${it.meaning} · ${it.onyomi}")) }
        grammar.forEach { add(SearchHit("grammar", it.id, it.title, "${it.jlpt} · ${it.level}")) }
    }
}

data class SearchHit(val type: String, val id: String, val title: String, val subtitle: String)
