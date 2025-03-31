standard library package Links {
    assoc Link specializes Base::Anything {
        end feature source: Base::Anything [1..*];
        end feature target: Base::Anything [1..*];
    }
    connector link : Link;

    assoc BinaryLink specializes Link {
        end feature source: Base::Anything [1];
        end feature target: Base::Anything [1];
    }
    connector binaryLinks : BinaryLink;
}
