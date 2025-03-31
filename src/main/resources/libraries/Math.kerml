package Math {
    feature pi: ScalarValues::Real(3.14159265358979323846..3.14159265358979323846);
    feature e:  ScalarValues::Real(2.71828182845904523536..2.71828182845904523536);
}

package Physics {
    feature c:  ScalarValues::Real(299792458..299792458) [m/s];
    feature mu0: ScalarValues::Real(1.25663706212e-6..1.25663706212e-6) [H/m];
    feature e0: ScalarValues::Real(8.8541878128e-12..8.8541878128e-12) [A s / V m];
}

