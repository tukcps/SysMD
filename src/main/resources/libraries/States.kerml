standard library package States {
   class StateAction :> Actions::Action;  // The base type of all StateDefinitions
   feature stateActions: StateAction [0 .. *]; // The base feature for all StateUsages
}
