plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(group = "org.postgresql", name = "postgresql", version = "42.+")
    implementation(group = "org.http4k", name = "http4k-core", version = "4.40.+")
    implementation(group = "org.http4k", name = "http4k-server-jetty", version = "4.40.+")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.5.+")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version = "0.4.+")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "2.0.0-alpha5")
    runtimeOnly(group = "org.slf4j", name = "slf4j-simple", version = "2.0.0-alpha5")
    testImplementation(kotlin("test"))
}

tasks.named<Jar>("jar") {
    dependsOn("copyRuntimeDependencies")
    manifest {
        attributes["Main-Class"] = "pt.isel.ls.SessionsServerKt"
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
    }
}

tasks.register<Copy>("copyRuntimeDependencies") {
    into("build/libs")
    from(configurations.runtimeClasspath)
}

tasks.register<JavaExec>("launch") {
    group = "launch"
    description = "Launches the HTTP Server with Postgres implementation"
    this.mainClass.set("pt.isel.ls.SessionsServerKt")
    classpath = sourceSets["main"].runtimeClasspath
    environment("JDBC_DATABASE_URL", "jdbc:postgresql://localhost/?user=postgres&password=postgres")
}
