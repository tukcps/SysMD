standard library package Ranges {

    doc /*
    # Ranges library

    ## Concept
    The library Ranges introduces an abstract datatype _Range_.
    It can be used complementary to ScalarValues::Real or subtypes thereof,
    e.g., ScalarValues::Integer.
    The objective is to allow
    - users to specify constraints and semantics for the choice of a value,
    - tools for analysis to identify these specific constraints.

    The specific constraints for the choice of values are in particular
    - the upper and lower bounds (min, max) of a value.
    - semantics for satisfiability of the invariants that depend on the value.
      - _one_ - semantics means that all dependent invariants of a model shall be satisfied for at least one value.
      - _all_ - semantics means that all dependent invariants of a model must be satisfied for all values in the range.

    For example, consider the following model:


        feature x: ScalarValues::Real, Ranges::OneRealIn = f(something) {
            min :>> -3.141; // constant, may not change.
            max :>> 100.0   // constant, may not change.
        }
    */

    doc /*
    The above model shall
    - introduce an invariant ```inv min <= x <= max;```

    - give the complementary information that it is sufficient to ensure
      that there exists at least one value for ```x``` that satisfies all invariants.

    - tools may check that the Liskov Principle applies considering whether a feature subtypes ```One``` or ```All````.

    ## Implementation
    */

    // Abstract Range from which a Real is chosen
     datatype InRange {
        feature min: ScalarValues::Real; // May be redefined
        feature max: ScalarValues::Real; // May be redefined
        // inv thatIsInRange { (min <= that) and (that <= max) }
    }
    
    datatype AllInRange :> InRange; 
    datatype OneInRange :> InRange; 
    datatype AnyInRange :> InRange; 
    
    datatype IntegerInRange :> ScalarValues::Integer, InRange; 
    datatype RealInRange :> ScalarValues::Real, InRange; 
    datatype AllIntegerInRange :> ScalarValues::Integer, AllInRange; 
    datatype AllRealInRange :> ScalarValues::Real, AllInRange; 
    
    datatype QuantityInRange :> InRange {
        feature unit: ScalarValues::String;
    }    
    datatype AllQuantityOf :> QuantityInRange; 
}