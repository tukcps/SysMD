standard library package Parts { 
    class Part :> Items::Item;                  // Base class for all part definitions
    feature parts: Part[0 .. *];                // Features that is superset of all part usages 
    // subsets items;                           // iff we support subsetting. 
}
