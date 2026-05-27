import org.gradle.internal.os.OperatingSystem

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
version = "4.2.3"               // must be number.number.number
val aaddVersion = "0.1.15"
val sysmlapiVersion = "3.9.12"
val useMavenAADD = true
val useMavenSysMLAPI = true

if (JavaVersion.current() < JavaVersion.VERSION_21) {
    throw GradleException("The build must be run with JVM 21 or newer.")
} else {
    val versionFile = file("src/main/resources/version")
    versionFile.createNewFile()
    versionFile.writeText("$version")
}

// Plugins needed: id and versions.
plugins {
    // Plugin that checks for updates of dependencies
    id("com.github.ben-manes.versions") version "0.53.0"
    id("idea")
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    alias(libs.plugins.jetbrainsCompose) apply true
    alias(libs.plugins.compose.compiler) apply true
    id("maven-publish")
    kotlin("plugin.spring") version "2.3.21"
}

// Repositories where to search
repositories {
    mavenCentral()
    google()

    // LaTeX Rendering for UI
    maven ("https://jitpack.io")
}

kotlin {
    jvmToolchain(21)
}

// Dependencies
dependencies {
    implementation(compose.desktop.currentOs)

    // Check if we do a standalone-build or a hierarchical build with git submodules
    if(file("aadd").exists() && !useMavenAADD) {
        println("  *** using AADD from project clone in ./aadd               ***")
        implementation(project(":aadd"))
    } else {
        println("  *** using AADD v$aaddVersion from Maven repository            ***")
        implementation("io.github.tukcps:aadd:$aaddVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    }

    if (!useMavenSysMLAPI) {
        println("  *** using SysMLv2API from project clone in ./sysmlapi     ***")
        implementation(project(":sysmlapi"))
    } else {
        println("  *** using SysML API $sysmlapiVersion from Maven repository        ***")
        implementation("io.github.tukcps:sysmlapi:$sysmlapiVersion")
    }

    // For UUID version 5 (name-based)
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")


    implementation("org.jetbrains.compose.material3:material3-desktop:1.9.0-beta03")

    // These are necessary for the annotations in the models.
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")

    // Open API / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // Needed for annotations for Spring Boot in package rest
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Parsing Markdown to AST
    implementation("org.commonmark:commonmark:0.28.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.28.0")
    implementation("org.commonmark:commonmark-ext-image-attributes:0.28.0")
    implementation("org.commonmark:commonmark-ext-yaml-front-matter:0.28.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.28.0")
    implementation("org.commonmark:commonmark-ext-ins:0.28.0")

    // Some more icons ...
    implementation(libs.compose.resources)
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

    // Rendering of LaTeX in MD
    implementation("com.github.opencollab.jlatexmath:jlatexmath:1.0.7")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Needed for state diagrams (HOOD GmbH)
    implementation("org.diagramsascode:diagramsascode-image:0.1.5")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.19")
    implementation("org.apache.xmlgraphics:batik-codec:1.19")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.20")

    // compose ui tests
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.5") {
        exclude(group = "org.mockito", module = "mockito-core")
    }

    testImplementation(libs.compose.ui.test)
    testImplementation(libs.compose.ui.test.junit4)
    implementation(kotlin("stdlib"))
}

// Don't use the regular jar as the project is a spring boot project.
tasks.named<Jar>("jar") {
    enabled = false
}

// Sets the file name of the bootJar
tasks.named<Jar>("bootJar") {
    archiveFileName.set("sysmd-$version.jar")
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}

// Configuration of tasks
tasks.test {
    useJUnitPlatform()
}

/**
 * Task that generates an installer package for SysMD Notebook.
 * For Windows, WiX-Tools 3.0 to 3.11.2 must be installed.
 * If it does not work, make bootJar explicit first and ensure that the folder 'libraries' is empty before.
 */
tasks.register<Exec>("sysMDPackage") {
    description = "Creates the SysMD installer"
    dependsOn("bootJar")

    val os = OperatingSystem.current()

    // Package type, depending on OS
    val packageType = when {
        os.isWindows -> "msi"
        os.isMacOsX -> "dmg"
        os.isLinux -> "deb"
        else -> throw GradleException("Unsupported OS: ${os.name}")
    }

    // Icon path, depending on OS
    val iconPath = when {
        os.isWindows -> "src/main/resources/SysMD-Icon.ico"
        os.isMacOsX -> "src/main/resources/SysMD-Icon.icns"
        os.isLinux -> "src/main/resources/SysMD-Icon.png" // if needed by Linux
        else -> throw GradleException("Unsupported OS: ${os.name}")
    }

    // Additional arguments for jPackage, depending on OS
    val additionalArgs = when {
        os.isWindows -> listOf("--win-shortcut", "--win-menu", "--win-dir-chooser", "--win-per-user-install")
        os.isMacOsX -> emptyList()
        os.isLinux -> emptyList()
        else -> throw GradleException("Unsupported OS: ${os.name}")
    }

    // Combination of arguments in a list of parameters for the command line
    val args = listOf(
        "jpackage",
        "--name", "SysMD Notebook",
        "--vendor", "Univ. Kaiserslautern-Landau, Chair of Cyber-Physical Systems",
        "--app-version", version,
        "--input", "build/libs",
        "--main-jar", "sysmd-$version.jar",
        "--type", packageType,
        "--dest", "build/installer",
        "--icon", iconPath,
        "--app-content", "install",
        "--java-options", """ "-splash:\${"$"}APPDIR/install/SysMD-Logo.png" """,
        "--resource-dir", "src/main/resources"      // location of resources
    ) + additionalArgs

    // Finally, execute jPackage command line
    commandLine(args)
}

/**
 * Clean also deletes the log files.
 */
tasks.named<Delete>("clean") {
    delete(
        "sysmd.log",
        fileTree(projectDir) {
            include("sysmd.log.*.gz")   // z. B. sysmd.log.2025-11-25.gz
        }
    )
}