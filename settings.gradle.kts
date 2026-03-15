rootProject.name = "couplebase"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Core modules
include(":core:common")
include(":core:model")
include(":core:database")
include(":core:network")
include(":core:sync")
include(":core:domain")
include(":core:datastore")
include(":core:ui")

// Feature modules
include(":feature:auth")
include(":feature:wedding-checklist")
include(":feature:wedding-budget")
include(":feature:wedding-guests")
include(":feature:wedding-vendors")
include(":feature:wedding-timeline")
include(":feature:couple-profile")
include(":feature:couple-goals")
include(":feature:finance-budget")
include(":feature:finance-expenses")
include(":feature:finance-savings")
include(":feature:comm-notes")
include(":feature:comm-journal")
include(":feature:comm-checkin")
include(":feature:settings")

// App
include(":composeApp")
