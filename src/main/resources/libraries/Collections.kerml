
standard library package Collections {

    private import Base::*;
    private import ScalarValues::*;
    private import SequenceFunctions::size;
    private import IntegerFunctions::*;
    private import ControlFunctions::*;

    abstract datatype Collection {
        feature elements[0..*] nonunique;
    }

    abstract datatype OrderedCollection :> Collection {
        feature elements[0..*] ordered nonunique :>> Collection::elements;
    }

    datatype Array :> OrderedCollection {
        feature dimensions: Positive[0..*] ordered nonunique;
        feature rank: Natural[1] = size(dimensions);
        // feature flattenedSize: Positive[1] = dimensions->reduce '*' ?? 1;
        // inv { flattenedSize == size(elements) }
    }
}
