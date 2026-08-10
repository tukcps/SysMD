standard library package ISQ {

    // Operations are hard coded in Quantity class.
    // Here we need only base interface that complies with standard ISQ.
    // Note that SI unit strings are not fully compliant.


    // Base quantities
    datatype LengthValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [m]; }
    datatype DurationValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [s]; }
    datatype MassValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [kg]; }
    datatype ElectricCurrentValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [A]; }
    datatype ThermodynamicTemperatureValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [K]; }
    datatype LuminousIntensityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [cd];  }
    datatype AmountOfSubstanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [mol]; }
    datatype AmountOfMoneyValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [EUR]; }
    datatype StorageCapacityValue :> Quantities::ScalarQuantityValue{ :>> range default := *..* [bit]; }

    // Derived quantities
    datatype AbsorbedDoseValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Gy]; }
    datatype AreaValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [m^2];  }
    datatype BitRateValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [bit/s]; }
    datatype ConductanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [S]; }
    datatype DoseEquivalentValue :> Quantities::ScalarQuantityValue{ :>> range default := *..* [Sv]; }
    datatype DimensionOneValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [1]; }
    datatype EnergyValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [J]; }
    datatype EnergyDensityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [J/m^3]; }
    datatype EntropyValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [J/K]; }
    datatype FrequencyValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [1/s]; }
    datatype IlluminanceValue :> Quantities::ScalarQuantityValue { :>> range default := (*..*) [lx]; }
    datatype KinematicViscosityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [St]; }
    datatype LuminanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [sb]; }
    datatype LuminousEfficacyValue :> Quantities::ScalarQuantityValue { :>> range default := *..*[lm/W]; }
    datatype LuminousEnergyValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [lm s]; }
    datatype LuminousFluxValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [lm]; }
    datatype PermittivityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [F/m]; }
    datatype MagneticFluxValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Wb]; }
    datatype MagneticFluxDensityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [T]; }
    datatype MomentOfForceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Nm]; }
    datatype MomentOfInertiaValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [kg m^2]; }
    datatype NuclearActivityValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Bq]; }
    datatype SolidAngularMeasureValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [sr]; }

    // Mechanical quantities
    datatype AccelerationValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [m/s^2]; }
    datatype ForceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [N]; }
    datatype MassDensityValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [kg/m^3]; }
    datatype MassFlowValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [kg/s]; }
    datatype MomentumValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [kg m/s]; }
    datatype PowerValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [W]; }
    datatype PressureValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Pa]; }
    datatype SpeedValue :> Quantities::ScalarQuantityValue { :>> range default :=  *..* [m/s]; }
    datatype VolumeValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [m^3]; }

    // Electrical quantities
    // Current is in base quantities
    datatype CapacitanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [A s / V];}
    datatype ElectricChargeValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [A s];}
    datatype ElectricFieldStrengthValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [N/C]; }
    datatype ElectricPotentialValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [V]; }
    datatype ElectricPotentialDifferenceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [V]; }
    datatype InductanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [H]; }
    datatype ResistanceValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [Ω]; }
    datatype TimeValue :> Quantities::ScalarQuantityValue { :>> range default := *..* [s]; }


    // VectorQuantityValues
    datatype CartesianForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [N]; }
    datatype CartesianPosition3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [m]; }
    datatype CartesianVelocity3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [m/s]; }
    datatype CartesianAcceleration3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [m/s^2]; }
    datatype CartesianMomentum3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [kg m/s]; }
    datatype CartesianMomentOfForce3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [Nm]; }
    datatype CartesianElectricFieldStrength3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default := (*..*, *..*, *..*) [N/C]; }
    datatype CartesianMagneticFluxDensity3dVector :> Quantities::'3dVectorQuantityValue' { :>> range default :=  (*..*, *..*, *..*) [T]; }

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