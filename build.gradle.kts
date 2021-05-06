import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.4.32"
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
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.litote.kmongo:kmongo-coroutine:4.2.5")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")
    implementation("org.apache.poi:poi:5.0.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")

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