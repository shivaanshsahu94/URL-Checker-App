package com.example.ui.navigation

/**
 * Sealed class representing all navigable tabs/screens in the app.
 * Provides compile-time safety for navigation routing, preventing
 * silent failures from string typos.
 */
sealed class Tab {
    data object History : Tab()
    data object Settings : Tab()
    data object SettingsHistory : Tab()
    data object SettingsProfile : Tab()
    data object SettingsChecker : Tab()
    data object SettingsTheme : Tab()
    data object SettingsHelp : Tab()
    data object SettingsAbout : Tab()

    /** Whether this tab is a primary tab shown in the bottom navigation bar. */
    val isPrimaryTab: Boolean
        get() = this is History || this is Settings
}
