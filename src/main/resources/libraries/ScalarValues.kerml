standard library package ScalarValues {
    datatype ScalarValue :> Base::DataValue;
	datatype String :> ScalarValue;
	datatype Boolean :> ScalarValue;
    datatype NumericalValue :> ScalarValue;
    datatype Number :> NumericalValue;
	datatype Complex :> Number;
	datatype Real :> Complex;
	datatype Rational :> Real;
	datatype Integer :> Rational;
	datatype Natural :> Integer;
    datatype Positive :> Natural;
}		
