# OMU日本 (OmuJapan)

Native Android Japanese learning app built with **Kotlin + Jetpack Compose**.

## Build
- **AndroidIDE / Android Studio**: open `/storage/emulated/0/AndroidIDEProjects/omjapan` and run `:app:assembleDebug`.
- **GitHub Actions**: push this repo and run the `Android CI` workflow. Download `omu-japan-debug` artifact.

> Note: This Termux shell cannot run Gradle/Kotlin Compose builds (`gradle/kotlinc` unavailable). Build verification must be done on AndroidIDE or CI.

## Features (current scaffold)
- Liquid-glass styled dark UI (Compose), light/dark setting
- Bottom navigation: Home, Learn, Practice, Library, Profile
- Hiragana/Katakana/Vocabulary/Kanji/Grammar screens from bundled JSON
- Detail screen with audio (TTS), learn/favorite toggles
- Quiz engine with scoring, XP, streak, mistakes, history (DataStore)
- Search, favorites, mistakes, stats, settings, teacher library

## Known gaps / next work
- Full AGSL/RuntimeShader refraction+dispersion+chromatic glass (currently graceful Compose glass fallback)
- Full JLPT/content depth and all requested quiz types
- On-device QA: TTS, performance, low-end/battery fallback, animations
