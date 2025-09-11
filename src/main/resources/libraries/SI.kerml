
standard library package SI {
    datatype Quantity :> Ranges::QuantityInRange;
    //Basic SI Domains
    datatype AmountOfMoney :> Quantity;
    datatype AmountOfSubstance :> Quantity;
    datatype ElectricCurrent :> Quantity; 
    datatype InformationCapacity :> Quantity; 
    datatype Length :> Quantity;
    datatype LuminousIntensity :> Quantity;
    datatype Mass :> Quantity;
    datatype Temperature :> Quantity; 
    datatype Time :> Quantity; 
    //derived Domains
    datatype AbsorbedDose :> Quantity;
    datatype Acceleration :> Quantity;
    datatype Activity :> Quantity;
    datatype ActivArity :> Quantity;
    datatype Area :> Quantity; 
    datatype BitRate :> Quantity;
    datatype Capacitance :> Quantity; 
    datatype CatalyticActivity :> Quantity;
    datatype Density :> Quantity;
    datatype DoseEquivalent :> Quantity;
    datatype ElectricalConductance :> Quantity;
    datatype ElectricalResistance :> Quantity;
    datatype ElectricCharge :> Quantity;
    datatype ElectricField :> Quantity;
    datatype ElectricPotential :> Quantity;
    datatype Energy :> Quantity;
    datatype EnergyDensity :> Quantity;
    datatype Entropy :> Quantity;
    datatype Force :> Quantity;
    datatype Frequency :> Quantity;
    datatype Illuminance :> Quantity;
    datatype Inductance :> Quantity;
    datatype KinematicViscosity :> Quantity;
    datatype Luminance :> Quantity;
    datatype LuminousEfficacy :> Quantity;
    datatype LuminousEnergy :> Quantity;
    datatype LuminousFlux :> Quantity;
    datatype MagneticFlux :> Quantity;
    datatype MagneticFluxDensity :> Quantity;
    datatype MassFlow :> Quantity;
    datatype MomentOfForce :> Quantity;
    datatype MomentOfInertia :> Quantity;
    datatype Momentum :> Quantity;
    datatype Permittivity :> Quantity;
    datatype Power :> Quantity;
    datatype PowerDensity :> Quantity;
    datatype Pressure :> Quantity;
    datatype QuantityOfDomainOne :> Quantity;
    datatype SolidAngle :> Quantity;
    datatype Speed :> Quantity; 
    datatype Timestamp :> Quantity; 
    datatype Volume :> Quantity; 
    datatype Voltage :> Quantity; 
}
