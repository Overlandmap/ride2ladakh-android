pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox Maps SDK (itinerary screen only) is served from an
        // authenticated Maven repo. The secret download token (sk…, scope
        // DOWNLOADS:READ) is read from MAPBOX_DOWNLOADS_TOKEN in
        // ~/.gradle/gradle.properties — never committed.
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull ?: ""
            }
        }
    }
}

rootProject.name = "Ride2Ladakh"
include(":core")
include(":app")
