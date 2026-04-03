library package SysMD {
    private import ScalarValues::String;

    //
    // A Project models the information related to an project or commit.
    //
    metaclass Project {
        feature title: String;
        feature maintainer: String;
        feature license: String;
        feature files: String[1 .. *]; // File names.
        feature icon: String;
     }

    metaclass File [1 .. *] {
        feature cells: String[1..*]; // Cells
    }
 }

// Example
metadata a: SysMD::Project {
    title = "test";
    maintainer = "Christoph Grimm";
    license = "Apache 2.0";
    files = ("file1", "file2");
}