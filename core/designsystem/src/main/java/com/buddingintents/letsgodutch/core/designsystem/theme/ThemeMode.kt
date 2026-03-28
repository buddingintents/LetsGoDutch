package com.buddingintents.letsgodutch.core.designsystem.theme

enum class ThemeMode(val ordinalValue: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2);

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode =
            when (ordinal.coerceIn(0, 2)) {
                0 -> LIGHT
                1 -> DARK
                else -> SYSTEM
            }
    }
}
