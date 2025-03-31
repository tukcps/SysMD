rootProject.name = "sysmd"

//
// Uncomment the following in case you want to include aadd or sysmlapi directly from the file system.
// Then, also adapt the file 'build.gradle.kts' accordingly.
//
if (file("aadd").exists()) {
    include(":aadd")
}
if (file("sysmlapi").exists()) {
    include(":sysmlapi")
}


// 'settings.gradle.kts' is executed in standalone-build, so we delete the marker for hierarchical projects here.
// Check if we do a standalone-build or a hierarchical build with git submodules
// Following is only needed for Backend Agila hierarchical builds.
if (org.gradle.internal.os.OperatingSystem.current().isWindows)
    File("${System.getProperty("user.home")}\\agila.hierarchical.build").delete()
else
    File("/tmp/agila.hierarchical.build").delete()
