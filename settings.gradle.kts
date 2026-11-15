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

rootProject.name = "ChessAcademy"

// App-Modul
include(":app")

// Core-Module
include(":core:common")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:audio")
include(":core:network")

// Schach-Kern-Module
include(":chess:rules")
include(":chess:pgn")
include(":chess:engine")
include(":chess:board")

// Feature-Module
include(":feature:onboarding")
include(":feature:home")
include(":feature:play")
include(":feature:academy")
include(":feature:training")
include(":feature:coach")
include(":feature:analyze")
include(":feature:profile")
include(":feature:settings")
