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

        feature x: ScalarValues::Real, Ranges::OneInRange = f(something) {
            range :>> [-3.141 .. 3.142];
        }

       It inherits the types Real, which specifies that the result is generally from the domain of the Reals.
       Furthermore, the type OnInRange that adds an invariant to the domain.
       It specifies that there must exist **at least one** element in the range that satisfies all constraints in
       a system.
       An example is the choice from a range of possible values, e.g., by parameterization.

       Note that another marker `AllInRange` specifies that **all* elements in the range must allow to
       satisfy all constraints in a system.
       An example is the specification of operation ranges - here, one usually wants that a system is operational
       for **all** values in a range.

       Finally, we distinguish ontologically between parameterization (open choices by an engineer)
       and external uncertainties, where a free choice is not meaningful (e.g. the value of $\pi$.)
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
    datatype InRange :> Base::Anything {
        feature range: ScalarValues::ScalarValue;
        // inv thatIsInRange { (that.min <= that.range.min) and (that.range.max <= that.max) }
        // Automatically added and globally solved by SysMD's compiler
    }
    
    datatype AllInRange :> InRange; 
    datatype OneInRange :> InRange; 
    datatype AnyInRange :> InRange; 
    
    datatype IntegerInRange :> ScalarValues::Integer, InRange; 
    datatype RealInRange :> ScalarValues::Real, InRange; 
    datatype AllIntegerInRange :> ScalarValues::Integer, AllInRange; 
    datatype AllRealInRange :> ScalarValues::Real, AllInRange; 

    datatype BooleanInSpec :> ScalarValues::Boolean {
        feature range: ScalarValues::Boolean; // Links with solver that handels unknown bools
    }
    datatype QuantityInRange :> RealInRange { feature unit: ScalarValues::String; }
    datatype AllQuantityOf :> QuantityInRange; 
}