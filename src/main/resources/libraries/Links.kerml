standard library package Links {

    abstract assoc Link specializes Base::Anything {
        end feature source: Base::Anything;
        end feature target: Base::Anything;
    }

    abstract feature links : Link subsets Base::things;

    assoc all BinaryLink specializes Link {
        :>> source: Base::Anything [1];
        :>> target: Base::Anything [1];
    }

    abstract feature binaryLinks: BinaryLink[0..*] nonunique subsets links {
            doc
            /*
             * binaryLinks is a specialization of links restricted to type BinaryLink.
             */
        }
}