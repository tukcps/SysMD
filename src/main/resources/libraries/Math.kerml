
/**
 * Some mathematical constants.
 * We model them as ranges to consider inaccurate representation.
 */
library package Math {
    feature pi: Ranges::RealInRange { :>> range = 3.14159265358979323846..3.14159265358979323846; }
    feature e:  Ranges::RealInRange { :>> range = 2.71828182845904523536..2.71828182845904523536; }
}

/**
 * Some physical constants.
 */
library package Physics {
    // Speed of light
    feature c:   ISQ::SpeedValue(299792458..299792458 [m/s]);
    feature mu0: Quantities::ScalarQuantityValue(1.25663706212e-6..1.25663706212e-6 [H/m]);
    feature e0:  Quantities::ScalarQuantityValue(8.8541878128e-12..8.8541878128e-12 [A s / V m]);
}

