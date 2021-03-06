import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val koinVersion: String by project
val kmongoVersion: String by project
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
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")
    implementation("org.apache.poi:poi:5.0.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}

tasks.jar {
    manifest{
            attributes["Main-Class"] = application.mainClass
            attributes["Implementation-Version"] = archiveVersion
            attributes["Class-Path"] = configurations.runtimeClasspath.get().files.joinToString(" "){
                it.name
            }
    }
}