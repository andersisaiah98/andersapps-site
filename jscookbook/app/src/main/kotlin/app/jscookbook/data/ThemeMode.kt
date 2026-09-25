package app.jscookbook.data

enum class ThemeMode {
    System,
    Light,
    Dark,
    ;

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        System -> systemIsDark
        Light -> false
        Dark -> true
    }
}
