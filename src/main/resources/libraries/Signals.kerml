
library package Signals {
    assoc EffectChain {
        :>> source: ScalarValues::Real;
        :>> target: ScalarValues::Real;
        inv inoutIsEqual { source == target }
    }
    assoc Signal;
}
