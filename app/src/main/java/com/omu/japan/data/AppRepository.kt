package com.omu.japan.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.omu.japan.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "omu_prefs")

class AppRepository(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val glass = stringPreferencesKey("glass")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val audio = booleanPreferencesKey("audio")
        val dailyGoal = intPreferencesKey("daily_goal")
        val jlpt = stringPreferencesKey("jlpt")
        val xp = intPreferencesKey("xp")
        val level = intPreferencesKey("level")
        val streak = intPreferencesKey("streak")
        val lastStudy = stringPreferencesKey("last_study")
        val learned = stringPreferencesKey("learned")
        val favorites = stringPreferencesKey("favorites")
        val mistakes = stringPreferencesKey("mistakes")
        val quizHistory = stringPreferencesKey("quiz_history")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = if (p[Keys.theme] == "light") ThemeMode.LIGHT else ThemeMode.DARK,
            glassQuality = when (p[Keys.glass]) {
                "high" -> GlassQuality.HIGH
                "balanced" -> GlassQuality.BALANCED
                "battery" -> GlassQuality.BATTERY_SAVER
                else -> GlassQuality.AUTO
            },
            reduceMotion = p[Keys.reduceMotion] ?: false,
            audioEnabled = p[Keys.audio] ?: true,
            dailyGoalXp = p[Keys.dailyGoal] ?: 20,
            jlptTarget = p[Keys.jlpt] ?: "N5"
        )
    }

    val progressFlow: Flow<UserProgress> = context.dataStore.data.map { p ->
        UserProgress(
            xp = p[Keys.xp] ?: 0,
            level = p[Keys.level] ?: 1,
            streak = p[Keys.streak] ?: 0,
            lastStudyDate = p[Keys.lastStudy] ?: "",
            learnedIds = decodeSet(p[Keys.learned]),
            favoriteIds = decodeSet(p[Keys.favorites]),
            mistakes = decodeMistakes(p[Keys.mistakes]),
            quizHistory = decodeHistory(p[Keys.quizHistory])
        )
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { p ->
            val cur = AppSettings(
                themeMode = if (p[Keys.theme] == "light") ThemeMode.LIGHT else ThemeMode.DARK,
                glassQuality = when (p[Keys.glass]) { "high" -> GlassQuality.HIGH; "balanced" -> GlassQuality.BALANCED; "battery" -> GlassQuality.BATTERY_SAVER; else -> GlassQuality.AUTO },
                reduceMotion = p[Keys.reduceMotion] ?: false,
                audioEnabled = p[Keys.audio] ?: true,
                dailyGoalXp = p[Keys.dailyGoal] ?: 20,
                jlptTarget = p[Keys.jlpt] ?: "N5"
            )
            val s = transform(cur)
            p[Keys.theme] = if (s.themeMode == ThemeMode.LIGHT) "light" else "dark"
            p[Keys.glass] = when (s.glassQuality) { GlassQuality.HIGH -> "high"; GlassQuality.BALANCED -> "balanced"; GlassQuality.BATTERY_SAVER -> "battery"; GlassQuality.AUTO -> "auto" }
            p[Keys.reduceMotion] = s.reduceMotion
            p[Keys.audio] = s.audioEnabled
            p[Keys.dailyGoal] = s.dailyGoalXp
            p[Keys.jlpt] = s.jlptTarget
        }
    }

    suspend fun markLearned(type: String, id: String) {
        context.dataStore.edit { p ->
            val learned = decodeSet(p[Keys.learned]).toMutableSet()
            learned += "$type:$id"
            p[Keys.learned] = encodeSet(learned)
            addXpInternal(p, 5)
            touchStreakInternal(p)
        }
    }

    suspend fun toggleFavorite(type: String, id: String) {
        context.dataStore.edit { p ->
            val fav = decodeSet(p[Keys.favorites]).toMutableSet()
            val k = "$type:$id"
            if (fav.contains(k)) fav.remove(k) else fav.add(k)
            p[Keys.favorites] = encodeSet(fav)
        }
    }

    suspend fun recordQuiz(kind: String, correct: Int, total: Int) {
        context.dataStore.edit { p ->
            val hist = decodeHistory(p[Keys.quizHistory]).toMutableList()
            hist += QuizResult(kind, correct, total, today(), System.currentTimeMillis())
            p[Keys.quizHistory] = encodeHistory(hist.takeLast(100))
            addXpInternal(p, correct * 10)
            touchStreakInternal(p)
        }
    }

    suspend fun addMistake(item: MistakeItem) {
        context.dataStore.edit { p ->
            val m = decodeMistakes(p[Keys.mistakes]).toMutableList()
            m += item
            p[Keys.mistakes] = encodeMistakes(m.takeLast(200))
        }
    }

    suspend fun clearMistakes() { context.dataStore.edit { p -> p[Keys.mistakes] = "[]" } }

    private fun addXpInternal(p: androidx.datastore.preferences.core.MutablePreferences, amount: Int) {
        val xp = (p[Keys.xp] ?: 0) + amount
        p[Keys.xp] = xp
        p[Keys.level] = 1 + xp / 100
    }
    private fun touchStreakInternal(p: androidx.datastore.preferences.core.MutablePreferences) {
        val today = today(); val last = p[Keys.lastStudy] ?: ""
        if (today == last) return
        val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, -1)
        val y = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        val streak = if (last == y) (p[Keys.streak] ?: 0) + 1 else 1
        p[Keys.streak] = streak; p[Keys.lastStudy] = today
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun decodeSet(s: String?): Set<String> = try { JSONArray(s ?: "[]").let { arr -> (0 until arr.length()).map { arr.getString(it) }.toSet() } } catch (_: Exception) { emptySet() }
    private fun encodeSet(set: Set<String>) = JSONArray(set.toList()).toString()
    private fun decodeMistakes(s: String?): List<MistakeItem> = try { JSONArray(s ?: "[]").let { arr -> (0 until arr.length()).map { val o=arr.getJSONObject(it); MistakeItem(o.optString("q"),o.optString("chosen"),o.optString("correct"),o.optString("date")) } } } catch (_: Exception) { emptyList() }
    private fun encodeMistakes(list: List<MistakeItem>) = JSONArray().apply { list.forEach { put(JSONObject().put("q",it.question).put("chosen",it.chosen).put("correct",it.correct).put("date",it.date)) } }.toString()
    private fun decodeHistory(s: String?): List<QuizResult> = try { JSONArray(s ?: "[]").let { arr -> (0 until arr.length()).map { val o=arr.getJSONObject(it); QuizResult(o.optString("kind"),o.optInt("correct"),o.optInt("total"),o.optString("date"),o.optLong("ts")) } } } catch (_: Exception) { emptyList() }
    private fun encodeHistory(list: List<QuizResult>) = JSONArray().apply { list.forEach { put(JSONObject().put("kind",it.kind).put("correct",it.correct).put("total",it.total).put("date",it.date).put("ts",it.ts)) } }.toString()
}
