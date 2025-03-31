standard library package Items {
    class Item :> Occurrences::Occurrence; // From KerML 
    feature items: Item [0 .. *];
}
