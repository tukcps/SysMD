standard library package ISQ {

    // Operations are hard coded in Quantity class.
    // Here we need only base interface that complies with standard ISQ.
    // Note that SI unit strings are not fully compliant.


    // Base quantities
    datatype LengthValue :> Quantities::ScalarQuantityValue { :>> unit default = "m"; }
    datatype DurationValue :> Quantities::ScalarQuantityValue { :>> unit default = "s"; }
    datatype MassValue :> Quantities::ScalarQuantityValue { :>> unit default = "kg"; }
    datatype ElectricCurrentValue :> Quantities::ScalarQuantityValue { :>> unit default = "A"; }
    datatype ThermodynamicTemperatureValue :> Quantities::ScalarQuantityValue { :>> unit default = "K"; }
    datatype LuminousIntensityValue :> Quantities::ScalarQuantityValue { :>> unit default = "cd";  }
    datatype AmountOfSubstanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "mol"; }
    datatype AmountOfMoneyValue :> Quantities::ScalarQuantityValue { :>> unit default = "EUR"; }
    datatype StorageCapacityValue :> Quantities::ScalarQuantityValue{ :>> unit default = "bit"; }

    // Derived quantities
    datatype AbsorbedDoseValue :> Quantities::ScalarQuantityValue { :>> unit default = "Gy"; }
    datatype AreaValue :> Quantities::ScalarQuantityValue { :>> unit default = "m^2";  }
    datatype BitRateValue :> Quantities::ScalarQuantityValue { :>> unit default = "bit/s"; }
    datatype ConductanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "S"; }
    datatype DoseEquivalentValue :> Quantities::ScalarQuantityValue{ :>> unit default = "Sv"; }
    datatype DimensionOneValue :> Quantities::ScalarQuantityValue { :>> unit default = "1"; }
    datatype EnergyValue :> Quantities::ScalarQuantityValue { :>> unit default = "J"; }
    datatype EnergyDensityValue :> Quantities::ScalarQuantityValue { :>> unit default = "J/m^3"; }
    datatype EntropyValue :> Quantities::ScalarQuantityValue { :>> unit default = "J/K"; }
    datatype FrequencyValue :> Quantities::ScalarQuantityValue { :>> unit default = "1/s"; }
    datatype IlluminanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "lx"; }
    datatype KinematicViscosityValue :> Quantities::ScalarQuantityValue { :>> unit default = "St"; }
    datatype LuminanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "sb"; }
    datatype LuminousEfficacyValue :> Quantities::ScalarQuantityValue { :>> unit default = "lm/W"; }
    datatype LuminousEnergyValue :> Quantities::ScalarQuantityValue { :>> unit default = "lm s"; }
    datatype LuminousFluxValue :> Quantities::ScalarQuantityValue { :>> unit default = "lm"; }
    datatype PermittivityValue :> Quantities::ScalarQuantityValue { :>> unit default = "F/m"; }
    datatype MagneticFluxValue :> Quantities::ScalarQuantityValue { :>> unit default = "Wb"; }
    datatype MagneticFluxDensityValue :> Quantities::ScalarQuantityValue { :>> unit default = "T"; }
    datatype MomentOfForceValue :> Quantities::ScalarQuantityValue { :>> unit default = "Nm"; }
    datatype MomentOfInertiaValue :> Quantities::ScalarQuantityValue { :>> unit default = "kg m^2"; }
    datatype NuclearActivityValue :> Quantities::ScalarQuantityValue { :>> unit default = "Bq"; }
    datatype SolidAngularMeasureValue :> Quantities::ScalarQuantityValue { :>> unit default = "sr"; }

    // Mechanical quantities
    datatype AccelerationValue :> Quantities::ScalarQuantityValue { :>> unit default = "m/s^2"; }
    datatype ForceValue :> Quantities::ScalarQuantityValue { :>> unit default = "N"; }
    datatype MassDensityValue :> Quantities::ScalarQuantityValue { :>> unit default = "kg/m^3"; }
    datatype MassFlowValue :> Quantities::ScalarQuantityValue { :>> unit default = "kg/s"; }
    datatype MomentumValue :> Quantities::ScalarQuantityValue { :>> unit default = "kg m/s"; }
    datatype PowerValue :> Quantities::ScalarQuantityValue { :>> unit default = "W"; }
    datatype PressureValue :> Quantities::ScalarQuantityValue { :>> unit default = "Pa"; }
    datatype SpeedValue :> Quantities::ScalarQuantityValue { :>> unit default = "m/s"; }
    datatype VolumeValue :> Quantities::ScalarQuantityValue { :>> unit default = "m^3"; }

    // Electrical quantities
    // Current is in base quantities
    datatype CapacitanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "A s / V";}
    datatype ElectricChargeValue :> Quantities::ScalarQuantityValue { :>> unit default = "A s";}
    datatype ElectricFieldStrengthValue :> Quantities::ScalarQuantityValue { :>> unit default = "N/C"; }
    datatype ElectricPotentialValue :> Quantities::ScalarQuantityValue { :>> unit default = "V"; }
    datatype ElectricPotentialDifferenceValue :> Quantities::ScalarQuantityValue { :>> unit default = "V"; }
    datatype InductanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "H"; }
    datatype ResistanceValue :> Quantities::ScalarQuantityValue { :>> unit default = "Ω"; }
    datatype TimeValue :> Quantities::ScalarQuantityValue { :>> unit default = "s"; }


    // VectorQuantityValues
    datatype CartesianForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "N"; }
    datatype CartesianPosition3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "m"; }
    datatype CartesianVelocity3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "m/s"; }
    datatype CartesianAcceleration3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "m/s^2"; }
    datatype CartesianMomentum3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "kg m/s"; }
    datatype CartesianMomentOfForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "Nm"; }
    datatype CartesianElectricFieldStrength3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "N/C"; }
    datatype CartesianMagneticFluxDensity3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit default = "T"; }

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