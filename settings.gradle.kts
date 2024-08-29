rootProject.name = "sysmd"


// settings is executed in standalone-build, so we delete the marker for hierarchical projects here.
// Check if we do a standalone-build or a hierarchical build with git submodules
if (org.gradle.internal.os.OperatingSystem.current().isWindows)
    File("${System.getProperty("user.home")}\\agila.hierarchical.build").delete()
else
    File("/tmp/agila.hierarchical.build").delete()
