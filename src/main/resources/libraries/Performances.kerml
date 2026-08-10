standard library package Performances {
    abstract behavior Performance specializes Occurrences::Occurrence disjoint from Objects::Object;
    abstract behavior Evaluation specializes Performance {
        feature result : Base::Anything[0..*] nonunique;
    }
}