package com.omu.japan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.omu.japan.data.AppContainer
import com.omu.japan.model.*
import com.omu.japan.navigation.Route
import com.omu.japan.ui.components.*
import com.omu.japan.ui.theme.OmuTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object QuizState {
    var questions by mutableStateOf<List<QuizQuestion>>(emptyList())
    var index by mutableStateOf(0)
    var correct by mutableStateOf(0)
    var answered by mutableStateOf(false)
    var kind by mutableStateOf("mixed")
    var summary by mutableStateOf("")
}

@Composable
fun QuizScreen(container: AppContainer, settings: AppSettings, nav: NavController) {
    val scope = rememberCoroutineScope()
    ScreenScaffold("Quiz", "Real questions, scoring, XP, and mistake review", onBack = { nav.popBackStack() }) {
        if (QuizState.questions.isEmpty() || QuizState.index >= QuizState.questions.size) {
            SectionCard("Start a quiz", "Mixed questions from kana, vocabulary, kanji, and grammar", strong = true) {
                listOf("mixed","hiragana","katakana","vocab","kanji","grammar").forEach { k ->
                    GhostBtn("Start $k", Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        QuizState.kind = k; QuizState.questions = buildQuiz(container, k); QuizState.index = 0; QuizState.correct = 0; QuizState.answered = false
                    }
                }
            }
            return@ScreenScaffold
        }
        val q = QuizState.questions[QuizState.index]
        SectionCard("Question ${QuizState.index + 1}/${QuizState.questions.size} · Score ${QuizState.correct}", strong = true) {
            LinearProgressIndicator(progress = { (QuizState.index + 1f) / QuizState.questions.size.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp), color = OmuTheme.c.accent, trackColor = OmuTheme.c.muted.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))
            Text(q.q, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
            Spacer(Modifier.height(12.dp))
            q.options.forEachIndexed { i, opt ->
                GhostBtn(opt, Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    if (QuizState.answered) return@GhostBtn
                    QuizState.answered = true
                    val ok = i == q.answer
                    if (ok) QuizState.correct++
                    else scope.launch { container.repository.addMistake(MistakeItem(q.q, opt, q.options.getOrElse(q.answer) { "" }, SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))) }
                }
            }
            if (QuizState.answered) {
                Text(q.explain, color = OmuTheme.c.muted)
                Spacer(Modifier.height(10.dp))
                PrimaryBtn(if (QuizState.index + 1 >= QuizState.questions.size) "See results" else "Next question") {
                    QuizState.index++; QuizState.answered = false
                    if (QuizState.index >= QuizState.questions.size) {
                        val total = QuizState.questions.size
                        val correct = QuizState.correct
                        QuizState.summary = "Score $correct/$total in ${QuizState.kind} quiz."
                        scope.launch { container.repository.recordQuiz(QuizState.kind, correct, total) }
                        QuizState.questions = emptyList(); QuizState.index = 0
                        nav.navigate(Route.QuizResult.path)
                    }
                }
            }
        }
    }
}

fun buildQuiz(container: AppContainer, kind: String): List<QuizQuestion> {
    val pool = mutableListOf<QuizQuestion>()
    fun addKana(list: List<com.omu.japan.model.KanaItem>) { list.forEachIndexed { i, it -> val opts = ([it.romaji] + list.filterIndexed { j,_ -> j!=i }.shuffled().take(3).map { k->k.romaji }).distinct().shuffled().take(4); pool += QuizQuestion("What is the romaji for ${it.char}?", opts, opts.indexOf(it.romaji).coerceAtLeast(0), "${it.char} is ${it.romaji}.") } }
    if (kind == "mixed" || kind == "hiragana") addKana(container.content.hiragana)
    if (kind == "mixed" || kind == "katakana") addKana(container.content.katakana)
    if (kind == "mixed" || kind == "vocab") container.content.vocabulary.forEachIndexed { i, it -> val opts = ([it.meaning] + container.content.vocabulary.filterIndexed { j,_ -> j!=i }.shuffled().take(3).map { v->v.meaning }).distinct().shuffled().take(4); pool += QuizQuestion("Meaning of ${it.word}?", opts, opts.indexOf(it.meaning).coerceAtLeast(0), "${it.word} means ${it.meaning}.") }
    if (kind == "mixed" || kind == "kanji") container.content.kanji.forEachIndexed { i, it -> val opts = ([it.meaning] + container.content.kanji.filterIndexed { j,_ -> j!=i }.shuffled().take(3).map { k->k.meaning }).distinct().shuffled().take(4); pool += QuizQuestion("Meaning of kanji ${it.kanji}?", opts, opts.indexOf(it.meaning).coerceAtLeast(0), "${it.kanji} = ${it.meaning}.") }
    if (kind == "mixed" || kind == "grammar") container.content.grammar.forEach { g -> g.quiz.forEach { pool += it.copy(explain = it.explain.ifBlank { g.explanation }) } }
    return pool.shuffled().take(10).ifEmpty { listOf(QuizQuestion("What is Japanese for Hello?", listOf("こんにちは","さようなら","ありがとう","すみません"), 0, "こんにちは means hello.")) }
}

@Composable
fun QuizResultScreen(nav: NavController) {
    ScreenScaffold("Quiz Results", "Your real score was saved locally.") {
        SectionCard(strong = true) {
            Text(QuizState.summary.ifBlank { "Quiz complete" }, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
            Spacer(Modifier.height(8.dp))
            Text("XP and streak are updated from real quiz results.", color = OmuTheme.c.muted)
            Spacer(Modifier.height(12.dp))
            PrimaryBtn("Try another quiz") { nav.navigate(Route.Quiz.path) { popUpTo(Route.QuizResult.path) { inclusive = true } } }
            Spacer(Modifier.height(8.dp))
            GhostBtn("Review mistakes") { nav.navigate(Route.Mistakes.path) }
        }
    }
}

@Composable
fun SettingsScreen(container: AppContainer, settings: AppSettings) {
    val scope = rememberCoroutineScope()
    ScreenScaffold("Settings", "Real settings saved locally.") {
        SectionCard("Glass quality", "Current: ${settings.glassQuality}") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(GlassQuality.AUTO, GlassQuality.HIGH, GlassQuality.BALANCED, GlassQuality.BATTERY_SAVER).forEach { q ->
                    GhostBtn(q.name.take(4), Modifier.weight(1f)) { scope.launch { container.repository.updateSettings { it.copy(glassQuality = q) } } }
                }
            }
        }
        SectionCard("Theme", "Current: ${settings.themeMode}") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostBtn("Dark") { scope.launch { container.repository.updateSettings { it.copy(themeMode = ThemeMode.DARK) } } }
                GhostBtn("Light") { scope.launch { container.repository.updateSettings { it.copy(themeMode = ThemeMode.LIGHT) } } }
            }
        }
        SectionCard("Audio (TTS)", if (settings.audioEnabled) "Enabled" else "Disabled") {
            PrimaryBtn(if (settings.audioEnabled) "Turn off" else "Turn on") { scope.launch { container.repository.updateSettings { it.copy(audioEnabled = !it.audioEnabled) } } }
        }
        SectionCard("Reduce motion", if (settings.reduceMotion) "Enabled" else "Disabled") {
            PrimaryBtn(if (settings.reduceMotion) "Turn off" else "Turn on") { scope.launch { container.repository.updateSettings { it.copy(reduceMotion = !it.reduceMotion) } } }
        }
        SectionCard("JLPT target", settings.jlptTarget) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("N5","N4","N3","N2","N1").forEach { l -> GhostBtn(l) { scope.launch { container.repository.updateSettings { it.copy(jlptTarget = l) } } } } }
        }
        SectionCard("Daily XP goal", "Current: ${settings.dailyGoalXp}") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(10,20,40).forEach { g -> GhostBtn("$g") { scope.launch { container.repository.updateSettings { it.copy(dailyGoalXp = g) } } } } }
        }
    }
}
