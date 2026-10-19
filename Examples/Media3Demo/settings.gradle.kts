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
        maven {
            url = uri("https://pub-61830e1369904d19af505ff74fd55ba4.r2.dev/maven")
            content { includeGroup("io.meshstream") }
        }
    }
}

rootProject.name = "MeshStreamCustomerMinimal"
include(":app")
