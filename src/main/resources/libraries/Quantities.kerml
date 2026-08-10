standard library package Quantities {
    datatype VectorQuantityValue :> Ranges::QuantityInRange;
    datatype '3dVectorQuantityValue' :> VectorQuantityValue;
    datatype ScalarQuantityValue :> VectorQuantityValue;
}