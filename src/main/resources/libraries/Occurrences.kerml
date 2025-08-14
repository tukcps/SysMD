standard library package Occurrences { 
     
    class Occurrence :> Base::Anything;
    abstract feature occurrences: Occurrence[0..*] nonunique subsets Base::things;

    class Life :> Occurrence;  
    
    assoc HappensLink :> Links::BinaryLink {
        end feature happensSource: Occurrence[0..*] redefines source;
        end feature happensTarget: Occurrence[0..*] redefines target;
    }

    assoc HappensBefore :> HappensLink {
        end feature earlierOccurrence: Occurrence [1] redefines happensSource;
        end feature laterOccurrence: Occurrence [1] redefines happensTarget; 
    }

    feature all happensBeforeLinks: HappensBefore[0..*] nonunique; // subsets Links::binaryLinks;

    assoc all HappensJustBefore :> HappensBefore;
  
    assoc all HappensDuring :> Links::BinaryLink;
   
    assoc all OutsideOf :> Links::BinaryLink;
    
    assoc all JustOutsideOf :> Links::BinaryLink;
    
    assoc all InsideOf :> Links::BinaryLink;
}
