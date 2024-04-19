import org.gradle.jvm.tasks.Jar
plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")

    // The following line allows to load io.gatling.gradle plugin and directly apply it
    id("io.gatling.gradle") version "3.10.3"
    application
}


gatling {
    // WARNING: options below only work when logback config file isn't provided
    logLevel = "WARN" // logback root level
    logHttp = io.gatling.gradle.LogHttp.NONE // set to 'ALL' for all HTTP traffic in TRACE, 'FAILURES' for failed HTTP traffic in DEBUG

    enterprise.closureOf<Any> {
        // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-cloud
        // Enterprise Self-Hosted configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-self-hosted
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://packages.confluent.io/maven/")
}

dependencies {
    gatling("ru.tinkoff:gatling-kafka-plugin_2.13:0.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("stdlib"))
}

configurations {
    create("fatJarDependencies") {
        extendsFrom(configurations.getByName("gatling"))
    }
}

tasks.register("fatJar", Jar::class) {
    dependsOn("gatlingClasses", "processResources")
    group = "build"
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "Engine"
        )
    }

    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveClassifier.set("all")

    from(sourceSets["gatling"].output)// this is needed for the gatling engine
    from(configurations["fatJarDependencies"].map { if (it.isDirectory) it else project.zipTree(it) })// this
    // is needed for GatlingPropertiesBuilder
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })//this needs to be at the end so the jackson dependencies don't collide with each other, according to gradle
    //is the only one needed but without the others the jar fails to run


    with(tasks.named("jar").get() as CopySpec)
}