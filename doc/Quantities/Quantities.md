# Quantities

Quantity consist of a value, a unit and a unitSpec. The value is stored as an AADD, so that a range of possible 
values for the current value is stored with its influences. The unit is an Object of Type unit, which stores
the SIunitList and other important properties. This unit is always stored as an SI Unit. To memorize the 
original unit, the UnitStr is used. 

## Generate a Quantity
There are different possibilities to generate a Quantity.

- Constructor with value and UnitStr. If the unitStr is not set the Unit is empty and the Quantity can be
used for IDDs which are not allowed to use Units or for AADDs without Units. The resulting Quantity is 
always a Quantity in SI representation.

```
    Quantity(value: DD, unitString: String = "") 
```
- Nearly the same, the only difference is, that a Unit object can be used. But the resulting Quantity is still
converted to the SI unit.

```
    Quantity(value: DD, unitObject: Unit, unitSpec: String = "") 
```

## Available Functions for Quantities:

All functions for Quantities which can be called from outside this package have the property, that the 
returned Quantity is a new Object and does not include any Objects from the calling Quantity. This has
the advantage, that changes to Quantities are not possible and a Quantity always stays the same. If 
something needs to be changed, always a new Quantity object is returned with a cloned Unit and a cloned
Value.

