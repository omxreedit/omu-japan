package com.omu.japan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun DetailScreen(container: AppContainer, settings: AppSettings, progress: UserProgress, type: String, id: String, nav: NavController) {
    val scope = rememberCoroutineScope()
    val key = "$type:$id"
    val learned = progress.learnedIds.contains(key)
    val fav = progress.favoriteIds.contains(key)
    ScreenScaffold("Detail", onBack = { nav.popBackStack() }) {
        SectionCard(strong = true) {
            when (type) {
                "hiragana", "katakana" -> {
                    val item = (if (type == "hiragana") container.content.hiragana else container.content.katakana).find { it.id == id }
                    if (item != null) {
                        Text(item.char, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = OmuTheme.c.text, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text(item.romaji, style = MaterialTheme.typography.headlineMedium, color = OmuTheme.c.text)
                        Text("Type: ${item.type}", color = OmuTheme.c.muted)
                        Text("Example: ${item.example}", color = OmuTheme.c.text)
                        Spacer(Modifier.height(10.dp)); PrimaryBtn("Play audio") { container.tts.speak(item.char, settings.audioEnabled) }
                    } else Text("Item not found", color = OmuTheme.c.muted)
                }
                "vocab" -> {
                    val item = container.content.vocabulary.find { it.id == id }
                    if (item != null) {
                        Text(item.word, style = MaterialTheme.typography.headlineLarge, color = OmuTheme.c.text)
                        Text("${item.kana} · ${item.romaji}", color = OmuTheme.c.accent)
                        Text("Meaning: ${item.meaning}", color = OmuTheme.c.text)
                        Text("Category: ${item.category}", color = OmuTheme.c.muted)
                        Text("Example: ${item.example}", color = OmuTheme.c.text)
                        Text(item.exampleKana, color = OmuTheme.c.muted)
                        Text(item.exampleMeaning, color = OmuTheme.c.muted)
                        Spacer(Modifier.height(10.dp)); PrimaryBtn("Play audio") { container.tts.speak(item.word, settings.audioEnabled) }
                    } else Text("Item not found", color = OmuTheme.c.muted)
                }
                "kanji" -> {
                    val item = container.content.kanji.find { it.id == id }
                    if (item != null) {
                        Text(item.kanji, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = OmuTheme.c.text, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text(item.meaning, style = MaterialTheme.typography.headlineMedium, color = OmuTheme.c.text)
                        Text("Onyomi: ${item.onyomi}", color = OmuTheme.c.muted)
                        Text("Kunyomi: ${item.kunyomi}", color = OmuTheme.c.muted)
                        Text("Strokes: ${item.strokes} · JLPT ${item.jlpt}", color = OmuTheme.c.muted)
                        item.examples.forEach { e -> Text("• ${e.word} (${e.reading}) - ${e.meaning}", color = OmuTheme.c.text) }
                        Spacer(Modifier.height(10.dp)); PrimaryBtn("Play audio") { container.tts.speak(item.kanji, settings.audioEnabled) }
                    } else Text("Item not found", color = OmuTheme.c.muted)
                }
                "grammar" -> {
                    val item = container.content.grammar.find { it.id == id }
                    if (item != null) {
                        Text(item.title, style = MaterialTheme.typography.headlineMedium, color = OmuTheme.c.text)
                        Text("${item.jlpt} · ${item.level}", color = OmuTheme.c.accent)
                        Spacer(Modifier.height(8.dp))
                        Text(item.explanation, color = OmuTheme.c.text)
                        Text("Example: ${item.example}", color = OmuTheme.c.text)
                        Text(item.kana, color = OmuTheme.c.muted)
                        Text(item.romaji, color = OmuTheme.c.muted)
                        Text(item.meaning, color = OmuTheme.c.muted)
                        Spacer(Modifier.height(8.dp))
                        Text("Common mistake: ${item.mistakes}", color = OmuTheme.c.muted)
                        Spacer(Modifier.height(10.dp)); PrimaryBtn("Play audio") { container.tts.speak(item.example, settings.audioEnabled) }
                    } else Text("Item not found", color = OmuTheme.c.muted)
                }
                else -> Text("Unknown type", color = OmuTheme.c.muted)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostBtn(if (learned) "Learned ✓" else "Mark learned", Modifier.weight(1f)) { scope.launch { container.repository.markLearned(type, id) } }
                GhostBtn(if (fav) "★ Favorited" else "☆ Favorite", Modifier.weight(1f)) { scope.launch { container.repository.toggleFavorite(type, id) } }
            }
        }
    }
}

@Composable
fun SearchScreen(container: AppContainer, nav: NavController) {
    var q by remember { mutableStateOf("") }
    val hits = remember(q) { if (q.isBlank()) emptyList() else container.content.allSearchable().filter { it.title.contains(q, true) || it.subtitle.contains(q, true) || it.type.contains(q, true) }.take(50) }
    ScreenScaffold("Search", "Search kana, vocabulary, kanji and grammar", onBack = { nav.popBackStack() }) {
        SectionCard { androidx.compose.material3.OutlinedTextField(value = q, onValueChange = { q = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Type to search...") }, singleLine = true) }
        if (q.isBlank()) SectionCard { Text("Type a word, romaji, kanji, or grammar title.", color = OmuTheme.c.muted) }
        hits.forEach { hit ->
            SectionCard(hit.title, hit.subtitle, onClick = { nav.navigate(Route.Detail.create(hit.type, hit.id)) }) { Text(hit.type.uppercase(), color = OmuTheme.c.accent, style = MaterialTheme.typography.labelMedium) }
        }
        if (q.isNotBlank() && hits.isEmpty()) SectionCard { Text("No results found.", color = OmuTheme.c.muted) }
    }
}

@Composable
fun FavoritesScreen(container: AppContainer, progress: UserProgress, nav: NavController) {
    ScreenScaffold("Favorites", "Your saved items for quick revision", onBack = { nav.popBackStack() }) {
        if (progress.favoriteIds.isEmpty()) SectionCard { Text("No favorites yet. Tap ☆ Save on any item.", color = OmuTheme.c.muted) }
        progress.favoriteIds.forEach { key ->
            val sep = key.indexOf(':'); if (sep <= 0) return@forEach
            val type = key.substring(0, sep); val id = key.substring(sep + 1)
            val title = when(type){
                "hiragana"->container.content.hiragana.find{it.id==id}?.let{"${it.char} · ${it.romaji}"}
                "katakana"->container.content.katakana.find{it.id==id}?.let{"${it.char} · ${it.romaji}"}
                "vocab"->container.content.vocabulary.find{it.id==id}?.word
                "kanji"->container.content.kanji.find{it.id==id}?.let{"${it.kanji} · ${it.meaning}"}
                "grammar"->container.content.grammar.find{it.id==id}?.title
                else->null
            } ?: return@forEach
            SectionCard(title, type.uppercase(), onClick = { nav.navigate(Route.Detail.create(type,id)) }) {}
        }
    }
}

@Composable
fun MistakesScreen(container: AppContainer, progress: UserProgress, nav: NavController) {
    val scope = rememberCoroutineScope()
    ScreenScaffold("Mistakes", "Wrong quiz answers saved for review", onBack = { nav.popBackStack() }) {
        if (progress.mistakes.isEmpty()) SectionCard { Text("No mistakes saved yet. Take a quiz!", color = OmuTheme.c.muted) }
        progress.mistakes.takeLast(30).reversed().forEach { m ->
            SectionCard(m.question) {
                Text("Your answer: ${m.chosen}", color = OmuTheme.c.muted)
                Text("Correct: ${m.correct} · ${m.date}", color = OmuTheme.c.accent)
            }
        }
        if (progress.mistakes.isNotEmpty()) GhostBtn("Clear mistakes") { scope.launch { container.repository.clearMistakes() } }
    }
}
