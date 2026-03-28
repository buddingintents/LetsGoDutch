pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LetsGoDutch"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:data",
    ":core:designsystem",
    ":feature:auth",
    ":feature:groups",
    ":feature:expenses",
    ":feature:ledger",
    ":feature:insights",
    ":feature:settlement",
)
