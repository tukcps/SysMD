standard library package Constraints {
    class ConstraintDefinition :> ScalarValues::Boolean;
    class ConstraintUsage :> ScalarValues::Boolean; 
    class AssertConstraintUsage :> ConstraintUsage; 
    class ConstraintDefinition :> ScalarValues::Boolean; 
}