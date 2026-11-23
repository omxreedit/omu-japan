package com.omu.japan.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.omu.japan.data.AppContainer
import com.omu.japan.model.AppSettings
import com.omu.japan.model.UserProgress
import com.omu.japan.navigation.Route
import com.omu.japan.ui.components.*
import com.omu.japan.ui.theme.OmuTheme

@Composable fun LearnScreen(nav: NavController) = ScreenScaffold("Learn", "Structured Japanese paths with real content and local progress.") {
    NavCard("あ Hiragana", "Basic, dakuten, handakuten, combinations") { nav.navigate(Route.Hiragana.path) }
    NavCard("ア Katakana", "Basic, dakuten, handakuten, combinations") { nav.navigate(Route.Katakana.path) }
    NavCard("語 Vocabulary", "Categories, examples, audio, favorites") { nav.navigate(Route.Vocab.path) }
    NavCard("漢 Kanji", "Meanings, readings, JLPT, examples") { nav.navigate(Route.Kanji.path) }
    NavCard("文 Grammar", "N5-N1 lessons with examples and practice") { nav.navigate(Route.Grammar.path) }
}

@Composable fun PracticeScreen(nav: NavController) = ScreenScaffold("Practice", "Real quizzes, revision, favorites and mistakes.") {
    NavCard("Mixed Quiz", "Kana, vocabulary, kanji, grammar") { nav.navigate(Route.Quiz.path) }
    NavCard("Daily Challenge", "A quick mission for today") { nav.navigate(Route.Daily.path) }
    NavCard("Mistakes", "Review what you got wrong") { nav.navigate(Route.Mistakes.path) }
    NavCard("Favorites", "Saved items for revision") { nav.navigate(Route.Favorites.path) }
    NavCard("Search", "Find any learning item") { nav.navigate(Route.Search.path) }
}

@Composable fun LibraryScreen(nav: NavController) = ScreenScaffold("Library", "Resources, stats and settings.") {
    NavCard("Teacher Library", "Verified YouTube channels") { nav.navigate(Route.Teachers.path) }
    NavCard("Statistics", "Real local learning stats") { nav.navigate(Route.Stats.path) }
    NavCard("Settings", "Glass quality, theme, audio, daily goal") { nav.navigate(Route.Settings.path) }
}

@Composable fun ProfileScreen(settings: AppSettings, progress: UserProgress, nav: NavController) = ScreenScaffold("Profile", "Your local learning identity.") {
    SectionCard("Learner", strong = true) {
        Text("Level ${progress.level} · XP ${progress.xp} · Streak ${progress.streak} days", color = OmuTheme.c.text)
        Text("JLPT target: ${settings.jlptTarget}", color = OmuTheme.c.muted)
    }
    SectionCard("Learning overview") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill("Learned", progress.learnedIds.size.toString(), Modifier.weight(1f))
            StatPill("Favorites", progress.favoriteIds.size.toString(), Modifier.weight(1f))
            StatPill("Mistakes", progress.mistakes.size.toString(), Modifier.weight(1f))
        }
    }
    NavCard("Statistics", "Detailed progress") { nav.navigate(Route.Stats.path) }
    NavCard("Settings", "Adjust behavior") { nav.navigate(Route.Settings.path) }
}

@Composable fun NavCard(title: String, subtitle: String, onClick: () -> Unit) {
    SectionCard(title, subtitle) { PrimaryBtn("Open", onClick = onClick) }
}

@Composable fun TeachersScreen(container: AppContainer) {
    val ctx = LocalContext.current
    ScreenScaffold("Teacher Library", "Verified official YouTube channels. Links open externally.") {
        container.content.teachers.forEach { t ->
            SectionCard(t.name, "${t.language} · ${t.level}") {
                Text(t.note, color = OmuTheme.c.text)
                Spacer(Modifier.height(4.dp))
                Text(t.topics.joinToString(" · "), color = OmuTheme.c.muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(10.dp))
                PrimaryBtn("Open channel") {
                    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(t.url))) }
                }
            }
        }
    }
}

@Composable fun DailyScreen(nav: NavController) = ScreenScaffold("Daily Challenge", "Complete a quick mixed quiz to keep your streak alive.") {
    SectionCard("Today's mission", "Answer 10 mixed questions. Correct answers give XP and update your streak.", strong = true) {
        PrimaryBtn("Start daily challenge") { nav.navigate(Route.Quiz.path) }
    }
}

@Composable fun StatsScreen(progress: UserProgress) = ScreenScaffold("Statistics", "Real progress from your local device.") {
    SectionCard("Overview", strong = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill("XP", progress.xp.toString(), Modifier.weight(1f))
            StatPill("Level", progress.level.toString(), Modifier.weight(1f))
            StatPill("Streak", progress.streak.toString(), Modifier.weight(1f))
        }
    }
    SectionCard("Quiz history") {
        if (progress.quizHistory.isEmpty()) Text("No quizzes taken yet.", color = OmuTheme.c.muted)
        else progress.quizHistory.takeLast(10).reversed().forEach { q ->
            Text("${q.date} · ${q.kind} · ${q.correct}/${q.total}", color = OmuTheme.c.muted)
            Spacer(Modifier.height(4.dp))
        }
    }
}
