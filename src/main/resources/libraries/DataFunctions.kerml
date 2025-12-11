standard library package BaseFunctions {
    private import Base::*;
    private import ScalarValues::*;
    // private import Objects::Object;
    // private import Objects::Object;

    abstract function '=='
        { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1]; }
    // TODO: "[0], line null: In expression 'not (x == y)' of null: null"
    // function '!='
    //    { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1] = not (x == y); }
    //abstract function '==='
    //    { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1]; }
    //function '!=='
    //    { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1] = not (x === y); }
    function ToString
        { in x: Anything[0..1]; return : String; }
    function '['
        { in x: Anything[0..*] nonunique; in y: Anything[0..*] nonunique; return : Anything[0..*] nonunique; }
    function '#'
        { in seq: Anything[0..*] ordered nonunique; in index: Positive[1..*] ordered nonunique; return : Anything[0..1]; }
    function ','
        { in seq1: Anything[0..*] ordered nonunique; in seq2: Anything[0..*] ordered nonunique; return : Anything[0..*] ordered nonunique; }
    //abstract function 'all'
    //    { return : Object[0..*]; }
}

standard library package ControlFunctions
{
    private import Base::Anything;
    private import ScalarValues::Boolean;

    // standard declares if as abstract without any instances
    // + has arity 0..2
    // + branches are defined as function types (for lazyness?)

    abstract function 'if' {
        in test: Boolean[1];
        in thenValue[1] : Anything;
        in elseValue[1] : Anything;
        return : Anything[1];
    }

    // TODO: '.', and, or, implies, ...
}

standard library package DataFunctions {
    private import Base::DataValue;
    private import ScalarValues::Boolean;

    abstract function '==' specializes BaseFunctions::'=='
        { in x: DataValue[0..1]; in y: DataValue[0..1]; return : Boolean[1]; }

    //abstract function '===' specializes BaseFunctions::'==='
    //    { in x: DataValue[0..1]; in y: DataValue[0..1]; return : Boolean[1]; }

    abstract function '+'
        { in x: DataValue[1]; in y: DataValue[0..1]; return : DataValue[1]; }
    abstract function '-'
        { in x: DataValue[1]; in y: DataValue[0..1]; return : DataValue[1]; }
    abstract function '*'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function '/'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }

    abstract function '**'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function '^'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }

    abstract function '%'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }

    abstract function 'not'
        { in x: DataValue[1]; return : DataValue[1]; }
    abstract function 'xor'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function '~'
        { in x: DataValue[1]; return : DataValue[1]; }
    abstract function '|'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function '&'
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }

    abstract function '<'
        { in x: DataValue[1]; in y: DataValue[1]; return : Boolean[1]; }
    abstract function '>'
        { in x: DataValue[1]; in y: DataValue[1]; return : Boolean[1]; }
    abstract function '<='
        { in x: DataValue[1]; in y: DataValue[1]; return : Boolean[1]; }
    abstract function '>='
        { in x: DataValue[1]; in y: DataValue[1]; return : Boolean[1]; }

    abstract function max
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function min
        { in x: DataValue[1]; in y: DataValue[1]; return : DataValue[1]; }
    abstract function '..'
        { in lower: DataValue[1]; in upper: DataValue[1]; return : DataValue[0..*] ordered; }

    abstract function 'if' specializes ControlFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : DataValue; in elseValue[1] : DataValue; return : DataValue[1]; }
}

standard library package ScalarFunctions {
    private import ScalarValues::*;

    abstract function '+' specializes DataFunctions::'+'
        { in x: ScalarValue[1]; in y: ScalarValue[0..1]; return : ScalarValue[1]; }

    abstract function '-' specializes DataFunctions::'-'
        { in x: ScalarValue[1]; in y: ScalarValue[0..1]; return : ScalarValue[1]; }

    abstract function '*' specializes DataFunctions::'*'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '/' specializes DataFunctions::'/'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '**' specializes DataFunctions::'**'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '^' specializes DataFunctions::'^'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '%' specializes DataFunctions::'%'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function 'not' specializes DataFunctions::'not'
        { in x: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function 'xor' specializes DataFunctions::'xor'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '~' specializes DataFunctions::'~'
        { in x: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '|' specializes DataFunctions::'|'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '&' specializes DataFunctions::'&'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '<' specializes DataFunctions::'<'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : Boolean[1]; }

    abstract function '>' specializes DataFunctions::'>'
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : Boolean[1]; }

    abstract function '<=' specializes DataFunctions::'<='
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : Boolean[1]; }

    abstract function '>=' specializes DataFunctions::'>='
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : Boolean[1]; }

    abstract function max specializes DataFunctions::max
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function min specializes DataFunctions::min
        { in x: ScalarValue[1]; in y: ScalarValue[1]; return : ScalarValue[1]; }

    abstract function '..' specializes DataFunctions::'..'
        { in lower: ScalarValue[1]; in upper: ScalarValue[1]; return : ScalarValue[0..*]; }

    abstract function 'if' specializes DataFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : ScalarValue; in elseValue[1] : ScalarValue; return : ScalarValue[1]; }
}

standard library package BooleanFunctions {
    private import ScalarValues::*;

    function 'not' specializes ScalarFunctions::'not'
        { in x: Boolean[1]; return : Boolean[1]; }
    function 'xor' specializes ScalarFunctions::'xor'
        { in x: Boolean[1]; in y: Boolean[1]; return : Boolean[1]; }
    function '|' specializes ScalarFunctions::'|'
        { in x: Boolean[1]; in y: Boolean[1]; return : Boolean[1]; }
    function '&' specializes ScalarFunctions::'&'
        { in x: Boolean[1]; in y: Boolean[1]; return : Boolean[1]; }
    function '==' specializes DataFunctions::'=='
        { in x: Boolean[0..1]; in y: Boolean[0..1]; return : Boolean[1]; }
    function ToString specializes BaseFunctions::ToString
        { in x: Boolean[1]; return : String[1]; }
    function ToBoolean
        { in x: String[1]; return : Boolean[1]; }

    abstract function 'if' specializes ScalarFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Boolean; in elseValue[1] : Boolean; return : Boolean[1]; }
}

standard library package StringFunctions {
    private import ScalarValues::*;

    function '+' specializes ScalarFunctions::'+'
        { in x: String[1]; in y:String[1]; return : String[1]; }
    function Length
        { in x: String[1]; return : Natural[1]; }
    function Substring
        { in x: String[1]; in lower: Integer[1]; in upper: Integer[1];
        return : String[1]; }
    function '<' specializes ScalarFunctions::'<'
        { in x: String[1]; in y: String[1]; return : Boolean[1]; }
    function '>' specializes ScalarFunctions::'>'
        { in x: String[1]; in y: String[1]; return : Boolean[1]; }
    function '<=' specializes ScalarFunctions::'<='
        { in x: String[1]; in y: String[1]; return : Boolean[1]; }
    function '>=' specializes ScalarFunctions::'>='
        { in x: String[1]; in y: String[1]; return : Boolean[1]; }
    function '==' specializes DataFunctions::'=='
        { in x: String[0..1]; in y: String[0..1]; return : Boolean[1]; }
    function ToString specializes BaseFunctions::ToString
        { in x: String[1]; }

    abstract function 'if' specializes ScalarFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : String; in elseValue[1] : String; return : String[1]; }
}

standard library package NumericalFunctions {
    private import ScalarValues::*;

    abstract function isZero
        { in x: NumericalValue[1]; return : Boolean; }
    abstract function isUnit
        { in x : NumericalValue[1]; return : Boolean; }
    abstract function abs
        { in x: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '+' specializes ScalarFunctions::'+'
        { in x: NumericalValue[1]; in y: NumericalValue[0..1]; return : NumericalValue[1]; }
    abstract function '-' specializes ScalarFunctions::'-'
        { in x: NumericalValue[1]; in y: NumericalValue[0..1]; return : NumericalValue[1]; }
    abstract function '*' specializes ScalarFunctions::'*'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '/' specializes ScalarFunctions::'/'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '**' specializes ScalarFunctions::'**'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '^' specializes ScalarFunctions::'^'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '%' specializes ScalarFunctions::'%'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function '<' specializes ScalarFunctions::'<'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : Boolean[1]; }
    abstract function '>' specializes ScalarFunctions::'>'
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : Boolean[1]; }
    abstract function '<=' specializes ScalarFunctions::'<='
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : Boolean[1]; }
    abstract function '>=' specializes ScalarFunctions::'>='
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : Boolean[1]; }
    abstract function max specializes ScalarFunctions::max
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function min specializes ScalarFunctions::min
        { in x: NumericalValue[1]; in y: NumericalValue[1]; return : NumericalValue[1]; }
    abstract function sum
        { in collection: ScalarValue[0..*]; return : ScalarValue[1]; }
    abstract function product
        { in collection: ScalarValue[0..*]; return : ScalarValue[1]; }

    abstract function 'if' specializes ScalarFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : NumericalValue; in elseValue[1] : NumericalValue; return : NumericalValue[1]; }
}

standard library package ComplexFunctions {
    private import ScalarValues::*;

    feature i: Complex[1] = rect(0.0, 1.0);
    function rect
        { in re: Real[1]; in im: Real[1]; return : Complex[1]; }
    function polar
        { in abs: Real[1]; in arg: Real[1]; return : Complex[1]; }
    function re
        { in x: Complex[1]; return : Real[1]; }
    function im
        { in x: Complex[1]; return : Real[1]; }
    function isZero specializes NumericalFunctions::isZero
        { in x : Complex[1]; return : Boolean[1]; }
    function isUnit specializes NumericalFunctions::isUnit
        { in x : Complex[1]; return : Boolean[1]; }
    function abs specializes NumericalFunctions::abs
        { in x: Complex[1]; return : Real[1]; }
    function arg
        { in x: Complex[1]; return : Real[1]; }
    function '+' specializes NumericalFunctions::'+'
        { in x: Complex[1]; in y: Complex[0..1]; return : Complex[1]; }
    function '-' specializes NumericalFunctions::'-'
        { in x: Complex[1]; in y: Complex[0..1]; return : Complex[1]; }
    function '*' specializes NumericalFunctions::'*'
        { in x: Complex[1]; in y: Complex[1]; return : Complex[1]; }
    function '/' specializes NumericalFunctions::'/'
        { in x: Complex[1]; in y: Complex[1]; return : Complex[1]; }
    function '**' specializes NumericalFunctions::'**'
        { in x: Complex[1]; in y: Complex[1]; return : Complex[1]; }
    function '^' specializes NumericalFunctions::'^'
        { in x: Complex[1]; in y: Complex[1]; return : Complex[1]; }
    function '==' specializes DataFunctions::'=='
        { in x: Complex[0..1]; in y: Complex[0..1]; return : Boolean[1]; }
    function ToString specializes BaseFunctions::ToString
        { in x: Complex[1]; return : String[1]; }
    function ToComplex
        { in x: String[1]; return : Complex[1]; }
    function sum specializes NumericalFunctions::sum
        { in collection: Complex[0..*]; return : Complex[1]; }
    function product specializes NumericalFunctions::product
        { in collection: Complex[0..*]; return : Complex[1]; }

    abstract function 'if' specializes NumericalFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Complex; in elseValue[1] : Complex; return : Complex[1]; }
}

standard library package RealFunctions {
    private import ScalarValues::*;

    function re :> ComplexFunctions::re
        { in x: Real[1]; return : Real[1] = x; }
    function im :> ComplexFunctions::im
        { in x: Real[1]; return : Real[1] = 0.0; }
    function abs specializes ComplexFunctions::abs
        { in x: Real[1]; return : Real[1]; }
    function arg specializes ComplexFunctions::arg
        { in x: Real[1]; return : Real[1] = 0.0; }
    function '+' specializes ComplexFunctions::'+'
        { in x: Real[1]; in y: Real[0..1]; return : Real[1]; }
    function '-' specializes ComplexFunctions::'-'
        { in x: Real[1]; in y: Real[0..1]; return : Real[1]; }
    function '*' specializes ComplexFunctions::'*'
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function '/' specializes ComplexFunctions::'/'
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function '**' specializes ComplexFunctions::'**'
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function '^' specializes ComplexFunctions::'^'
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function '<' specializes NumericalFunctions::'<'
        { in x: Real[1]; in y: Real[1]; return : Boolean[1]; }
    function '>' specializes NumericalFunctions::'>'
        { in x: Real[1]; in y: Real[1]; return : Boolean[1]; }
    function '<=' specializes NumericalFunctions::'<='
        { in x: Real[1]; in y: Real[1]; return : Boolean[1]; }
    function '>=' specializes NumericalFunctions::'>='
        { in x: Real[1]; in y: Real[1]; return : Boolean[1]; }
    function max specializes NumericalFunctions::max
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function min specializes NumericalFunctions::min
        { in x: Real[1]; in y: Real[1]; return : Real[1]; }
    function '==' specializes ComplexFunctions::'=='
        { in x: Real[0..1]; in y: Real[0..1]; return : Boolean[1]; }
    function sqrt
        { in x: Real[1]; return : Real[1]; }
    function floor
        { in x: Real[1]; return : Integer[1]; }
    function round
        { in x: Real[1]; return : Integer[1]; }
    function ToString specializes ComplexFunctions::ToString
        { in x: Real[1]; return : String[1]; }
    function ToInteger
        { in x: Real[1]; return : Integer[1]; }
    function ToRational
        { in x: Real[1]; return : Rational[1]; }
    function ToReal
        { in x: String[1]; return : Real[1]; }
    function sum specializes ComplexFunctions::sum
        { in collection: Real[0..*]; return : Real; }
    function product specializes ComplexFunctions::product
        { in collection: Real[0..*]; return : Real; }

    abstract function 'if' specializes ComplexFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Real; in elseValue[1] : Real; return : Real[1]; }
}

standard library package RationalFunctions {
    private import ScalarValues::*;

    function rat
        { in numer: Integer[1]; in denum: Integer[1]; return : Rational[1]; }
    function numer
        { in rat: Rational[1]; return : Integer[1]; }
    function denom
        { in rat: Rational[1]; return : Integer[1]; }
    function abs specializes RealFunctions::abs
        { in x: Rational[1]; return : Rational[1]; }
    function '+' specializes RealFunctions::'+'
        { in x: Rational[1]; in y: Rational[0..1]; return : Rational[1]; }
    function '-' specializes RealFunctions::'-'
        { in x: Rational[1]; in y: Rational[0..1]; return : Rational[1]; }
    function '*' specializes RealFunctions::'*'
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function '/' specializes RealFunctions::'/'
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function '**' specializes RealFunctions::'**'
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function '^' specializes RealFunctions::'^'
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function '<' specializes RealFunctions::'<'
        { in x: Rational[1]; in y: Rational[1]; return : Boolean[1]; }
    function '>' specializes RealFunctions::'>'
        { in x: Rational[1]; in y: Rational[1]; return : Boolean[1]; }
    function '<=' specializes RealFunctions::'<='
        { in x: Rational[1]; in y: Rational[1]; return : Boolean[1]; }
    function '>=' specializes RealFunctions::'>='
        { in x: Rational[1]; in y: Rational[1]; return : Boolean[1]; }
    function max specializes RealFunctions::max
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function min specializes RealFunctions::min
        { in x: Rational[1]; in y: Rational[1]; return : Rational[1]; }
    function '==' specializes RealFunctions::'=='
        { in x: Rational[0..1]; in y: Rational[0..1]; return : Boolean[1]; }
    function gcd
        { in x: Rational[1]; in y: Rational[1]; return : Integer[1]; }
    function floor specializes RealFunctions::floor
        { in x: Rational[1]; return : Integer[1]; }
    function round specializes RealFunctions::round
        { in x: Rational[1]; return : Integer[1]; }
    function ToString specializes RealFunctions::ToString
        { in x: Rational[1]; return : String[1]; }
    function ToInteger
        { in x: Rational[1]; return : Integer[1]; }
    function ToRational
        { in x: String[1]; return : Rational[1]; }
    function sum specializes RealFunctions::sum
        { in collection: Rational[0..*]; return : Rational[1]; }
    function product specializes RealFunctions::product
        { in collection: Rational[0..*]; return : Rational[1]; }

    abstract function 'if' specializes RealFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Rational; in elseValue[1] : Rational; return : Rational[1]; }
}

standard library package IntegerFunctions {
    private import ScalarValues::*;

    function abs specializes RationalFunctions::abs
        { in x: Integer[1]; return : Natural[1]; }
    function '+' specializes RationalFunctions::'+'
        { in x: Integer[1]; in y: Integer[0..1]; return : Integer[1]; }
    function '-' specializes RationalFunctions::'-'
        { in x: Integer[1]; in y: Integer[0..1]; return : Integer[1]; }
    function '*' specializes RationalFunctions::'*'
        { in x: Integer[1]; in y: Integer[1]; return : Integer[1]; }
    function '/' specializes RationalFunctions::'/'
        { in x: Integer[1]; in y: Integer[1]; return : Rational[1]; }
    function '**' specializes RationalFunctions::'**'
        { in x: Integer[1]; in y: Natural[1]; return : Integer[1]; }
    function '^' specializes RationalFunctions::'^'
        { in x: Integer[1]; in y: Natural[1]; return : Integer[1]; }
    function '%' specializes NumericalFunctions::'%'
        { in x: Integer[1]; in y: Integer[1]; return : Integer[1]; }
    function '<' specializes RationalFunctions::'<'
        { in x: Integer[1]; in y: Integer[1]; return : Boolean[1]; }
    function '>' specializes RationalFunctions::'>'
        { in x: Integer[1]; in y: Integer[1]; return : Boolean[1]; }
    function '<=' specializes RationalFunctions::'<='
        { in x: Integer[1]; in y: Integer[1]; return : Boolean[1]; }
    function '>=' specializes RationalFunctions::'>='
        { in x: Integer[1]; in y: Integer[1]; return : Boolean[1]; }
    function max specializes RationalFunctions::max
        { in x: Integer[1]; in y: Integer[1]; return : Integer[1]; }
    function min specializes RationalFunctions::min
        { in x: Integer[1]; in y: Integer[1]; return : Integer[1]; }
    function '==' specializes DataFunctions::'=='
        { in x: Integer[0..1]; in y: Integer[0..1]; return : Boolean[1]; }
    function '..' specializes ScalarFunctions::'..'
        { in lower: Integer[1]; in upper: Integer[1]; return : Integer[0..*]; }
    function ToString specializes RationalFunctions::ToString
        { in x: Integer[1]; return : String[1]; }
    function ToNatural
        { in x: Integer[1]; return : Natural[1]; }
    function ToInteger
        { in x: String[1]; return : Integer[1]; }
    function sum specializes RationalFunctions::sum
        { in collection: Integer[0..*]; return : Integer[1]; }
    function product specializes RationalFunctions::product
        { in collection: Integer[0..*]; return : Integer[1]; }

    abstract function 'if' specializes RationalFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Integer; in elseValue[1] : Integer; return : Integer[1]; }
}

standard library package NaturalFunctions {
    private import ScalarValues::*;

    function '+' specializes IntegerFunctions::'+'
        { in x: Natural[1]; in y: Natural[0..1]; return : Natural[1]; }
    function '*' specializes IntegerFunctions::'*'
        { in x: Natural[1]; in y: Natural[1]; return : Natural[1]; }
    function '/' specializes IntegerFunctions::'/'
        { in x: Natural[1]; in y: Natural[1]; return : Natural[1]; }
    function '%' specializes IntegerFunctions::'%'
        { in x: Natural[1]; in y: Natural[1]; return : Natural[1]; }
    function '<' specializes IntegerFunctions::'<'
        { in x: Natural[1]; in y: Natural[1]; return : Boolean[1]; }
    function '>' specializes IntegerFunctions::'>'
        { in x: Natural[1]; in y: Natural[1]; return : Boolean[1]; }
    function '<=' specializes IntegerFunctions::'<='
        { in x: Natural[1]; in y: Natural[1]; return : Boolean[1]; }
    function '>=' specializes IntegerFunctions::'>='
        { in x: Natural[1]; in y: Natural[1]; return : Boolean[1]; }
    function max specializes IntegerFunctions::max
        { in x: Natural[1]; in y: Natural[1]; return : Natural[1]; }
    function min specializes IntegerFunctions::min
        { in x: Natural[1]; in y: Natural[1]; return : Natural[1]; }
    function '==' specializes IntegerFunctions::'=='
        { in x: Natural[0..1]; in y: Natural[0..1]; return : Boolean[1]; }
    function ToString specializes IntegerFunctions::ToString
        { in x: Natural[1]; return : String[1]; }
    function ToNatural
        { in x: String[1]; return : Natural[1]; }

    abstract function 'if' specializes IntegerFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Natural; in elseValue[1] : Natural; return : Natural[1]; }
}
