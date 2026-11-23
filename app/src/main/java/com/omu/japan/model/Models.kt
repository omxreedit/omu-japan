package com.omu.japan.model

import org.json.JSONArray
import org.json.JSONObject

enum class GlassQuality { AUTO, HIGH, BALANCED, BATTERY_SAVER }
enum class ThemeMode { DARK, LIGHT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val glassQuality: GlassQuality = GlassQuality.AUTO,
    val reduceMotion: Boolean = false,
    val audioEnabled: Boolean = true,
    val dailyGoalXp: Int = 20,
    val jlptTarget: String = "N5"
)

data class UserProgress(
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastStudyDate: String = "",
    val learnedIds: Set<String> = emptySet(),
    val favoriteIds: Set<String> = emptySet(),
    val mistakes: List<MistakeItem> = emptyList(),
    val quizHistory: List<QuizResult> = emptyList()
)

data class MistakeItem(val question: String, val chosen: String, val correct: String, val date: String)
data class QuizResult(val kind: String, val correct: Int, val total: Int, val date: String, val ts: Long)

data class KanaItem(val id: String, val char: String, val romaji: String, val meaning: String, val type: String, val example: String)
data class VocabItem(val id: String, val word: String, val kana: String, val romaji: String, val meaning: String, val category: String, val example: String, val exampleKana: String, val exampleMeaning: String)
data class KanjiExample(val word: String, val reading: String, val meaning: String)
data class KanjiItem(val id: String, val kanji: String, val meaning: String, val onyomi: String, val kunyomi: String, val strokes: Int, val jlpt: String, val examples: List<KanjiExample>)
data class QuizQuestion(val q: String, val options: List<String>, val answer: Int, val explain: String)
data class GrammarItem(val id: String, val title: String, val jlpt: String, val level: String, val explanation: String, val example: String, val kana: String, val romaji: String, val meaning: String, val mistakes: String, val quiz: List<QuizQuestion>)
data class TeacherItem(val name: String, val language: String, val level: String, val topics: List<String>, val url: String, val note: String)

fun JSONObject.toKana() = KanaItem(optString("id"), optString("char"), optString("romaji"), optString("meaning"), optString("type"), optString("example"))
fun JSONObject.toVocab() = VocabItem(optString("id"), optString("word"), optString("kana"), optString("romaji"), optString("meaning"), optString("category"), optString("example"), optString("exampleKana"), optString("exampleMeaning"))
fun JSONObject.toKanji(): KanjiItem {
    val ex = mutableListOf<KanjiExample>()
    val arr = optJSONArray("examples") ?: JSONArray()
    for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { ex += KanjiExample(it.optString("word"), it.optString("reading"), it.optString("meaning")) }
    return KanjiItem(optString("id"), optString("kanji"), optString("meaning"), optString("onyomi"), optString("kunyomi"), optInt("strokes"), optString("jlpt"), ex)
}
fun JSONObject.toGrammar(): GrammarItem {
    val qs = mutableListOf<QuizQuestion>()
    val arr = optJSONArray("quiz") ?: JSONArray()
    for (i in 0 until arr.length()) {
        val q = arr.optJSONObject(i) ?: continue
        val opts = mutableListOf<String>()
        val oa = q.optJSONArray("options") ?: JSONArray()
        for (j in 0 until oa.length()) opts += oa.optString(j)
        qs += QuizQuestion(q.optString("q"), opts, q.optInt("answer"), q.optString("explain", optString("explanation")))
    }
    return GrammarItem(optString("id"), optString("title"), optString("jlpt"), optString("level"), optString("explanation"), optString("example"), optString("kana"), optString("romaji"), optString("meaning"), optString("mistakes"), qs)
}
fun JSONObject.toTeacher(): TeacherItem {
    val topics = mutableListOf<String>()
    val arr = optJSONArray("topics") ?: JSONArray()
    for (i in 0 until arr.length()) topics += arr.optString(i)
    return TeacherItem(optString("name"), optString("language"), optString("level"), topics, optString("url"), optString("note"))
}
