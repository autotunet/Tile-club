package com.example.data.localization

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String
) {
    ESPAÑOL("es", "Español", "🇪🇸"),
    ENGLISH("en", "English", "🇺🇸"),
    PORTUGUES("pt", "Português", "🇧🇷"),
    FRANCAIS("fr", "Français", "🇫🇷"),
    DEUTSCH("de", "Deutsch", "🇩🇪"),
    ITALIANO("it", "Italiano", "🇮🇹");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ESPAÑOL
        }
    }
}
