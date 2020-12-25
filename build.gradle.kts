import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "cacao"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.jetbrains.team/maven/p/ui/dev")
    }
}

dependencies {
    implementation("org.jetbrains.skiko:skiko-jvm-runtime-windows:0.1.5")
    implementation("com.squareup.okio:okio:2.9.0")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    explicitApi()
}