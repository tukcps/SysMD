---
title: Reliability
name:  Reliability
---
# Contents
[toc]
# Reliability Metrics

Part 5 of ISO 26262 covers product development at hardware level, and with that, fault metrics defined by the standard.
Two main metrics defined in the standard are**Single-point Fault Metric**, measuring the robustness of the design to single point
and residual faults, and**Latent-fault Metric**, measuring robustness of the design to latent faults. Both only apply to higher
ASIL level (B, C, D).

Both metrics depend on their respective failure rates, as well as the total failure rate $\lambda_{tot}$.
```SysML::OpenBoardnet
package Reliability {

    abstract part def ReliableItem {
        attribute mtbf: DurationValue {:>> unit = "h";}
        attribute mttr: DurationValue {:>> unit = "h";}
        attribute availability: DimensionOneValue {:>> range = 0.0..100.0 [%];}

        attribute lambdaSPF:     FrequencyValue;           // single-point failure rate
        attribute lambdaRF:      FrequencyValue;           // residual-fault failure rate
        attribute lambdaMPF:     FrequencyValue;           // multiple-point failure rate
        attribute lambdaS:       FrequencyValue;           // safe-fault failure rate
        attribute totLambda:     FrequencyValue;           // total failure rate
        attribute SPF_det:       FrequencyValue;           // detected single-point failure rate
        attribute RF_det:        FrequencyValue;           // detected residual-fault failure rate
        attribute MPF_det:       FrequencyValue;           // detected multiple-point failure rate
        attribute LF_det:        FrequencyValue;           // detected latent fault failure rate
        attribute totLambda_det: FrequencyValue;           // total detected failure rate
        
        attribute Trepair:       DurationValue;            // Duration to repair
        attribute Nrepair:       Real;                     // number of repair events
        
        attribute SPFM_MPF:      DimensionOneValue {:>> unit ="%";} 
        attribute SPFM_SFM:      DimensionOneValue {:>> unit ="%";} 
        attribute LFM:           DimensionOneValue {:>> unit ="%";} 
        attribute DCoverage:     DimensionOneValue {:>> unit ="%";} 
        attribute RFP:           DimensionOneValue {:>> unit ="%";} 
        
        attribute MTTF:          DurationValue;            // mean time to failure
        attribute DurationValue_period: DurationValue {:>> unit ="h";} // analysis period
        attribute reliability:   DimensionOneValue {:>> unit ="%";} 
        attribute probFailure:   DimensionOneValue {:>> unit ="%";} 
    }
}
```
## ISO 26262-related Metrics
Part 5 (Hardware Level) and Part 9 (ASIL-Oriented Analyses) primarily discuss the metrics, their determination, as well as
how they relate to the particular ASIL level.
### Failure rate
Failure rate $\lambda$ is the FrequencyValue with which a system or a component fails ($$N_{fail}$) during the total operating DurationValue
($$T_{oper}$). Usually expressed in failures per unit DurationValue, e.g. failures per hour. ISO 26262 commonly refers to the
**Probability of Failure per Hour (PFH)**, which is nothing other than number of failures per hour.

$\lambda = \frac{N_{fail}}{T{_{oper}}}$

$PFH = \lambda = \frac{N_{fail}}{hour}$

Total failure rate is the sum of all possible failures in the system: single-point fault ($$\lambda_{SPF}$), residual fault
($$\lambda_{RF}$), multiple-point fault ($$\lambda_{MPF}$) and safe fault ($$\lambda_{S}$).

$\lambda_{tot} = \lambda_{SPF} + \lambda_{RF} + \lambda_{MPF} + \lambda_{S}$
```SysML::OpenBoardnet::Reliability
calc def calcLambda {
    in attribute N_fail:    ScalarQuantityValue;
    in attribute T_oper:    DurationValue;
    return lambda:          FrequencyValue = N_fail/T_oper;    
}

calc def calcTotFailureRate {
    in attribute lSPF: FrequencyValue;
    in attribute lRF:  FrequencyValue;
    in attribute lMPF: FrequencyValue;
    in attribute lS:   FrequencyValue;
    return tot: FrequencyValue = lSPF + lRF + lMPF + lS;
}
```
### Single-point Fault Metric (SPFM)
SPFM is the robustness of a system to single-point and residual faults, and is defined as a sum of multiple-point and safe
faults divided by the total failure rate, or a sum of single-point and residual faults divided by the total failure rate:

$SPFM = \frac{\sum (\lambda_{MPF} + \lambda_{S})}{\sum (\lambda_{tot})} = 1 - \frac{\sum (\lambda_{SPF} + \lambda_{RF})}{\sum (\lambda_{tot})}$
```SysML::OpenBoardnet::Reliability
calc def calcSPFM_MPF {
    in attribute lMPF: FrequencyValue;
    in attribute lS:   FrequencyValue;
    in attribute lTot: FrequencyValue;
    return SPFM: DimensionOneValue = (lMPF + lS) / lTot {:>> unit ="%";}
}
   
calc def calcSPFM_SPF {
    in attribute lambdaSPF: FrequencyValue;
    in attribute lambdaRF:  FrequencyValue;
    in attribute lambdaTot: FrequencyValue;
    return SPFM_SPF:  DimensionOneValue = (lambdaSPF + lambdaRF)/lambdaTot {:>> unit ="%";}
}
```
### Latent-fault Metric (LFM)
LFM is the proportion of the latent faults, that remain undetected during operation, but could contribute to a hazardous event,
and is defined as a sum of detected/perceived multiple-point faults and safe faults divided by the total failure rate.

$LFM = \frac{\sum (\lambda_{MPF_{Per/Det}} + \lambda_{S})}{\sum (\lambda_{MPF} + \lambda_{S})}$

Regarding the target of SPFM and LFM for specific ASILs, standard defines the dependency as:

|       | ASIL A | ASIL B | ASIL C | ASIL D |
| ------| ------ | ------ | ------ | ------ |
| SPFM  |   /   | >= 90% | >= 97% | >= 99% |
| LFM   |   /   | >= 60% | >= 80% | >= 90% |
```SysML::OpenBoardnet::Reliability
calc def calcLFM {
    in attribute lMPF_det: FrequencyValue;
    in attribute lS:       FrequencyValue;
    in attribute lMPF:     FrequencyValue;
    return LFM_val: DimensionOneValue = (lMPF_det + lS) / (lMPF + lS) {:>> unit ="%";}
}
```
### Diagnostic Coverage (DC)
Diagnostic Coverage is the ratio of detected number of faults with respect to the total number of faults.
Higher diagnostic coverage indicates better fault detection and mitigation capabilities (necessary for higher ASILs).

$DC = (\frac{\lambda_{detected}}{\lambda_{total}})*100%$

Failure rate of detected faults is the sum of all other**detected**failure rates, except failure rate of safe faults, due to
the fact they do not contribute to the impact on system performance.

$\lambda_{detected} = \lambda_{SPF_{det}} + \lambda_{MPF_{det}} + \lambda_{RF_{det}} + \lambda_{LF_{det}}$
```SysML::OpenBoardnet::Reliability
calc def calcDetectedFailureRate {
    in attribute SPF_det:        FrequencyValue;
    in attribute MPF_det:        FrequencyValue;
    in attribute RF_det:         FrequencyValue;
    in attribute LF_det:         FrequencyValue;
    return detFailureRate: FrequencyValue = SPF_det + MPF_det + RF_det + LF_det;    
}

calc def calcDiagnosticCoverage {
    in attribute detFailureRate: FrequencyValue;
    in attribute totFailureRate: FrequencyValue;
    return DC:             DimensionOneValue = detFailureRate/totFailureRate {:>> unit ="%";}
}
```
### Residual Fault Probability (RFP)
RFP is the probability that a fault remains undetected in the system after applying diagnostic measures.
It ensures that the residual risk of undetected faults is within acceptable limits defined by the particular ASIL.

$RFP = 1-DC$

|       | ASIL A | ASIL B    | ASIL C    | ASIL D |
| ------| ------ | ------    | ------    | ------ |
| DC    | >= 60% | >= 90%    | >= 99%    | >= 99% |
| RFP   | /     | >= ASIL A | >= ASIL B | ~ 0% |
```SysML::OpenBoardnet::Reliability
calc def calcRFP {
        in attribute DC:  DimensionOneValue {:>> unit ="%";}
        return RFP: DimensionOneValue  = 1.0 - DC {:>> unit ="%";}   
    }
```
## Non-ISO 26262-related Metrics
Even though metrics mentioned in ISO 26262 standard are of the utmost imporance, there are various other metrics that are
related to reliability, but are not specified in the standard as parts of it.
### Mean DurationValue to Failure (MTTF)
The average DurationValue between failures for a non-repairable system, a basic measure of reliability for systems that cannot be repaired.
For the calculation, the assumption is made that the failure rate is constant.

$MTTF = \frac{T_{total}}{N_{failures}} = \frac{1}{\lambda}$
```SysML::OpenBoardnet::Reliability
calc def calcMTTF {
    in attribute lam: FrequencyValue;
    return mttf_val: DurationValue = 1.0 / lam;
}
```
### Mean DurationValue to Repair (MTTR)
The average DurationValue required to repair a system and restore it to operational status after a failure.

$MTTR = \frac{T_{repair}}{N_{repairs}}$
```SysML::OpenBoardnet::Reliability
calc def calcMTTR {
    in attribute Trepair: DurationValue;
    in attribute Nrepair: Real;
    return MTTR: DurationValue = Trepair/Nrepair;    
}
```
### Mean DurationValue Between Failures (MTBF)
For repairable systems, it is the average DurationValue between failures, including both the DurationValue the system operates between failures
(MTTF) and DurationValue to repair the failures (MTTR).

$MTBF = MTTF + MTTR$
```SysML::OpenBoardnet::Reliability
calc def calcMTBF {
    in attribute inMTTF: DurationValue;
    in attribute inMTTR: DurationValue;
    return MTBF: DurationValue = inMTTF + inMTTR;    
}
```
### Availability (V)
Proportion of DurationValue a system is in a functional condition, a key metrics to asses the overall performance and reliability of
the system.

$V = \frac{MTTF}{MTTF + MTTR} = \frac{MTTF}{MTBF}$

|        | ASIL A | ASIL B | ASIL C | ASIL D |
| ------ | ------ | ------ | ------ | ------ |
| V      | >= 99% | >= 99.9% | >= 99.99% | >= 99.999% |
```SysML::OpenBoardnet::Reliability
calc def calcAvail {
    in attribute iMTTF: DurationValue;
    in attribute iMTBF: DurationValue;
    return v_val: DimensionOneValue = iMTTF / iMTBF {:>> unit ="%";}
}
```
### Reliability (R(t))
Probability that a system or component performs its required function(s) without failure
over a specified period of DurationValue $t$.

$R(t) = e^{-\lambda t}$
```SysML::OpenBoardnet::Reliability
calc def calcReliab {
    in attribute lambda: FrequencyValue;
    in attribute timePeriod: DurationValue; 
    return R: DimensionOneValue = exp(-lambda*timePeriod) {:>> unit ="%";}
}
```
### Probability of Failure (Q(t))
Probability that a system of component will fail within a specified period of DurationValue $t$. It is
the opposite of reliability.

$Q(t) = 1 - R(t) = 1 - e^{-\lambda t}$
```SysML::OpenBoardnet::Reliability
calc def calcProbFailure {
        in attribute lambda: FrequencyValue;
        in attribute timePeriod: DurationValue;
        return Q: DimensionOneValue = 1.0 - exp(-lambda*timePeriod) {:>> unit ="%";}    
    }

    // Requirements Definition
    requirement def ReliabilityRequirement {
        subject itm: ReliableItem;
        
        attribute minRequiredMTBF: DurationValue {:>> range = 1..876000 [h];}
        attribute maxAllowedMTTR: DurationValue {:>> range = 0..24 [h];}
        
        constraint {
            itm::mtbf >= minRequiredMTBF
        }
        constraint {
            itm::mttr <= maxAllowedMTTR
        }
    }
```
