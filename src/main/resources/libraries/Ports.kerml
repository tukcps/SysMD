
standard library package Ports { 
    class Port :> Objects::Object;          // The base class of all Ports
    feature ports: Port [1 .. *];           // The superset of all features typed by Port
}
