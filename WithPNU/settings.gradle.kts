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
    plugins {
        id("com.android.application") version "8.5.0"
        //alias(libs.plugins.android.application)
        id("org.jetbrains.kotlin.android") version "1.9.0"
        //alias(libs.plugins.jetbrains.kotlin.android)
        id("com.google.gms.google-services") version "4.4.2"
        //alias(libs.plugins.jetbrains.kotlin.android)
        //alias(libs.plugins.google.gms.google.services)

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

//    versionCatalogs {
//        create("libs") {
//            from(files("gradle/libs.versions.toml"))
//        }
//    } 7.4이상 버전에서는 추가할 필요가 없다

}

rootProject.name = "My Application"
include(":app")
 