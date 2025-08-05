standard library package Occurrences { 
     
    class Occurrence :> Base::Anything; 
    class Life :> Occurrence;  
    
    assoc HappensLink :> Links::BinaryLink {
        end feature happensSource: Occurrence[0..*] redefines source;
        end feature happensTarget: Occurrence[0..*] redefines target;
    }
    
    assoc HappensBefore :> HappensLink {
        end feature earlierOccurrence: Occurrence [1] redefines happensSource;
        end feature laterOccurrence: Occurrence [1] redefines happensTarget; 
    }

    assoc HappensJustBefore :> HappensBefore;
  
    assoc HappensDuring :> Links::BinaryLink;
   
    assoc OutsideOf :> Links::BinaryLink; 
    
    assoc JustOutsideOf :> Links::BinaryLink; 
    
    assoc InsideOf :> Links::BinaryLink;
}
