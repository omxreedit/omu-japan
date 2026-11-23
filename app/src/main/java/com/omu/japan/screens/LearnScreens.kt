package com.omu.japan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.omu.japan.data.AppContainer
import com.omu.japan.model.AppSettings
import com.omu.japan.model.UserProgress
import com.omu.japan.navigation.Route
import com.omu.japan.ui.components.*
import com.omu.japan.ui.theme.OmuTheme
import kotlinx.coroutines.launch

@Composable
fun KanaScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, kind: String, nav: NavController) {
    val scope = rememberCoroutineScope()
    val list = if (kind == "katakana") container.content.katakana else container.content.hiragana
    var filter by remember { mutableStateOf("all") }
    val filters = listOf("all", "basic", "dakuten", "handakuten", "combo")
    ScreenScaffold(if (kind == "katakana") "Katakana" else "Hiragana", "${list.size} items · tap details · audio · learned/favorite", onBack = { nav.popBackStack() }) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { f -> GhostBtn(f.replaceFirstChar { it.uppercase() }, onClick = { filter = f }) }
        }
        Spacer(Modifier.height(12.dp))
        list.filter { filter == "all" || it.type == filter }.forEach { item ->
            val key = "$kind:${item.id}"
            val learned = progress.learnedIds.contains(key)
            val fav = progress.favoriteIds.contains(key)
            SectionCard(onClick = { nav.navigate(Route.Detail.create(kind, item.id)) }) {
                Row(Modifier.fillMaxWidth()) {
                    Text(item.char, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = OmuTheme.c.text, modifier = Modifier.width(72.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.romaji, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
                        Text(item.example, color = OmuTheme.c.muted)
                        Text((if (learned) "✓ learned" else "not learned") + if (fav) " · ★" else "", color = OmuTheme.c.muted, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostBtn("Audio", Modifier.weight(1f)) { container.tts.speak(item.char, settings.audioEnabled) }
                    GhostBtn(if (learned) "Learned" else "Learn", Modifier.weight(1f)) { scope.launch { container.repository.markLearned(kind, item.id) } }
                    GhostBtn(if (fav) "★" else "☆", Modifier.weight(1f)) { scope.launch { container.repository.toggleFavorite(kind, item.id) } }
                }
            }
        }
    }
}

@Composable
fun VocabScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, nav: NavController) {
    val scope = rememberCoroutineScope()
    var cat by remember { mutableStateOf("All") }
    val cats = listOf("All") + container.content.vocabulary.map { it.category }.distinct()
    ScreenScaffold("Vocabulary", "${container.content.vocabulary.size} words · categories · audio · favorites", onBack = { nav.popBackStack() }) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cats.forEach { c -> GhostBtn(c, onClick = { cat = c }) }
        }
        Spacer(Modifier.height(12.dp))
        container.content.vocabulary.filter { cat == "All" || it.category == cat }.forEach { item ->
            val key = "vocab:${item.id}"; val learned = progress.learnedIds.contains(key); val fav = progress.favoriteIds.contains(key)
            SectionCard(item.word, "${item.kana} · ${item.romaji}", onClick = { nav.navigate(Route.Detail.create("vocab", item.id)) }) {
                Text(item.meaning, color = OmuTheme.c.accent)
                Text("${item.example} — ${item.exampleMeaning}", color = OmuTheme.c.muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostBtn("Audio", Modifier.weight(1f)) { container.tts.speak(item.word, settings.audioEnabled) }
                    GhostBtn(if (learned) "Learned" else "Learn", Modifier.weight(1f)) { scope.launch { container.repository.markLearned("vocab", item.id) } }
                    GhostBtn(if (fav) "★" else "☆", Modifier.weight(1f)) { scope.launch { container.repository.toggleFavorite("vocab", item.id) } }
                }
            }
        }
    }
}

@Composable
fun KanjiScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, nav: NavController) {
    val scope = rememberCoroutineScope()
    ScreenScaffold("Kanji", "${container.content.kanji.size} kanji · readings · JLPT · examples", onBack = { nav.popBackStack() }) {
        container.content.kanji.forEach { item ->
            val key = "kanji:${item.id}"; val learned = progress.learnedIds.contains(key); val fav = progress.favoriteIds.contains(key)
            SectionCard(onClick = { nav.navigate(Route.Detail.create("kanji", item.id)) }) {
                Row(Modifier.fillMaxWidth()) {
                    Text(item.kanji, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = OmuTheme.c.text, modifier = Modifier.width(76.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.meaning, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
                        Text("On: ${item.onyomi} · Kun: ${item.kunyomi}", color = OmuTheme.c.muted)
                        Text("JLPT ${item.jlpt} · ${item.strokes} strokes", color = OmuTheme.c.muted, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostBtn("Audio", Modifier.weight(1f)) { container.tts.speak(item.kanji, settings.audioEnabled) }
                    GhostBtn(if (learned) "Learned" else "Learn", Modifier.weight(1f)) { scope.launch { container.repository.markLearned("kanji", item.id) } }
                    GhostBtn(if (fav) "★" else "☆", Modifier.weight(1f)) { scope.launch { container.repository.toggleFavorite("kanji", item.id) } }
                }
            }
        }
    }
}

@Composable
fun GrammarScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, nav: NavController) {
    val scope = rememberCoroutineScope()
    ScreenScaffold("Grammar", "JLPT N5-N1 lessons with examples and practice", onBack = { nav.popBackStack() }) {
        container.content.grammar.forEach { item ->
            val key = "grammar:${item.id}"; val learned = progress.learnedIds.contains(key)
            SectionCard(item.title, "${item.jlpt} · ${item.level}", onClick = { nav.navigate(Route.Detail.create("grammar", item.id)) }) {
                Text(item.explanation, color = OmuTheme.c.text)
                Spacer(Modifier.height(6.dp))
                Text("例: ${item.example}", color = OmuTheme.c.accent)
                Text(item.kana, color = OmuTheme.c.muted, style = MaterialTheme.typography.bodyMedium)
                Text(item.meaning, color = OmuTheme.c.muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostBtn("Audio", Modifier.weight(1f)) { container.tts.speak(item.example, settings.audioEnabled) }
                    GhostBtn(if (learned) "Learned" else "Mark learned", Modifier.weight(1f)) { scope.launch { container.repository.markLearned("grammar", item.id) } }
                }
            }
        }
    }
}
