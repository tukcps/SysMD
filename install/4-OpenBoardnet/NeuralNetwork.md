---
title: Neural Network Model
name:  NeuralNetwork
logo: Files/car-small.png
---
# Neural Network Model
## Neural Network Layer Base
```SysML::OpenBoardnet
package NeuralNetworkModel {
    private import BaseTypes::*;
    
    // Abstract base type for all layers
    part def NeuralNetworkLayer {
        attribute FLOPs: DimensionOneValue {:>> unit = "MFLOPs";}
        attribute Memory: StorageCapacityValue;
        part precision: PrecisionTypes::Precision;
    }
}
```
## Layer Definitions
### Dense Layer
```SysML::OpenBoardnet::NeuralNetworkModel
part def DenseLayer :> NeuralNetworkLayer {
    attribute inputNeurons: Integer;
    attribute outputNeurons: Integer;
        
    // FLOPs calculation
    :>> FLOPs =  ToReal(2 * inputNeurons * outputNeurons);

    // Memory calculation
    :>> Memory = ToReal(inputNeurons * outputNeurons + outputNeurons) * precision::size {:>> unit = "kB";}
}
```
### Convolution Layer
```SysML::OpenBoardnet::NeuralNetworkModel
part def ConvLayer :> NeuralNetworkLayer {
    attribute kernelSize: Integer;
    attribute numFilters: Integer;
    attribute inputHeight: Integer;
    attribute inputWidth: Integer;
    attribute numChannels: Integer;
    attribute stride: Integer;
    attribute padding: Integer;   
    attribute outputHeight: Integer = floor((inputHeight - kernelSize + 2 * padding) / stride) + 1;
    attribute outputWidth: Integer = floor((inputWidth - kernelSize + 2 * padding) / stride) + 1;
    
    :>> FLOPs =  ToReal(2 * kernelSize^2 * numChannels * numFilters * outputHeight * outputWidth);  
    :>> Memory = ToReal(kernelSize^2 * numChannels * numFilters + numFilters) * precision::size {:>> unit = "kB";}
}
```
### Pooling Layer
```SysML::OpenBoardnet::NeuralNetworkModel
part def PoolingLayer :> NeuralNetworkLayer {
    attribute kernelSize: Integer;
    attribute inputHeight: Integer;
    attribute inputWidth: Integer;
    attribute numChannels: Integer;
        
    :>> FLOPs =  ToReal((kernelSize^2 - 1) * inputHeight * inputWidth * numChannels);
    // Memory requirement is negligible
    :>> Memory = 0.0 [B];
}
```
## Batch Normalization Layer
```SysML::OpenBoardnet::NeuralNetworkModel
part def BatchNormLayer :> NeuralNetworkLayer {
    attribute numNeurons: Integer;
        
    :>> FLOPs = ToReal(2 * numNeurons);
    :>> Memory = ToReal(4 * numNeurons) * precision::size {:>> unit = "kB";}
}
```
## SiLu Layer
```SysML::OpenBoardnet::NeuralNetworkModel
part def SiLULayer :> NeuralNetworkLayer {
    attribute numChannels : Integer;
    attribute inputHeight : Integer;
    attribute inputWidth  : Integer;

    :>> FLOPs = ToReal(4 * numChannels * inputHeight * inputWidth);
    :>> Memory = 0.0[B];
}
```
# YOLOv5n Model
```SysML::OpenBoardnet::NeuralNetworkModel
part def NeuralNetwork{
    attribute FLOPsTotal: DimensionOneValue = sumOverParts(FLOPs) {:>> unit = "GFLOPs";}
    attribute MemoryTotal : StorageCapacityValue = sumOverParts(Memory) {:>> unit = "MB";}
}

part def Yolov5n:> NeuralNetwork {
    part model_0_conv : ConvLayer {
        :>> kernelSize = 6;
        :>> numFilters = 16;
        :>> inputHeight = 640;
        :>> inputWidth = 640;
        :>> numChannels = 3;
        :>> stride = 2;
        :>> padding = 2;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_0_bn : BatchNormLayer {
        :>> numNeurons = 16;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_0_act : SiLULayer {
        :>> numChannels = 16;
        :>> inputHeight = 320;
        :>> inputWidth = 320;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_1_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 32;
        :>> inputHeight = 320;
        :>> inputWidth = 320;
        :>> numChannels = 16;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv1_bn : BatchNormLayer {
        :>> numNeurons = 16;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv1_act : SiLULayer {
        :>> numChannels = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 16;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 16;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv1_act : SiLULayer {
        :>> numChannels = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 16;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 16;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_m_0_cv2_act : SiLULayer {
        :>> numChannels = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv2_bn : BatchNormLayer {
        :>> numNeurons = 16;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv2_act : SiLULayer {
        :>> numChannels = 16;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv3_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_2_cv3_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_3_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 160;
        :>> inputWidth = 160;
        :>> numChannels = 32;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_3_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_3_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_0_cv2_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv2_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_m_1_cv2_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv2_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv2_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv3_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_4_cv3_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_5_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 128;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_5_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_5_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_0_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_1_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_m_2_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv3_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_6_cv3_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_7_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 256;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_7_bn : BatchNormLayer {
        :>> numNeurons = 256;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_7_act : SiLULayer {
        :>> numChannels = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv1_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv1_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv1_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_m_0_cv2_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv2_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv2_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv3_bn : BatchNormLayer {
        :>> numNeurons = 256;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_8_cv3_act : SiLULayer {
        :>> numChannels = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv1_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv1_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_m_1 : PoolingLayer {
        :>> kernelSize = 5;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_m_2 : PoolingLayer {
        :>> kernelSize = 5;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_m : PoolingLayer {
        :>> kernelSize = 5;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 512;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv2_bn : BatchNormLayer {
        :>> numNeurons = 256;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_9_cv2_act : SiLULayer {
        :>> numChannels = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_10_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_10_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_10_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_m_0_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv3_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_13_cv3_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_14_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_14_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_14_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv1_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 32;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_m_0_cv2_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv2_bn : BatchNormLayer {
        :>> numNeurons = 32;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv2_act : SiLULayer {
        :>> numChannels = 32;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv3_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_17_cv3_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_18_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_18_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_18_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv1_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_m_0_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv2_bn : BatchNormLayer {
        :>> numNeurons = 64;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv2_act : SiLULayer {
        :>> numChannels = 64;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv3_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_20_cv3_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_21_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 128;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 2;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_21_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_21_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv1_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv1_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv1_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv1_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv1_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv2_conv : ConvLayer {
        :>> kernelSize = 3;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 1;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv2_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_m_0_cv2_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv2_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv2_bn : BatchNormLayer {
        :>> numNeurons = 128;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv2_act : SiLULayer {
        :>> numChannels = 128;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv3_conv : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv3_bn : BatchNormLayer {
        :>> numNeurons = 256;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_23_cv3_act : SiLULayer {
        :>> numChannels = 256;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_24_m_0 : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 255;
        :>> inputHeight = 80;
        :>> inputWidth = 80;
        :>> numChannels = 64;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_24_m_1 : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 255;
        :>> inputHeight = 40;
        :>> inputWidth = 40;
        :>> numChannels = 128;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
    part model_24_m_2 : ConvLayer {
        :>> kernelSize = 1;
        :>> numFilters = 255;
        :>> inputHeight = 20;
        :>> inputWidth = 20;
        :>> numChannels = 256;
        :>> stride = 1;
        :>> padding = 0;
        part precision: BaseTypes::PrecisionTypes::Float32;
    }
}
```
