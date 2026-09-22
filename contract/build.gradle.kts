import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("com.diffplug.spotless") version "6.25.0"
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    implementation(kotlin("stdlib"))
    implementation("com.wavesenterprise:we-contract-sdk-grpc")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "com.ororura.bootstrap.Dispatcher"
    }
}

project.setProperty("mainClassName", "com.ororura.bootstrap.Dispatcher")


tasks.test {
    useJUnitPlatform()
}


// Java 17-compatible formatter; formatting must not change contract semantics.
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.19.2")
    }
}
