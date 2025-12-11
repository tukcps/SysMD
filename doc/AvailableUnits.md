# Table of all supported units

SysMD's parser maps units and measures directly to its solver. 
It does not use SysML v2's units and measures packages. 
Nevertheless, units and domains are implemented in a way that is mostly following
the same syntax as in SysML v2's related packages. 

All the Domains are types in the **ISQ** Package following the SysML v2 standard.

For Quantities without a domain, the following types from the **Quantities** package can be used:
- ScalarQuantityValue
- VectorQuantityValue
- '3DVectorQuantityValue'

# Basic units
| Domain                       | Unit                     |    Symbol     |
|:-----------------------------|:-------------------------|:-------------:|
| **AmountOfMoneyValue**       | Euro                     |       €       |
|                              | USD                      |       $       |
|                              | Pound                    |       £       |
| **AreaValueValue**           | Square Meter             |      m^2      |
|                              | Acre                     |      ac       |
| **BitRateValue**             | Bits per Second          |  bps or b/s   |
| **MassDensityValue**         | Kilogram per Cubic Meter |    kg/m^3     |
| **DimensionOneValue**        | Percent                  |       %       |
|                              | Decibel                  |      dB       |
|                              | Degree                   |   ° or deg    |
|                              | Radiant                  |      rad      |
|                              | Pi                       | Pi or pi or π |
| **LengthValue**              | Meter                    |       m       |
|                              | Inch                     |     inch      |
|                              | Feet                     |      ft       |
|                              | Yard                     |      yd       |
|                              | Mile                     |      mi       |
|                              | Nautical mile            |      nmi      |
| **MassValue**                | Kilogram                 |      kg       |
|                              | Tonne (metric ton)       |       t       |
|                              | Short Ton                |      tn       |
|                              | Grain                    |      gr       |
|                              | Carat                    |      ct       |
|                              | Ounce                    |      oz       |
|                              | Pound                    |      lb       |
| **MassFlowValue**            | Kilogram per Second      |     kg/s      |
| **SolidAngularMeasureValue** | Steradian                |      sr       |
| **StorageCapacityValue**     | Bit                      |  bit or Bit   |
|                              | Byte                     |   B or Byte   |
| **DurationValue**            | Second                   |       s       |
|                              | Minute                   |      min      |
|                              | Hour                     |       h       |
|                              | Day                      |       d       |
|                              | Year (= 365 days)        | a or yr or y  |
| **VolumeValue**              | Cubic Meter              |      m^3      |
|                              | Litre                    |       l       |
|                              | Pint                     |      pt       |
|                              | Quart                    |      qt       |
|                              | Gallon                   |      gal      |
|                              | Barrel                   |      bbl      |

# Atomic units

| Domain                     | Unit      | Symbol |
|----------------------------|-----------|:------:|
| **AbsorbedDoseValue**      | Gray      |   Gy   |
| **AmountOfSubstanceValue** | Mole      |  mol   |
| **DoseEquivalentValue**    | Sievert   |   Sv   |
| **NuclearActivityValue**   | Becquerel |   Bq   |

## Mechanic units
| **Domain**                  | **Unit**                  | **Symbol** |
|-----------------------------|---------------------------|:----------:|
| **AccelerationValue**       | Meter per Second squared  |   m/s^2    |
| **EnergyValue**             | Joule                     |     J      |
|                             | Calorie                   |    cal     |
|                             | Watt-second               |     Ws     |
|                             | Watt-hour                 |     Wh     |
| **EnergyDensityValue**      | Joule per cubic Meter     |   J/m^3    |
| **ForceValue**              | Newton                    |     N      |
| **FrequencyValue**          | Hertz                     |     Hz     |
| **KinematicViscosityValue** | Stokes                    |     St     |
| **MomentOfForceValue**      | Newton Meter              |     Nm     |
| **MomentOfInertiaValue**    | Kilogram Meter squared    |   kg*m^2   |
| **MomentumValue**           | Kilogram Meter per Second |   kg*m/s   |
| **PowerValue**              | Watt                      |     W      |
|                             | Horsepower                |     HP     |
| **PressureValue**           | Pascal                    |     Pa     |
|                             | Bar                       |    bar     |
|                             | pounds per square inch    |    psi     |
| **SpeedValue**              | Meter per Second          |    m/s     |
|                             | Knot                      |     kt     |
|                             | Miles per hour            |    mph     |

## Electricity and magnetism units
| **Domain**                     | **Unit**           | **Symbol** |
|--------------------------------|--------------------|:----------:|
| **CapacitanceValue**           | Farad              |     F      |
| **ConductanceValue**           | Siemens            |     S      |
| **ElectricChargeValue**        | Coulomb            |     C      |
|                                | Ampere hours       |     Ah     |
|                                | Ampere second      |     As     |
| **ElectricCurrentValue**       | Ampere             |     A      |
| **ElectricFieldStrengthValue** | Newton per Coulomb |    N/C     |
| **ElectricPotentialValue**     | Volt               |     V      |
| **InductanceValue**            | Henry              |     H      |
| **MagneticFluxValue**          | Weber              |     Wb     |
| **MagneticFluxDensityValue**   | Tesla              |     T      |
| **ResistanceValue**            | Ohm                |  Ohm or Ω  |
| **PermittivityValue**          | Farad per Second   |    F/s     |

# Photometry units

| **Domain**                 | **Unit**       | **Symbol** |
|----------------------------|----------------|:----------:|
| **IlluminanceValue**       | Lux            |     lx     |
| **LuminanceValue**         | Stilb          |     sb     |
| **LuminousEnergyValue**    | Lumen Second   |    lm*s    |
| **LuminousFluxValue**      | Lumen          |     lm     |
| **LuminousIntensityValue** | Candela        |     cd     |
| **LuminousEfficacyValue**  | Lumen per Watt |    lm/W    |

# Thermodynamics

| **Domain**                        | **Unit**          | **Symbol** |
|-----------------------------------|-------------------|:----------:|
| **ThermodynamicTemperatureValue** | Kelvin            |     K      |
|                                   | Degree Celsius    |     °C     |
|                                   | Degree Fahrenheit |     °F     |
| **EntropyValue**                  | Joule per kelvin  |    J/K     |

Further units can be derived by multiplication, division and exponentiation of
the above units and by adding a prefix directly to the above units.

# 3D Vectors

| **Domain**                                 | **Unit**                  | **Symbol** |
|--------------------------------------------|---------------------------|:----------:|
| **CartesianAcceleration3dVector**          | Meter per Second squared  |   m/s^2    |
| **CartesianElectricFieldStrength3dVector** | Newton per Coulomb        |    N/C     |
| **CartesianForce3dVector**                 | Newton                    |     N      |
| **CartesianMagneticFluxDensity3dVector**   | Tesla                     |     T      |
| **CartesianMomentum3dVector**              | Kilogram Meter per Second |   kg*m/s   |
| **CartesianMomentOfForce3dVector**         | Newton Meter              |     Nm     |
| **CartesianPosition3dVector**              | Meter                     |     m      |
| **CartesianVelocity3dVector**              | Meter per Second          |    m/s     |



```

```


