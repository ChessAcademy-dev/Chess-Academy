pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Chess Academy"

// --- App Container ---
include(":app")

// --- Core Infrastructure ---
include(":core:common")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:audio")
include(":core:network")

// --- Chess Domain ---
include(":chess:rules")
include(":chess:pgn")
include(":chess:engine")
include(":chess:board")

// --- Feature Modules ---
include(":feature:onboarding")
include(":feature:home")
include(":feature:play")
include(":feature:academy")
include(":feature:tactics")
include(":feature:analysis")
include(":feature:profile")
include(":feature:settings")
