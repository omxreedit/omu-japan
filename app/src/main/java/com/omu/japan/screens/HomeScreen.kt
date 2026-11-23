package com.omu.japan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.omu.japan.data.AppContainer
import com.omu.japan.model.AppSettings
import com.omu.japan.model.UserProgress
import com.omu.japan.navigation.Route
import com.omu.japan.ui.components.*
import com.omu.japan.ui.theme.OmuTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, nav: NavController) {
    val greeting = remember { val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); if (h < 12) "おはよう" else if (h < 18) "こんにちは" else "こんばんは" }
    val date = remember { SimpleDateFormat("EEEE, MMM d", Locale.US).format(Date()) }
    val todayVocab = container.content.vocabulary.getOrNull((System.currentTimeMillis() / 86400000L % container.content.vocabulary.size.coerceAtLeast(1)).toInt())
    val todayKanji = container.content.kanji.getOrNull((System.currentTimeMillis() / 86400000L % container.content.kanji.size.coerceAtLeast(1)).toInt())
    ScreenScaffold("OMU日本", "$greeting · $date") {
        SectionCard("Today's mission", "JLPT ${settings.jlptTarget} · XP ${progress.xp} · Level ${progress.level} · Streak ${progress.streak} days", strong = true) {
            Text("Japanese Learning Journey", color = OmuTheme.c.accent, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { ((progress.xp % 100) / 100f).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth().height(8.dp), color = OmuTheme.c.accent, trackColor = Color.White.copy(alpha = 0.12f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryBtn("Continue learning", Modifier.weight(1f)) { nav.navigate(Route.Learn.path) }
                GhostBtn("Daily challenge", Modifier.weight(1f)) { nav.navigate(Route.Daily.path) }
            }
        }
        SectionCard("Quick shortcuts") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostBtn("Hiragana", Modifier.weight(1f)) { nav.navigate(Route.Hiragana.path) }
                GhostBtn("Katakana", Modifier.weight(1f)) { nav.navigate(Route.Katakana.path) }
                GhostBtn("Vocab", Modifier.weight(1f)) { nav.navigate(Route.Vocab.path) }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostBtn("Kanji", Modifier.weight(1f)) { nav.navigate(Route.Kanji.path) }
                GhostBtn("Grammar", Modifier.weight(1f)) { nav.navigate(Route.Grammar.path) }
                GhostBtn("Quiz", Modifier.weight(1f)) { nav.navigate(Route.Quiz.path) }
            }
        }
        SectionCard("Learning overview") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("Learned", progress.learnedIds.size.toString(), Modifier.weight(1f))
                StatPill("Favorites", progress.favoriteIds.size.toString(), Modifier.weight(1f))
                StatPill("Mistakes", progress.mistakes.size.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("Daily goal: ${settings.dailyGoalXp} XP · Glass: ${settings.glassQuality}", color = OmuTheme.c.muted)
        }
        if (todayVocab != null) SectionCard("Today's vocabulary", todayVocab.word) { Text("${todayVocab.kana} · ${todayVocab.meaning}", color = OmuTheme.c.muted) }
        if (todayKanji != null) SectionCard("Today's kanji", todayKanji.kanji) { Text("${todayKanji.meaning} · JLPT ${todayKanji.jlpt}", color = OmuTheme.c.muted) }
        SectionCard("Recent activity") {
            val last = progress.quizHistory.lastOrNull()
            Text(if (last == null) "No quiz activity yet. Start a daily challenge." else "Last quiz: ${last.kind} · ${last.correct}/${last.total} · ${last.date}", color = OmuTheme.c.muted)
        }
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
        Text(label, color = OmuTheme.c.muted, style = MaterialTheme.typography.labelMedium)
    }
}
