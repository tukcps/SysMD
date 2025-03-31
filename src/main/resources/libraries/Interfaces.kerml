standard library package Interfaces {
    class Interface :> Connections::Connection; 
    feature interfaces: Interface [0 .. *]; 
}
