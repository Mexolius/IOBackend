import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.jvmTarget = "1.8"

group = "com.gumi.moodle"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.litote.kmongo:kmongo-coroutine:4.2.5")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    destinationDirectory.set(file("$rootDir/build/distributions"))

    group = "distribution"

    from(sourceSets.main.get().output)

    manifest{
        attributes(
            "Main-Class" to application.mainClass,
            "Implementation-Version" to archiveVersion
        )
    }

    dependsOn(configurations.runtimeClasspath, "check", "test")
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.assembleDist{
    dependsOn("fatJar")
}