standard library package VectorValues {
        private import ScalarValues::NumericalValue;
        private import ScalarValues::Real;
        private import Collections::Array;

        abstract datatype VectorValue;

        datatype NumericalVectorValue :> VectorValue, Array intersects VectorValue, Array;

        datatype CartesianVectorValue :> NumericalVectorValue{
                feature :>> elements : Real;
        }

        datatype ThreeVectorValue :> NumericalVectorValue {
                feature :>> dimension = 3;
        }

        datatype CartesianThreeVectorValue :> CartesianVectorValue, ThreeVectorValue
                intersects CartesianVectorValue, ThreeVectorValue;
}
