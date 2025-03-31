package ISO26262 {
    class Element;
    class Function :> Element;
    class Component :> Element {
        feature Availability: ScalarValues::Boolean(true) = bySpecializations(Availability);
        feature SecurityOfSupply: ScalarValues::Real(0.0 .. 1.0); 
    }
    class System :> Component; 
    class Part :> Component; 
    class Software :> Component;
    class Processor :> Component;
    class Comment :> Base::Anything; 
    
    assoc implements :> Links::Link {
        end feature 'from': Component [1..*] redefines source;
        end feature 'to':   Function [1..*] redefines target;
    }
    
    assoc implementedBy :> Links::Link {
        end feature 'from': Function [1..*] redefines source;
        end feature 'to':   Component [1..*] redefines target;
    }
    
    assoc executes :> Links::Link {
        end feature 'from': Processor [1..*] redefines source;
        end feature 'to':   Software [1..*] redefines target;
    } 
    
    assoc executedBy :> Links::Link {
        end feature 'from': Component [1..*] redefines source;
        end feature 'to':   Function [1..*] redefines target;
    }
}

