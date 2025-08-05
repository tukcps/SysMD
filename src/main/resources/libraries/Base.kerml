standard library package Base { 
    // abstract classifier Anything;
    abstract feature things: Base::Anything [1..*] nonunique; 
    abstract datatype DataValue specializes Base::Anything;
    feature dataValues: DataValue;
}

