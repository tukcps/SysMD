---
name: Math
title: Mathematical and Physical Constants
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: 
website: https://cps.cs.uni-kl.de
---

# Math 
The built-in library of SysMD provides some constants from different domains:

- Mathematics
- Physics and electrical

```

package Math {
    feature pi: ScalarValues::Real(3.14159265358979323846..3.14159265358979323846);
    feature e:  ScalarValues::Real(2.71828182845904523536..2.71828182845904523536);
}

package Physics {
    attribute c:  ScalarValues::Real(299792458..299792458) [m/s];
    attribute mu0: ScalarValues::Real(1.25663706212e-6..1.25663706212e-6) [H/m];
    attribute e0: ScalarValues::Real(8.8541878128e-12..8.8541878128e-12) [A s / V m];
}
```
