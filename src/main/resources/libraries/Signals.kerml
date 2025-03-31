
library package Signals {
    assoc EffectChain {
        end feature source: ScalarValues::Real;
        end feature target: ScalarValues::Real;
        inv inoutIsEqual { source == target } 
    }
    assoc Signal {
        end feature source: ScalarValues::Real; 
        end feature source: ScalarValues::Real; 
    }
}
