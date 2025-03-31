standard library package Base { 
    // Base::Anything is built-in
    abstract feature things: Base::Anything [1..*] nonunique; 
    abstract datatype DataValue specializes Base::Anything;
    feature dataValues: DataValue;
}

