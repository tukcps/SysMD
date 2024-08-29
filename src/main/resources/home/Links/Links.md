---
name:       Links
title:      Implementation of the KerML Package "Links"
logo:       Files/logo.png
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version:    2.12
description: The SysML v2 Base library for SysMD Notebook
website:    https://cps.cs.uni-kl.de
---

# Links
This library model introduces the most general Association Link, the type of links, the most general Feature typed by
Associations (see 8.3.4.4 and 8.4.4.5). The participant Feature of Link is the most general associationEnd,
identifying the things being linked by (at the "ends" of) each Link (exactly one thing per end, which might be
the same things). Link is specialized into BinaryLink, the most general Association with exactly two associationEnds,
source and target, which subset participant and identify the two things linked, which might be the same thing. BinaryLink
is the type of binaryLinks, the most general Feature typed by binary Associations.
They are specialized into SelfLink and selfLinks, respectively, for links that have the same thing on both ends,
identified by thisThing and thatThing, redefining source and target, respectively. These are used by BindingConnectors
to specify that Features have the same values (see 8.3.4.5). SelfLinks are not in time or
space (they are not Occurrences, see 9.2.4).

```SysML
standard library package Links {
    assoc Link specializes Base::Anything {
        end feature source: Base::Anything;
        end feature target: Base::Anything;
    }
    feature link : Link; 
    assoc BinaryLink specializes Link {
        end feature source: Base::Anything [1];
        end feature target: Base::Anything [1]; 
    }
    feature binaryLinks : BinaryLink; 
}
```
