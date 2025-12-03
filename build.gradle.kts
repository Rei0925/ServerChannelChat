plugins {
    kotlin("jvm") version "2.2.21"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    kotlin("kapt") version "2.2.21" apply false
}

group = "com.mcgendai.chat"
version = "0.0.3"
repositories {
    mavenCentral()
    gradlePluginPortal()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    plugins.apply("org.jetbrains.dokka")
    plugins.apply("org.jetbrains.dokka-javadoc")

    repositories {
        mavenCentral()
        mavenLocal()

        // Paper 用
        maven("https://repo.papermc.io/repository/maven-public/")

        // BungeeCord 用（両方必要）
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.aikar.co/content/groups/aikar/")

        //Fabric 用
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }

    dokka{
        pluginsConfiguration.html {
            customAssets.from(rootProject.file("logo.png"))
            footerMessage.set("(c)2025 Rei0925. All Rights Reserved.")
        }
        dokkaSourceSets.configureEach {
            externalDocumentationLinks.register("scc-docs") {
                url("https://docs.mcgendai.com/")
                packageListUrl("https://docs.mcgendai.com/0.0.3/Kdoc/package-list")
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(rootDir.resolve("docs/"))
    }
    dokkaPublications.javadoc {
        outputDirectory.set(rootDir.resolve("Javadocs/"))
    }
    pluginsConfiguration.html {
        customAssets.from(rootProject.file("logo.png"))
        footerMessage.set("(c)2025 Rei0925. All Rights Reserved.")
    }
    dokkaSourceSets.configureEach {
        externalDocumentationLinks.register("scc-docs") {
            url("https://docs.mcgendai.com/")
            packageListUrl("https://docs.mcgendai.com/0.0.3/Kdoc/package-list")
        }
    }
}

dependencies {
    dokka(project(":common"))
    dokka(project(":mod"))
    dokka(project(":plugin"))
}