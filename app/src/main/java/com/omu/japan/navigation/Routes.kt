package com.omu.japan.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Learn : Route("learn")
    data object Practice : Route("practice")
    data object Library : Route("library")
    data object Profile : Route("profile")
    data object Hiragana : Route("hiragana")
    data object Katakana : Route("katakana")
    data object Vocab : Route("vocab")
    data object Kanji : Route("kanji")
    data object Grammar : Route("grammar")
    data object Quiz : Route("quiz")
    data object QuizResult : Route("quiz_result")
    data object Daily : Route("daily")
    data object Search : Route("search")
    data object Favorites : Route("favorites")
    data object Mistakes : Route("mistakes")
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object Teachers : Route("teachers")
    data object Detail : Route("detail/{type}/{id}") { fun create(type: String, id: String) = "detail/$type/$id" }
}

val bottomRoutes = listOf(Route.Home, Route.Learn, Route.Practice, Route.Library, Route.Profile)
