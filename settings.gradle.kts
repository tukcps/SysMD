import java.nio.file.Files

// Gradle 9 needs an explicit toolchain repository; without it `jvmToolchain(25)` (build.gradle.kts)
// cannot be auto-provisioned and the build fails on machines that only have a JRE or an older JDK.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "sysmd"

/**
 * SysMD uses AADD library for semi-symbolic computations and constraint propagation.
 * - Adapt the files `gradle.properties` to specify where aadd library lies;
 * take care of upper/lower case spelling!
 * - Set in 'build.gradle.kts' whether the dependency from a local directory shall be used,
 * or one from Maven.
 */
val aaddDirectory = providers
    .gradleProperty("aaddDirectory")
    .orNull

val path = aaddDirectory?.let { rootDir.resolve(it) }
val isDir = Files.isDirectory(path!!.toPath())

// If there is a local folder with AADD, use it.
// (also, check build.gradle.kts!)
if (aaddDirectory != null && isDir) {
    println("Found a configured AADD project in: $aaddDirectory")
    includeBuild(aaddDirectory)
}