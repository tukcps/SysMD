standard library package Quantities {
    datatype VectorQuantityValue :> Ranges::QuantityInRange{
        feature num: ScalarValues::Integer; //number of elements
    }
    datatype '3dVectorQuantityValue' :> VectorQuantityValue{
        :>> num = 3;
    }
    datatype ScalarQuantityValue :> VectorQuantityValue{
        :>> num = 1;
    }

}