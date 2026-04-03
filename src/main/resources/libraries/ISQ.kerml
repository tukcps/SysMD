standard library package ISQ {

    // Operations are hard coded in Quantity class.
    // Here we need only base interface that complies with standard ISQ.
    // Note that SI unit strings are not fully compliant.


    // Base quantities
    datatype LengthValue :> Quantities::ScalarQuantityValue { :>> unit = "m"; }
    datatype DurationValue :> Quantities::ScalarQuantityValue { :>> unit = "s"; }
    datatype MassValue :> Quantities::ScalarQuantityValue { :>> unit = "kg"; }
    datatype ElectricCurrentValue :> Quantities::ScalarQuantityValue { :>> unit = "A"; }
    datatype ThermodynamicTemperatureValue :> Quantities::ScalarQuantityValue { :>> unit = "K"; }
    datatype LuminousIntensityValue :> Quantities::ScalarQuantityValue { :>> unit = "cd";  }
    datatype AmountOfSubstanceValue :> Quantities::ScalarQuantityValue { :>> unit = "mol"; }
    datatype AmountOfMoneyValue :> Quantities::ScalarQuantityValue { :>> unit = "€"; }
    datatype StorageCapacityValue :> Quantities::ScalarQuantityValue{ :>> unit = "bit"; }

    // Derived quantities
    datatype AbsorbedDoseValue :> Quantities::ScalarQuantityValue { :>> unit = "Gy"; }
    datatype AreaValue :> Quantities::ScalarQuantityValue { :>> unit = "m^2";  }
    datatype BitRateValue :> Quantities::ScalarQuantityValue { :>> unit = "bit/s"; }
    datatype ConductanceValue :> Quantities::ScalarQuantityValue { :>> unit = "S"; }
    datatype DoseEquivalentValue :> Quantities::ScalarQuantityValue{ :>> unit = "Sv"; }
    datatype DimensionOneValue :> Quantities::ScalarQuantityValue { :>> unit = "1"; }
    datatype EnergyValue :> Quantities::ScalarQuantityValue { :>> unit = "J"; }
    datatype EnergyDensityValue :> Quantities::ScalarQuantityValue { :>> unit = "J/m^3"; }
    datatype EntropyValue :> Quantities::ScalarQuantityValue { :>> unit = "J/K"; }
    datatype FrequencyValue :> Quantities::ScalarQuantityValue { :>> unit = "1/s"; }
    datatype IlluminanceValue :> Quantities::ScalarQuantityValue { :>> unit = "lx"; }
    datatype KinematicViscosityValue :> Quantities::ScalarQuantityValue { :>> unit = "St"; }
    datatype LuminanceValue :> Quantities::ScalarQuantityValue { :>> unit = "sb"; }
    datatype LuminousEfficacyValue :> Quantities::ScalarQuantityValue { :>> unit = "lm/W"; }
    datatype LuminousEnergyValue :> Quantities::ScalarQuantityValue { :>> unit = "lm s"; }
    datatype LuminousFluxValue :> Quantities::ScalarQuantityValue { :>> unit = "lm"; }
    datatype PermittivityValue :> Quantities::ScalarQuantityValue { :>> unit = "F/m"; }
    datatype MagneticFluxValue :> Quantities::ScalarQuantityValue { :>> unit = "Wb"; }
    datatype MagneticFluxDensityValue :> Quantities::ScalarQuantityValue { :>> unit = "T"; }
    datatype MomentOfForceValue :> Quantities::ScalarQuantityValue { :>> unit = "Nm"; }
    datatype MomentOfInertiaValue :> Quantities::ScalarQuantityValue { :>> unit = "kg m^2"; }
    datatype NuclearActivityValue :> Quantities::ScalarQuantityValue { :>> unit = "Bq"; }
    datatype SolidAngularMeasureValue :> Quantities::ScalarQuantityValue { :>> unit = "sr"; }

    // Mechanical quantities
    datatype AccelerationValue :> Quantities::ScalarQuantityValue { :>> unit = "m/s^2"; }
    datatype ForceValue :> Quantities::ScalarQuantityValue { :>> unit = "N"; }
    datatype MassDensityValue :> Quantities::ScalarQuantityValue { :>> unit = "kg/m^3"; }
    datatype MassFlowValue :> Quantities::ScalarQuantityValue { :>> unit = "kg/s"; }
    datatype MomentumValue :> Quantities::ScalarQuantityValue { :>> unit = "kg m/s"; }
    datatype PowerValue :> Quantities::ScalarQuantityValue { :>> unit = "W"; }
    datatype PressureValue :> Quantities::ScalarQuantityValue { :>> unit = "Pa"; }
    datatype SpeedValue :> Quantities::ScalarQuantityValue { :>> unit = "m/s"; }
    datatype VolumeValue :> Quantities::ScalarQuantityValue { :>> unit = "m^3"; }

    // Electrical quantities
    // Current is in base quantities
    datatype CapacitanceValue :> Quantities::ScalarQuantityValue { :>> unit = "A s / V";}
    datatype ElectricChargeValue :> Quantities::ScalarQuantityValue { :>> unit = "A s";}
    datatype ElectricFieldStrengthValue :> Quantities::ScalarQuantityValue { :>> unit = "N/C"; }
    datatype ElectricPotentialValue :> Quantities::ScalarQuantityValue { :>> unit = "V"; }
    datatype ElectricPotentialDifferenceValue :> Quantities::ScalarQuantityValue { :>> unit = "V"; }
    datatype InductanceValue :> Quantities::ScalarQuantityValue { :>> unit = "H"; }
    datatype ResistanceValue :> Quantities::ScalarQuantityValue { :>> unit = "Ω"; }
    datatype TimeValue :> Quantities::ScalarQuantityValue { :>> unit = "s"; }


    // VectorQuantityValues
    datatype CartesianForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "N"; }
    datatype CartesianPosition3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "m"; }
    datatype CartesianVelocity3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "m/s"; }
    datatype CartesianAcceleration3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "m/s^2"; }
    datatype CartesianMomentum3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "kg m/s"; }
    datatype CartesianMomentOfForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "Nm"; }
    datatype CartesianElectricFieldStrength3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "N/C"; }
    datatype CartesianMagneticFluxDensity3dVector :> Quantities::'3dVectorQuantityValue' { :>> unit = "T"; }

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