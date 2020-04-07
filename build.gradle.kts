plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

group = properties["pluginGroup"]!!
version = properties["pluginVersion"]!!

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/") //paper
    maven(url = "https://repo.dmulloy2.net/nexus/repository/public/") //protocollib
    maven(url = "https://jitpack.io/") //tap, psychic
    maven(url = "https://maven.enginehub.org/repo/") //worldedit
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8")) //kotlin
    compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT") //paper
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0") //protocollib
    compileOnly("com.github.noonmaru:tap:2.3.3") //tap
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.1.0") //worldedit
    implementation("com.github.noonmaru:kommand:0.1.9")

    testCompileOnly("junit:junit:4.12") //junit
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
    shadowJar {
        relocate("com.github.noonmaru.kommand", "com.github.noonmaru.regions.shaded")
        archiveClassifier.set("dist")
    }
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\test\\plugins")
    }
}

publishing {
    publications {
        create<MavenPublication>("Regions") {
            artifactId = project.name
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}