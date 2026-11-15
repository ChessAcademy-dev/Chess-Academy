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

// Inkludierung aller Core- und Feature-Module gemäß Projektstruktur
include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:audio")
include(":core:network")
include(":chess:rules")
include(":chess:pgn")
include(":chess:engine")
include(":chess:board")
include(":feature:onboarding")
include(":feature:home")
