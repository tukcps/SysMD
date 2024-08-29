
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

/*
 * Gradle build file for SysMD Notebook.
 * - gradle clean: cleans up.
 * - gradle build: builds it.
 * - gradle test: tests it.
 * - gradle publish: publishes it as Maven pom in the cpsgit repository.
 *
 * IMPORTANT: in plugins, you must
 * - comment-out the kotlin version if you use it in a hierarchical Gradle build
 * - comment in the kotlin version if you are using it in a standalone Gradle build
 * - also set the value standalone according to your setup
 */
group   = "com.github.tukcps"
version = "3.0.13"
val jaaddVersion = "3.1.0"
val sysmlapiVersion = "3.0.2"

val kotlinVersion = getKotlinPluginVersion()

if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("The build must be run with Java 17 or newer; best use Java 19!")
} else {
    val versionFile = file("src/main/resources/version")
    versionFile.createNewFile()
    versionFile.writeText("$version")
}


// Plugins needed: id and versions.
plugins {
    // Plugin that checks for updates:
    id("com.github.ben-manes.versions") version "0.51.0"
    // id("java-library")
    id("idea")
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    alias(libs.plugins.jetbrainsCompose) apply true
    alias(libs.plugins.compose.compiler) apply true
    id("maven-publish")
}

// Repositories where to search
repositories {
    mavenCentral()
    google()

    //jAADD
    maven ("https://cpsgit.informatik.uni-kl.de/api/v4/projects/87/packages/maven") {
        name = "GitLab"
        credentials(HttpHeaderCredentials::class) {
            name = "Deploy-Token"
            value = "m2XeQuM-1sqMXeUX-2-X"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }

    // SysML-API
    maven("https://cpsgit.informatik.uni-kl.de/api/v4/projects/164/packages/maven") {
        name = "GitLab"
        credentials(HttpHeaderCredentials::class) {
            name = "Deploy-Token"
            value = "ri2mY2J5Y5tqT2U4JfJR"
        }
        authentication { create<HttpHeaderAuthentication>("header") }
    }

    // LaTeX Rendering for UI
    maven ("https://jitpack.io")
}


// Dependencies
dependencies {
    implementation(compose.desktop.currentOs)

    // Check if we do a standalone-build or a hierarchical build with git submodules
    val standalone: Boolean = if (org.gradle.internal.os.OperatingSystem.current().isWindows)
        !File("${System.getProperty("user.home")}\\agila.hierarchical.build").exists()
    else
        !File("/tmp/agila.hierarchical.build").exists()

    if(file("../jaadd").exists()&&!standalone) {
        println("  *** using jaadd from local clone in ./jaadd           ***")
        implementation(project(":jaadd"))
    } else {
        println("  *** using jaadd $jaaddVersion from CPS Maven repo     ***")
        implementation("com.github.tukcps:jaadd:$jaaddVersion")
    }

    if (file("../agila-base").exists()&&!standalone) {
        println("  *** using SysMD from local clone in ./sysmd           ***")
        implementation(project(":sysmlapi"))
    } else {
        println("  *** using SysML API version $sysmlapiVersion from CPS Maven repo     ***")
        implementation("com.github.tukcps:sysmlapi:$sysmlapiVersion")
    }

    // For UUID version 5 (name-based)
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")

    implementation("org.jetbrains.compose.material3:material3-desktop:1.6.11")

    // These are necessary for the annotations in the models.
    implementation("org.springframework:spring-web:6.1.10")
    implementation("org.slf4j:slf4j-nop:2.0.13")

    // Needed for annotations for Spring Boot in package rest
    implementation("org.json:json:20240303")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")

    // Parsing markdown to AST
    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.22.0")
    implementation("org.commonmark:commonmark-ext-image-attributes:0.22.0")
    implementation("org.commonmark:commonmark-ext-yaml-front-matter:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.22.0")
    implementation("org.commonmark:commonmark-ext-ins:0.22.0")

    // Some more icons ...
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.6.2")
    implementation("br.com.devsrsouza.compose.icons.jetbrains:line-awesome:1.0.0")

    // Rendering of LaTeX in MD
    implementation("com.github.opencollab.jlatexmath:jlatexmath:1.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Needed for state diagrams (HOOD GmbH)
    implementation ("org.diagramsascode:diagramsascode-image:0.1.5")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}


// The application created in various distributables
// only for standalone:
compose.desktop {

    val standalone = !File("/tmp/agila.hierarchical.build").exists()
    if (standalone) {
        application {
            mainClass = "com.github.tukcps.sysmd.MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = "SysMD notebook"
                description = "Notebook frontend for the SysMD language"
                copyright   = "(c) 2020-2024, University of Kaiserslautern, Chair of Cyber-Physical Systems"

                macOS {
                    setDockNameSameAsPackageName = true
                    iconFile.set(project.file("src/main/resources/SysMD-Icon.icns"))
                }
                windows {
                    shortcut = true
                    menu = true
                    menuGroup = "Genial!"
                    iconFile.set(project.file("src/main/resources/SysMD-Icon.ico"))
                }
                linux {
                    iconFile.set(project.file("src/main/resources/SysMD-Icon.png"))
                }
            }
        }
    }
}


// Publishing of jar and pom
publishing {
    publications {
        create<MavenPublication>("appel") {
            from(components["java"])
        }
    }

    repositories {
        maven("https://cpsgit.informatik.uni-kl.de/api/v4/projects/117/packages/maven") {
            name = "GitLab"
            credentials(HttpHeaderCredentials::class) {
                name = "Deploy-Token"
                value = "jDN-qwxvyFW9DxXPqCJL"
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}


// Configuration of tasks
tasks.test {
    useJUnitPlatform()
}

// Generate Bytecode for v17
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}


tasks.withType<JavaCompile> {
    options.release.set(17)
}
