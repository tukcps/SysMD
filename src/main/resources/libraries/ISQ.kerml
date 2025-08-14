standard library package ISQ {

    // Operations are hard coded in Quantity class.
    // Here we need only base interface that complies with standard ISQ.
    // Note that SI unit strings are not fully compliant.

    datatype Quantity :> Ranges::QuantityInRange;
    datatype ScalarQuantityValue :> Quantity;

    // Base quantities
    datatype LengthValue :> Quantity   { :>> unit = "m"; }
    datatype DurationValue :> Quantity { :>> unit = "s"; }
    datatype MassValue :> Quantity     { :>> unit = "kg"; }
    datatype ElectricCurrentValue :> Quantity { :>> unit = "A"; }
    datatype ThermodynamicTemperatureValue :> Quantity { :>> unit = "K"; }
    datatype LuminousIntensityValue :> Quantity { :>> unit = "cd";  }
    datatype AmountOfSubstanceValue :> Quantity { :>> unit = "mol"; }

    // Derived quantities
    datatype VolumeValue :> Quantity { :>> unit = "m^3"; }
    datatype TimeValue :> Quantity { :>> unit = "s"; }
    datatype SpeedValue :> Quantity { :>> unit = "m/s"; }
    datatype AccelerationValue :> Quantity { :>> unit = "m/s^2"; }
    datatype EnergyValue :> Quantity { :>> unit = "J"; }    // or in Base Units:  kg*m^2 / s^2, but SysMD converts anyhow.
    datatype PowerValue :> Quantity { :>> unit = "W"; }

    // Mechanical quantities
    datatype MassDensityValue :> ScalarQuantityValue { :>> unit = "kg/m^3"; }

    // Electrical quantities
    // Current is in base quantities
    datatype ElectricChargeValue :> ScalarQuantityValue { :>> unit = "A s"}
    datatype VoltageValue :> Quantity { :>> unit = "V"; }
    datatype InductanceValue :> ScalarQuantityValue { :>> unit = "H"; }
    datatype ResistanceValue :> ScalarQuantityValue { :>> unit = "A/V"; }
    datatype CapacitanceValue :> ScalarQuantityValue { :>> unit = "A s / V"}


    feature <isq> 'International System of Quantities' {
         feature L: LengthValue[1];
         feature M: MassValue[1];
         feature T: DurationValue[1];
         feature I: ElectricCurrentValue[1];
         feature 'Θ': ThermodynamicTemperatureValue[1];
         feature N: AmountOfSubstanceValue[1];
         feature J: LuminousIntensityValue[1];
    }
}