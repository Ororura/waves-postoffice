import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    java
    application

    id("io.spring.dependency-management")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.wavesenterprise.app"
version = "1.0.0-SNAPSHOT"

val kotlinVersion: String by project
val springBootVersion: String by project
val jacksonVersion: String by project
val jnaVersion: String by project
val weSdkBomVersion: String by project

repositories {
    mavenCentral()

    maven {
        name = "waves-releases"
        url = uri("https://artifacts.wavesenterprise.com/repository/maven-releases/")
        mavenContent { releasesOnly() }
    }

    maven {
        name = "waves-snapshots"
        url = uri("https://artifacts.wavesenterprise.com/repository/maven-snapshots/")
        mavenContent { snapshotsOnly() }
    }

    if (providers.gradleProperty("useMavenLocal").orNull == "true") {
        mavenLocal()
    }
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
            bomProperty("kotlin.version", kotlinVersion)
        }
        mavenBom("com.wavesenterprise:we-sdk-bom:$weSdkBomVersion") {
            bomProperty("kotlin.version", kotlinVersion)
        }
        mavenBom("com.fasterxml.jackson:jackson-bom:$jacksonVersion")
    }

    dependencies {
        dependency("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        dependency("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        dependency("net.java.dev.jna:jna:$jnaVersion")
    }
}

dependencies {
    implementation("com.wavesenterprise:we-contract-sdk-grpc")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("com.ororura.bootstrap.PostOfficeContractDispatcher")
}

tasks.withType<ShadowJar>().configureEach {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.19.2")
    }
}
