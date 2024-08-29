#include <iostream>
#include <fstream>
#include <list>
using namespace std;

typedef struct{
    string reqName;
    double reqResValue;
    string reqResUnit;
    double reqRefValue;
    string reqRefUnit;
    string attributeQualifiedName;
    bool success;
}Result;

enum class Operator{
    GT,GE,LT,LE,EE
};

class ResultWriter{

private:
    std::list< Result > results;

public:
    ResultWriter(){}

    void addResult(string requirementName, double resultValue,  string resultUnit, Operator op, double referenceValue, string referenceUnit, string attributeQualifiedName){
        Result newResult;
        newResult.reqName = requirementName;
        newResult.reqResValue = resultValue;
        newResult.reqResUnit = resultUnit;
        newResult.reqRefValue = referenceValue;
        newResult.reqRefUnit = referenceUnit;
        newResult.attributeQualifiedName = attributeQualifiedName;

       switch(op){
            case Operator::GT: {
                newResult.success = resultValue > referenceValue;
                break;
            }
            case Operator::GE: {
               newResult.success = resultValue >= referenceValue;
               break;
            }
            case Operator::LT: {
                newResult.success = resultValue < referenceValue;
                break;
            };
            case Operator::LE: {
               newResult.success = resultValue <= referenceValue;
               break;
            }
            case Operator::EE :{
                newResult.success = resultValue == referenceValue;
                break;
            };
        }


        results.push_back(newResult);
    }

    void writeResultFile(){
        ofstream resultFile("src/test/resources/toSystemC/test/testbenches/results.json");
        resultFile<<"["<<endl;
            while(results.size() > 0){
                cout<<"Writing result for Requirement: "<<results.back().reqName<<endl;
                resultFile
                <<"\t{"<<endl
                <<"\t \"constraintName\":\""<<results.back().reqName<<"\""<<","<<endl
                <<"\t \"resultValue\":"<<results.back().reqResValue<<","<<endl
                <<"\t \"resultUnit\":\""<<results.back().reqResUnit<<"\""<<","<<endl
                <<"\t \"referenceValue\":"<<results.back().reqRefValue<<","<<endl
                <<"\t \"referenceUnit\":\""<<results.back().reqRefUnit<<"\""<<","<<endl
                <<"\t \"successful\":"<<results.back().success<<","<<endl
                <<"\t \"attributeQualifiedName\":\""<<results.back().attributeQualifiedName<<"\""<<""<<endl
                <<"\t}";
                results.pop_back();
                if(results.size() == 0) resultFile<<endl; else resultFile<<","<<endl;
            }
            resultFile<<"]"<<endl;
            resultFile.close();

    }
};

