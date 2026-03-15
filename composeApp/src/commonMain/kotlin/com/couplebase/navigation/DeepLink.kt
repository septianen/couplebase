package com.couplebase.navigation

/**
 * Typed deep link destinations.
 * URI scheme: couplebase://[path]
 *
 * Supported links:
 * - couplebase://join/{code}  → Join couple space with invite code
 * - couplebase://home         → Navigate to Home tab
 * - couplebase://wedding      → Navigate to Wedding tab
 * - couplebase://finance      → Navigate to Finance tab
 * - couplebase://us           → Navigate to Us tab
 * - couplebase://me           → Navigate to Me tab
 * - couplebase://settings     → Navigate to Settings screen
 */
sealed interface DeepLink {
    data class JoinCouple(val code: String) : DeepLink
    data class NavigateTab(val tab: MainComponent.Tab) : DeepLink
    data object Settings : DeepLink

    companion object {
        /**
         * Parses a URI string into a typed DeepLink, or null if unrecognized.
         * Supports both `couplebase://` scheme and path-only (for web routing).
         */
        fun parse(uri: String): DeepLink? {
            val path = uri
                .removePrefix("couplebase://")
                .removePrefix("https://couplebase.app/")
                .trimEnd('/')
                .lowercase()

            val segments = path.split("/").filter { it.isNotEmpty() }
            if (segments.isEmpty()) return null

            return when (segments[0]) {
                "join" -> segments.getOrNull(1)?.let { JoinCouple(it) }
                "home" -> NavigateTab(MainComponent.Tab.Home)
                "wedding" -> NavigateTab(MainComponent.Tab.Wedding)
                "finance" -> NavigateTab(MainComponent.Tab.Finance)
                "us" -> NavigateTab(MainComponent.Tab.Us)
                "me" -> NavigateTab(MainComponent.Tab.Me)
                "settings" -> Settings
                else -> null
            }
        }
    }
}
