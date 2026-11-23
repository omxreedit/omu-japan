package com.omu.japan.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omu.japan.data.AppContainer
import com.omu.japan.model.AppSettings
import com.omu.japan.model.UserProgress
import com.omu.japan.navigation.Route
import com.omu.japan.navigation.bottomRoutes
import com.omu.japan.screens.*
import com.omu.japan.ui.glass.GlassCard
import com.omu.japan.ui.theme.OmuTheme

data class NavItem(val route: Route, val label: String, val icon: ImageVector)

@Composable
fun OmuAppRoot(container: AppContainer, settings: AppSettings, progress: UserProgress) {
    val nav = rememberNavController()
    val items = listOf(
        NavItem(Route.Home, "Home", Icons.Rounded.Home),
        NavItem(Route.Learn, "Learn", Icons.Rounded.AutoStories),
        NavItem(Route.Practice, "Practice", Icons.Rounded.FitnessCenter),
        NavItem(Route.Library, "Library", Icons.Rounded.LocalLibrary),
        NavItem(Route.Profile, "Profile", Icons.Rounded.Person)
    )
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(OmuTheme.c.bg, OmuTheme.c.bg2, OmuTheme.c.bg3, OmuTheme.c.bg)
                )
            )
    ) {
        // atmospheric accents
        Box(Modifier.size(260.dp).offset(x = (-60).dp, y = 60.dp).background(Brush.radialGradient(listOf(OmuTheme.c.accent.copy(alpha = 0.18f), Color.Transparent))))
        Box(Modifier.size(220.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = 120.dp).background(Brush.radialGradient(listOf(Color(0x33FFFFFF), Color.Transparent))))

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).navigationBarsPadding()) {
                    GlassCard(shape = RoundedCornerShape(28.dp), strong = true) {
                        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                            items.forEach { item ->
                                val selected = current == item.route.path
                                GlassCard(
                                    shape = RoundedCornerShape(20.dp),
                                    strong = selected,
                                    onClick = {
                                        if (current != item.route.path) nav.navigate(item.route.path) {
                                            popUpTo(Route.Home.path) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                ) {
                                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(item.icon, item.label, tint = if (selected) OmuTheme.c.accent else OmuTheme.c.muted)
                                        Text(item.label, style = MaterialTheme.typography.labelMedium, color = if (selected) OmuTheme.c.text else OmuTheme.c.muted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { inner ->
            NavHost(
                navController = nav,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(inner),
                enterTransition = { fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220)) },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220)) }
            ) {
                composable(Route.Home.path) { HomeScreen(container, settings, progress, nav) }
                composable(Route.Learn.path) { LearnScreen(nav) }
                composable(Route.Practice.path) { PracticeScreen(nav) }
                composable(Route.Library.path) { LibraryScreen(nav) }
                composable(Route.Profile.path) { ProfileScreen(settings, progress, nav) }
                composable(Route.Hiragana.path) { KanaScreen(container, settings, progress, "hiragana", nav) }
                composable(Route.Katakana.path) { KanaScreen(container, settings, progress, "katakana", nav) }
                composable(Route.Vocab.path) { VocabScreen(container, settings, progress, nav) }
                composable(Route.Kanji.path) { KanjiScreen(container, settings, progress, nav) }
                composable(Route.Grammar.path) { GrammarScreen(container, settings, progress, nav) }
                composable(Route.Quiz.path) { QuizScreen(container, settings, nav) }
                composable(Route.QuizResult.path) { QuizResultScreen(nav) }
                composable(Route.Daily.path) { DailyScreen(nav) }
                composable(Route.Search.path) { SearchScreen(container, nav) }
                composable(Route.Favorites.path) { FavoritesScreen(container, progress, nav) }
                composable(Route.Mistakes.path) { MistakesScreen(container, progress, nav) }
                composable(Route.Stats.path) { StatsScreen(progress) }
                composable(Route.Settings.path) { SettingsScreen(container, settings) }
                composable(Route.Teachers.path) { TeachersScreen(container) }
                composable(Route.Detail.path, arguments = listOf(navArgument("type") { type = NavType.StringType }, navArgument("id") { type = NavType.StringType })) { be ->
                    DetailScreen(container, settings, progress, be.arguments?.getString("type") ?: "", be.arguments?.getString("id") ?: "", nav)
                }
            }
        }
    }
}
