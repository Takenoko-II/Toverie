plugins {
    kotlin("jvm") version "2.2.21"
}

group = "com.gmail.takenokoii78"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files(
        "../JSON/target/JSON-1.0-SNAPSHOT.jar"
    ))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
