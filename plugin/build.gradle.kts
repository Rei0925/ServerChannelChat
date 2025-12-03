plugins{
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.0"
    kotlin("kapt")
}
dependencies {
    //paper
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    //bungee
    compileOnly("net.md-5:bungeecord-api:1.21-R0.5-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bungeecord:4.3.0")
    implementation("co.aikar:acf-bungee:0.5.1-SNAPSHOT")
    //velocity
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(project(":common"))
    implementation("com.worksap.nlp:sudachi:0.7.5")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jsoup:jsoup:1.21.2")
}
version = "0.0.3"

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveBaseName.set("ServerChannelChat")
    archiveVersion.set(version.toString())
    archiveClassifier.set("plugin")
}

tasks.processResources {
    filesMatching(listOf("plugin.yml", "bungee.yml")) {
        expand("version" to project.version)
    }
}