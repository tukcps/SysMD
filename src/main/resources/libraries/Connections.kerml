
standard library package Connections {
    class BinaryConnection :> Links::BinaryLink; 
    feature binaryConnections: BinaryConnection[0..*];
    class Connection :> Links::Link; 
    feature connections: Connection[0..*];
}
