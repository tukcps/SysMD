---
title: BaseTypes
name:  BaseTypes
---
Defines the fundamental data types, units of measure (SI units), and value types used across the entire model. 
It ensures semantic consistency for all attributes like length, mass, or speed throughout the project.
```SysML::OpenBoardnet
package BaseTypes {  
    
    // An abstract 'Feature' that other features will specialize.
    abstract part def Feature;

    // An abstract 'Component' for software components.
    abstract part def Component;
    
    // An abstract 'Function' for system functions.
    abstract part def Function;

    // Precision Types - Centralized precision definitions for all layers
    package PrecisionTypes {
        abstract part def Precision {
            attribute size: StorageCapacityValue {:>> unit = "kB";}
            attribute name: String;
        }
        
        part def Float64 :> Precision {
            attribute :>> size = 8.0 [B];
            attribute :>> name = "Float64";
        }
        
        part def Float32 :> Precision {
            attribute :>> size = 4.0 [B];
            attribute :>> name = "Float32";
        }
        
        part def Float16 :> Precision {
            attribute :>> size = 2.0 [B];
            attribute :>> name = "Float16";
        }
        
        part def Int32 :> Precision {
            attribute :>> size = 4.0 [B];
            attribute :>> name = "Int32";
        }
        
        part def Int16 :> Precision {
            attribute :>> size = 2.0 [B];
            attribute :>> name = "Int16";
        }
        
        part def Int8 :> Precision {
            attribute :>> size = 1.0 [B];
            attribute :>> name = "Int8";
        }
        
        part def Boolean :> Precision {
            attribute :>> size = 1.0 [B];
            attribute :>> name = "Boolean";
        }
    }
}
```
