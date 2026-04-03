standard library package BaseFunctions {
    private import Base::*;
    private import ScalarValues::*;
    // private import Objects::Object;
    // private import Objects::Object;

    abstract function '=='
        { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1]; }
    function '!=' { in x: Anything[0..1]; in y: Anything[0..1]; return : Boolean[1]; }
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
    abstract function 'all' { return : Anything[0..*]; } /* fixme: returns Object */

    abstract function 'istype'{
        in seq: Anything[0..*];
        /*abstract*/ in feature 'type': Anything;
        return : Boolean[1];
    }
    abstract function 'hastype'{
        in seq: Anything[0..*];
        /*abstract*/ in feature 'type': Anything;
        return : Boolean;
    }
    abstract function '@'{
        in seq: Anything[0..*];
        /*abstract*/ in feature 'type': Anything;
        return : Boolean[1];
    }
    // fixme: Metaobject type
    abstract function '@@'{
        in seq: Anything[0..*];
        /*abstract*/ in feature 'type': Anything;
        return : Boolean[1];
    }
    abstract function 'as'{
        in seq: Anything[0..*] ordered nonunique;
        return : Anything[0..*] ordered nonunique;
    }
    abstract function 'meta'{
        in seq: Anything[0..*] ordered nonunique;
        return : Anything[0..*] ordered nonunique;
    }
}

standard library package ControlFunctions
{
    private import Base::Anything;
    private import ScalarValues::Boolean;
    private import ScalarValues::ScalarValue;
    private import ScalarFunctions::min;
    private import ScalarFunctions::max;

    // standard declares if as abstract without any instances
    // + has arity 0..2
    // + branches are defined as function types (for lazyness?)

    abstract function 'if' {
        in test: Boolean[1];
        in thenValue[1] : Anything;
        in elseValue[1] : Anything;
        return : Anything[1];
    }

    // FIXME: doesn't compile
    //abstract function '.' {
    //    in feature source : Anything[0..*] nonunique {
    //        abstract feature target : Anything[0..*] nonunique;
    //    }
    //    private feature chain chains source.target;
    //    chain
    //}

    abstract function '??' {
        in firstValue: Anything[0..*] ordered nonunique;
        in expr secondValue[0..1] { return : Anything[0..*] ordered nonunique; }
        return : Anything[0..*] ordered nonunique;
    }

    function 'and' {
        in firstValue: Boolean[1];
        in expr secondValue[0..1] { return : Boolean[1]; }
        return : Boolean[1];
    }
    function 'or'{
        in firstValue: Boolean[1];
        in expr secondValue[0..1] { return : Boolean[1]; }
        return : Boolean[1];
    }
    function 'implies'{
        in firstValue: Boolean[1];
        in expr secondValue[0..1] { return : Boolean[1]; }
        return : Boolean[1];
    }
    abstract function collect {
        in collection: Anything[0..*] ordered nonunique;
        in expr mapper[0..*] {
            in argument: Anything[1];
            return : Anything[0..*] ordered nonunique;
        }
        return : Anything[0..*] ordered nonunique;
    }
    abstract function select {
        in collection: Anything[0..*] ordered nonunique;
        in expr selector[0..*] {
            in argument: Anything[1];
            return : Boolean[1];
        }
        return : Anything[0..*] ordered nonunique;
    }
    function selectOne {
        in collection: Anything[0..*] ordered nonunique;
        in expr selector1[0..*] {
            in argument: Anything[1];
            return : Boolean[1];
        }
        // TODO: Doesn't parse yet
        return : Anything[0..1] ; //= collection->select {in x; selector1(x)}[1];
    }
    abstract function reject{
        in collection: Anything[0..*] ordered nonunique;
        in expr rejector[0..*] {
            in argument: Anything[1];
            return : Boolean[1];
        }
        return : Anything[0..*] ordered nonunique;
    }
    abstract function reduce {
        in collection: Anything[0..*] ordered nonunique;
        in expr reducer[0..*] {
            in firstArg: Anything[1];
            in secondArg: Anything[1];
            return : Anything[1];
        }
        return : Anything[0..*] ordered nonunique;
    }

    abstract function forAll {
        in collection: Anything[0..*] ordered nonunique;
        in expr test[0..*] {
            in argument: Anything[1];
            return : Boolean[1];
        }
        return : Boolean[1];
    }
    abstract function exists {
        in collection: Anything[0..*] ordered nonunique;
        in expr test[0..*] {
            in argument: Anything[1];
            return : Boolean[1];
        }
        return : Boolean[1];
    }
    function allTrue {
        in collection: Boolean[0..*];
        return : Boolean[1] = collection->forAll {in x; x};
    }
    function anyTrue {
        in collection: Boolean[0..*];
        return : Boolean[1] = collection->exists {in x; x};
    }
    function minimize {
        in collection: ScalarValue[1..*];
        in expr fn[0..*] {
            in argument: ScalarValue[1];
            return : ScalarValue[1];
        }
        // TODO Doesn't parse
        return : ScalarValue[1]; // = collection->collect {in x; fn(x)}->reduce min;
    }
    function maximize {
        in collection: ScalarValue[1..*];
        in expr fn[0..*] {
            in argument: ScalarValue[1];
            return : ScalarValue[1];
        }
        // TODO Doesn't parse
        return : ScalarValue[1]; // = collection->collect {in x; fn(x)}->reduce max;
    }
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

    // SysMD extension: min/max are variadic
    abstract function max
        { in xs: DataValue[1..*]; return : DataValue[1]; }
    abstract function min
        { in xs: DataValue[1..*]; return : DataValue[1]; }
    abstract function '..'
        { in lower: DataValue[1]; in upper: DataValue[1]; return : DataValue[0..*] ordered; }

    abstract function 'if' specializes ControlFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : DataValue; in elseValue[1] : DataValue; return : DataValue[1]; }
}

standard library package ScalarFunctions {
    private import ScalarValues::*;
    private import Base::*;

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
        { in xs: ScalarValue[1..*]; return : ScalarValue[1]; }
    abstract function min specializes DataFunctions::min
        { in xs: ScalarValue[1..*]; return : ScalarValue[1]; }

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
        { in xs: NumericalValue[1..*]; return : NumericalValue[1]; }
    abstract function min specializes ScalarFunctions::min
        { in xs: NumericalValue[1..*]; return : NumericalValue[1]; }
    abstract function sum
        { in collection: NumericalValue[0..*]; return : NumericalValue[1]; }
    abstract function product
        { in collection: NumericalValue[0..*]; return : NumericalValue[1]; }

    abstract function 'if' specializes ScalarFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : NumericalValue; in elseValue[1] : NumericalValue; return : NumericalValue[1]; }

    // SysMD extension
    function 'sqr' { in x : NumericalValue[1]; return : NumericalValue[1]; }
}

standard library package ComplexFunctions {
    private import ScalarValues::*;

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
        { in test: Boolean[1]; in thenValue : Complex; in elseValue : Complex; return : Complex[1]; }

    feature i: Complex[1] = rect(0.0, 1.0);

    // SysMD extension
    function sqr :> NumericalFunctions::sqr { in x : Complex[1]; return : Complex[1]; }
}

standard library package RealFunctions {
    private import ScalarValues::*;
    private import Base::*;

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
        { in xs: Real[1..*]; return : Real[1]; }
    function min specializes NumericalFunctions::min
        { in xs: Real[1..*]; return : Real[1]; }
    function '==' specializes ComplexFunctions::'=='
        { in x: Real[0..1]; in y: Real[0..1]; return : Boolean[1]; }
    function sqrt
        { in x: Real[1]; return : Real[1]; }
    function floor
        { in x: Real[1]; return : Integer[1]; }
    function ceil // missing from standard?
        { in x : Real[1]; return : Integer[1]; }
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

    // SysMD extensions
    function allOf { in x : Real[1]; return : Real[1]; }
    function anyOf { in x : Real[1]; return : Real[1]; }
    function sum_i { in over : Integer[2]; in summand : Real[1]; return : Real[1]; }
    function sqr :> ComplexFunctions::sqr { in x : Real[1]; return : Real[1]; }
    function power2 { in x : Real[1]; return : Real[1]; }
    function intersect { in a : Real[0]; in b : Real[1]; return : Real[1]; }
    function cityBlockDistance { in xs : Real[2]; return : Real[1]; }
    function quantityOfVectorAtPosition { in vec : Real[1]; in ix : Integer[1]; return : Real[1]; }

    function stepInterpolation { in input : Real[1]; in points : Real[4..*]; return : Real[1]; }
    function oneOf { in x : Real[1]; return : Real[1]; }
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
        { in xs: Rational[1..*]; return : Rational[1]; }
    function min specializes RealFunctions::min
        { in xs: Rational[1..*]; return : Rational[1]; }
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

    function 'if' specializes RealFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Rational; in elseValue[1] : Rational; return : Rational[1]; }

    // SysMD extensions
    function allOf :> RealFunctions::allOf { in x : Rational[1]; return : Rational[1]; }
    function anyOf :> RealFunctions::anyOf { in x : Rational[1]; return : Rational[1]; }
    function sum_i :> RealFunctions::sum_i { in over : Integer[2]; in summand : Rational[1]; return : Rational[1]; }
    function intersect :> RealFunctions::intersect { in a : Rational[0]; in b : Rational[1]; return : Rational[1]; }
    function cityBlockDistance :> RealFunctions::cityBlockDistance { in xs : Rational[2]; return : Rational[1]; }
    function quantityOfVectorAtPosition :> RealFunctions::quantityOfVectorAtPosition
        { in vec : Rational[1]; in ix : Integer[1]; return : Rational[1]; }

    function sqr :> RealFunctions::sqr { in x : Rational[1]; return : Rational[1]; }
    function oneOf :> RealFunctions::oneOf { in x : Rational[1]; return : Rational[1]; }
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
        { in xs: Integer[1..*]; return : Integer[1]; }
    function min specializes RationalFunctions::min
        { in xs: Integer[1..*]; return : Integer[1]; }
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

    // SysMD extension
    function allOf     :> RationalFunctions::allOf { in x : Integer[1]; return : Integer[1]; }
    function anyOf     :> RationalFunctions::anyOf { in x : Integer[1]; return : Integer[1]; }
    function sum_i     :> RationalFunctions::sum_i { in over : Integer[2]; in summand : Integer[1]; return : Integer[1]; }
    function intersect :> RationalFunctions::intersect { in a : Integer[0]; in b : Integer[1]; return : Integer[1]; }
    function cityBlockDistance :> RationalFunctions::cityBlockDistance { in xs : Integer[2]; return : Natural[1]; }
    function quantityOfVectorAtPosition :> RationalFunctions::quantityOfVectorAtPosition
        { in vec : Integer[1]; in ix : Integer[1]; return : Integer[1]; }

    function sqr :> RationalFunctions::sqr { in x : Integer[1]; return : Integer[1]; }
    function oneOf :> RationalFunctions::oneOf { in x : Integer[1]; return : Integer[1]; }
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
        { in xs: Natural[1..*]; return : Natural[1]; }
    function min specializes IntegerFunctions::min
        { in xs: Natural[1..*]; return : Natural[1]; }
    function '==' specializes IntegerFunctions::'=='
        { in x: Natural[0..1]; in y: Natural[0..1]; return : Boolean[1]; }
    function ToString specializes IntegerFunctions::ToString
        { in x: Natural[1]; return : String[1]; }
    function ToNatural
        { in x: String[1]; return : Natural[1]; }

    abstract function 'if' specializes IntegerFunctions::'if' // non-standard
        { in test: Boolean[1]; in thenValue[1] : Natural; in elseValue[1] : Natural; return : Natural[1]; }

    // SysMD extension
    function allOf :> IntegerFunctions::allOf { in x : Natural[1]; return : Natural[1]; }
    function anyOf :> IntegerFunctions::anyOf { in x : Natural[1]; return : Natural[1]; }
    function sum_i :> IntegerFunctions::sum_i { in over : Natural[2]; in summand : Natural[1]; return : Natural[1]; }
    function intersect :> IntegerFunctions::intersect { in a : Natural[0]; in b : Natural[1]; return : Natural[1]; }
    function quantityOfVectorAtPosition :> IntegerFunctions::quantityOfVectorAtPosition
        { in vec : Natural[1]; in ix : Integer[1]; return : Natural[1]; }

    function sqr :> IntegerFunctions::sqr { in x : Natural[1]; return : Natural[1]; }
    function power2 :> RealFunctions::power2 { in x : Natural[1]; return : Natural[1]; }
    function oneOf :> IntegerFunctions::oneOf { in x : Natural[1]; return : Natural[1]; }
}

standard library package TrigFunctions
{
    private import ScalarValues::Real;

    feature pi : Real;
    // TODO: "No AST for result expression found"
    //inv piPrecision { RealFunctions::round(pi * 1E20) == 314159265358979323846.0 }

    function deg {
        in theta_rad : Real[1];
        return : Real[1] = theta_rad * 180.0 / pi;
    }
    function rad {
        in theta_deg : Real;
        return : Real[1] = theta_deg * pi / 180.0;
    }
    datatype UnitBoundedReal :> Real {
        // TODO: "'that' could be resolved, but is of wrong type"
        // inv unitBound { -1.0 <= that & that <= 1.0 }
    }
    function sin {
        in theta : Real[1];
        return : UnitBoundedReal[1];
    }
    function cos {
        in theta : Real[1];
        return : UnitBoundedReal[1];
    }
    function tan {
        in theta : Real[1];
        return : Real = sin(theta) / cos(theta);
    }
    function cot {
        in theta : Real;
        return : Real = cos(theta) / sin(theta);
    }
    function arcsin {
        in x : UnitBoundedReal[1];
        return : Real[1];
    }
    function arccos {
        in x : UnitBoundedReal[1];
        return : Real[1];
    }
    function arctan {
        in x : Real[1];
        return : Real[1];
    }
}

library package SysMD
{
    private import Base::Anything;
    private import ScalarValues::*;

    alias ITE for ControlFunctions::'if';
    public import RealFunctions::allOf;
    public import RealFunctions::anyOf;
    public import RealFunctions::oneOf;
    public import RealFunctions::sum_i;
    public import RealFunctions::sum;
    // fixme: type?
    function characterizedResult { in : Anything[0..*]; return : Anything[0..*]; }
    function ln { in x : Real[1]; return : Real[1]; }
    function exp { in x : Real[1]; return : Real[1]; }
    public import RealFunctions::sqr;
    public import RealFunctions::sqrt;
    public import RealFunctions::ceil;
    public import RealFunctions::floor;
    public import RealFunctions::power2;
    alias pow2 for power2;
    alias powerb for RealFunctions::'^';
    alias power for RealFunctions::'^';
    alias powb for RealFunctions::'^';
    alias pow for RealFunctions::'^';
    public import TrigFunctions::sin;
    public import TrigFunctions::cos;
    function toReal { in int : Boolean[1]; return : Real[1]; } // not to be confused with 'ToReal'...
    // fixme: move these to quantities?
    function DateTime { in date : String[1]; return : Real[1]; }
    function Date { in date: String[1]; return : Real[1]; }
    function Month { in date: String[1]; return : Real[1]; }
    function Year { in date: String[1]; return : Real[1]; }
    public import RealFunctions::max;
    public import RealFunctions::min;
    public import RealFunctions::abs;
    public import RealFunctions::intersect;
    // fixme: these should be polymorphic...
    function bySpecializations { in attr : Anything[1]; return : ScalarValue[1]; }
    function byParts { in attr : Anything[1]; return : ScalarValue[1]; }
    function byImplements { in attr : Anything[1]; return : ScalarValue[1]; }
    function linearInterpolation { in x : Real[1]; in points : Real[2..*]; return : Real[1];  }
    function stepInterpolation { in x : Real[1]; in points : Real[2..*]; return : Real[1];  }
    function ToReal { in int : Integer[1]; return : Real[1]; }
    function ToInteger { in int : Real[1]; return : Integer[1]; }
     // fixme: vector type?
    function norm { in vec : Real[1]; return : Real[1]; }
    function size { in vec : Real[1]; return : Real[1]; }
    function angle { in vs : Real[2]; return : Real[1]; }
    public import RealFunctions::cityBlockDistance;

    // FIXME: type of unresolved names?
    function sumOverParts { in attr : Anything[1]; return : Real[1]; }
    function productOverParts { in attr : Anything[1]; return : Real[1]; }
    function sumOverSubclasses { in attr : Anything[1]; return : Real[1]; }
    function productOverSubclasses { in attr : Anything[1]; return : Real[1]; }
    function sumOverPartsNotTransitive { in attr : Anything[1]; return : Real[1]; }
    function productOverPartsNotTransitive { in attr : Anything[1]; return : Real[1]; }
    function sumOverSubclassesNotTransitive { in attr : Anything[1]; return : Real[1]; }
    function productOverSubclassesNotTransitive { in attr : Anything[1]; return : Real[1]; }

    function owns { in pkg : Anything[1]; in item : Anything[1]; return : Boolean[1]; }

    // legacy SysMD extension. May require special context or opt-in in future.
    function ' ' { in x: Anything[1..*]; in y: Anything[1..*]; return : Anything[0..*]; }

    function 'cross' { in x : Real[1]; in y : Real[1]; return : Real[1]; }
    function 'dot'   { in x : Real[1]; in y : Real[1]; return : Real[1]; }
}
