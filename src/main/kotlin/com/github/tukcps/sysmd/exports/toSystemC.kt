package com.github.tukcps.sysmd.exports

import com.github.tukcps.sysmd.exceptions.SysMDFatalInternalError
import com.github.tukcps.sysmd.exports.systemCElements.*
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.PartUsageImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.PortUsageImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.RequirementUsageImplementation
import com.github.tukcps.sysmd.services.resolve.resolve
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.reflect.KFunction1


class Exporter {
    private val mainUsages : MutableList<Usage> = mutableListOf()
    private val mainChannels : MutableList<Channel> = mutableListOf()
    private val mainExpressions : MutableList<Feature> = mutableListOf()
    private val mainVariables : MutableList<Variable> = mutableListOf()
    private val mainVariablesNoValues : MutableList<VariableNoValues> = mutableListOf()
    private val mainConstants : MutableList<Constant> = mutableListOf()
    private val mainInputPorts : MutableList<Port> = mutableListOf()
    private val mainOutputPorts : MutableList<Port> = mutableListOf()
    private val mainBidirectionalPorts : MutableList<Port> = mutableListOf()

    private val allModules: HashMap<String, Module> = hashMapOf()
    private val allPorts: HashMap<String, Port> = hashMapOf()
    private val allChannels: MutableList<Channel> = mutableListOf()
    private val allRequirements: HashMap<String, Requirement> = hashMapOf()

    private var macros = BooleanArray(6) {true}
    private var packageName = ""

    private val modulesFolder = "modules/"
    private val testbenchesFolder = "testbenches/"

    /**
     * Takes a Package element as starting point and gathers information from the SysMD tree
     */
    fun analyzeSysMD(startingElement: Element?){

        if (startingElement is Package){
            packageName = startingElement.escapedName()?:"UnnamedPackage"

            setUpModules(startingElement)
            allModules.completeSuperclasses()

            setUpRequirements(startingElement)

            assignExpressions(startingElement)
            assignInvariants(startingElement)
            assignPorts(startingElement)

            setUpChannels(startingElement)

            allModules.adoptInheritance()

            assignUsages(startingElement)

            allChannels.forEach { channel ->
                //Inform Ports if they are bound to a TLM or Hierarchical Channel
                channel.checkTLMandHierachical()
            }

            //Classify the Expressions for the Modules
            allModules.classifyModuleExpressions()

            //Classify the Expressions for the main.cpp
            expressionClassification(
                mainExpressions,
                mainVariables,
                mainVariablesNoValues,
                mainConstants,
                mainOutputPorts,
                mainInputPorts,
                mainBidirectionalPorts
            )
        } else {
            throw Exception("The starting element used to generate SystemC templates is NOT a Package! Instead it is a\"${startingElement?.qualifiedName}\"")
        }
    }



    /**
     * Builds a SystemC Project.
     * The model is saved at the location defined in the path variable.
     * Via macros_input a BooleanArray can be passed telling which
     * SystemC AMS Macros shall be provided by the templates.
     * @param pathIn Directory, where generated SystemC Projects should be created.
     * @param tbLibFolder Directory to existing TestBenches.
     */
    fun toSystemC(pathIn: String, tbLibFolder : String) {

        var path = Path(pathIn)

        deleteFilesInDirectory(path.resolve(packageName).toFile())

        //Set up Directory for generated SystemC Files
        path = Path(setUpFolder(path, packageName))

        if (!macros[4]) {
            throw Exception("Macro 5 (void processing()) must not be disabled as it is required by TDF modules!")
        }

        //Create Header and Source file for all Modules
        val modulePath = pathToString(path.resolve("modules"))
        allModules.forEach { (_,module) ->
            if(!module.useSuperClass){
                module.writeHeaderFile(modulePath, macros, ::printRangeOrValue)
                module.writeSourceFile(modulePath, macros)
            }
        }

        //Iterate over all channels and create the Source File for Hierarchical channels
        allChannels.forEach {
            when (it.channelType) {
                ChannelType.HIERARCHICAL -> {
                    it.createHierarchicalChannel(modulePath)
                }

                ChannelType.TLM -> {
                    it.createTLMChannel(modulePath)
                }

                else -> {}
            }
        }

        //Iterates over all ports. When a TLM port is found, the function to create the files is called
        allPorts.forEach { (_, pt) ->
            if ((pt.isInitiator() == true) or (pt.isTarget() == true)) {
                pt.createTLMPort(modulePath)
            }
        }

        val pathString = pathToString(path)
        setUpTestBenches(tbOrigin = pathToString(Path(tbLibFolder)),
            tbDestination = "$pathString/testbenches")

        writeMain(pathString)

        writeCMakeLists(pathString, packageName)
        writeMakefile(pathString)

    }

    /**
     * Copies TestBenches to the directory where the generated SystemC files are stored.
     */
    private fun setUpTestBenches(tbOrigin : String, tbDestination : String){
        allRequirements.values.toList().forEach{ requirement ->
            if(requirement.generateTB){

                if(requirement.existsTB){
                    //Copy Existing TestBench to location
                    try {
                        File(tbOrigin + "/" + requirement.requirementName + "_TB.cpp").copyTo(
                            File(tbDestination + "/" + requirement.requirementName + "_TB.cpp"), overwrite = true
                        )
                    } catch (e : NoSuchFileException){
                        println(e.message)
                    } catch (e : IOException){
                        println(e.message)
                    }
                }else{
                    //Generate a TestBench Template
                    requirement.writeTestBench(tbDestination)
                }

            }
        }
        writeResultWriterCPP(tbDestination)
    }

    private fun writeResultWriterCPP(tbDestination : String){
        File("$tbDestination/ResultWriter.cpp").printWriter().use { out ->
            out.println("#include <iostream>\n" +
                    "#include <fstream>\n" +
                    "#include <list>\n" +
                    "using namespace std;\n" +
                    "\n" +
                    "typedef struct{\n" +
                    "    string reqName;\n" +
                    "    double reqResValue;\n" +
                    "    string reqResUnit;\n" +
                    "    double reqRefValue;\n" +
                    "    string reqRefUnit;\n" +
                    "    string attributeQualifiedName;\n" +
                    "    bool success;\n" +
                    "}Result;\n" +
                    "\n" +
                    "enum class Operator{\n" +
                    "    GT,GE,LT,LE,EE\n" +
                    "};\n" +
                    "\n" +
                    "class ResultWriter{\n" +
                    "\n" +
                    "private:\n" +
                    "    std::list< Result > results;\n" +
                    "\n" +
                    "public:\n" +
                    "    ResultWriter(){}\n" +
                    "\n" +
                    "    void addResult(string requirementName, double resultValue,  string resultUnit, Operator op, double referenceValue, string referenceUnit, string attributeQualifiedName){\n" +
                    "        Result newResult;\n" +
                    "        newResult.reqName = requirementName;\n" +
                    "        newResult.reqResValue = resultValue;\n" +
                    "        newResult.reqResUnit = resultUnit;\n" +
                    "        newResult.reqRefValue = referenceValue;\n" +
                    "        newResult.reqRefUnit = referenceUnit;\n" +
                    "        newResult.attributeQualifiedName = attributeQualifiedName;\n" +
                    "\n" +
                    "       switch(op){\n" +
                    "            case Operator::GT: {\n" +
                    "                newResult.success = resultValue > referenceValue;\n" +
                    "                break;\n" +
                    "            }\n" +
                    "            case Operator::GE: {\n" +
                    "               newResult.success = resultValue >= referenceValue;\n" +
                    "               break;\n" +
                    "            }\n" +
                    "            case Operator::LT: {\n" +
                    "                newResult.success = resultValue < referenceValue;\n" +
                    "                break;\n" +
                    "            };\n" +
                    "            case Operator::LE: {\n" +
                    "               newResult.success = resultValue <= referenceValue;\n" +
                    "               break;\n" +
                    "            }\n" +
                    "            case Operator::EE :{\n" +
                    "                newResult.success = resultValue == referenceValue;\n" +
                    "                break;\n" +
                    "            };\n" +
                    "        }\n" +
                    "\n" +
                    "\n" +
                    "        results.push_back(newResult);\n" +
                    "    }\n" +
                    "\n" +
                    "    void writeResultFile(){\n" +
                    "        ofstream resultFile(\"$tbDestination/results.json\");\n" +
                    "        resultFile<<\"[\"<<endl;\n" +
                    "            while(results.size() > 0){\n" +
                    "                cout<<\"Writing result for Requirement: \"<<results.back().reqName<<endl;\n" +
                    "                resultFile\n" +
                    "                <<\"\\t{\"<<endl\n" +
                    "                <<\"\\t \\\"constraintName\\\":\\\"\"<<results.back().reqName<<\"\\\"\"<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"resultValue\\\":\"<<results.back().reqResValue<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"resultUnit\\\":\\\"\"<<results.back().reqResUnit<<\"\\\"\"<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"referenceValue\\\":\"<<results.back().reqRefValue<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"referenceUnit\\\":\\\"\"<<results.back().reqRefUnit<<\"\\\"\"<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"successful\\\":\"<<results.back().success<<\",\"<<endl\n" +
                    "                <<\"\\t \\\"attributeQualifiedName\\\":\\\"\"<<results.back().attributeQualifiedName<<\"\\\"\"<<\"\"<<endl\n" +
                    "                <<\"\\t}\";\n" +
                    "                results.pop_back();\n" +
                    "                if(results.size() == 0) resultFile<<endl; else resultFile<<\",\"<<endl;\n" +
                    "            }\n" +
                    "            resultFile<<\"]\"<<endl;\n" +
                    "            resultFile.close();\n" +
                    "\n" +
                    "    }\n" +
                    "};\n")
        }
    }

    private fun pathToString(path: Path): String {
        val separator = FileSystems.getDefault().separator
        return path.toString().replace(separator, "/")
    }

    /**
     * Function to delete all files in a directory, mainly useful for tests to empty the destination folder before putting files in it
     */
    private fun deleteFilesInDirectory(dir: File) {
        for (file in dir.listFiles() ?: arrayOf()) {
            if (file.isDirectory) {
                deleteFilesInDirectory(file)
            }else if(file.isFile){
                file.delete()
            }
        }
    }

    /**
     * Called by the User Control Board to get the data of available Modules and Channels
     */
    fun getUCBData() : UcbDataPack{
        return UcbDataPack(allModules.values.toMutableList(), allChannels, macros, allRequirements.values.toMutableList(), packageName)
    }

    private fun setUpRequirements(element: Element){
        when (element){
            is RequirementUsageImplementation -> {
                allRequirements[element.path()] =
                    Requirement(element.declaredName!!.substringAfterLast("::"),
                        findDutName(element),
                        (getElementOfType(element, "FeatureImplementation") as FeatureImplementation).referencedFeature!!.path()
                    )
            }
        }
        callOnChildren(element, ::setUpRequirements)
    }

    /**
     * Finds the name of the Device that is referred to in this Requirement.
     */
    private fun findDutName(element: RequirementUsageImplementation) : String{
        element.ownedElement.forEach {
            if(it.javaClass.simpleName == "FeatureImplementation") {
                return (allModules[(it as Feature).referencedFeature!!.qualifiedName]?.moduleName ?:
                    allModules[it.referencedFeature!!.qualifiedName + "_CLASS"]?.moduleName ?:
                        throw Exception("Problem with Requirement: ${element.declaredName} - No matching Module found for subject ${it.declaredName} ")
                        )
            }
        }

        throw Exception("No DUT Subject found for Requirement ${element.declaredName}!")
    }

    /**
     * Traverses down the Hierarchy and sets up Modules for Classes and PartUsages.
     * Creates a Module for every:
     *  - Class
     *  - PartUsage (if the Class is "Base::Part")
     * @param element The starting element from which the traversal should begin.
     */
    private fun setUpModules(element: Element) {
        var module : Module? = null

        when(element){
            is ClassImplementation -> {
                module = Module(
                    moduleName = element.declaredName.toString(),
                    fullQualifiedName = element.qualifiedName!!,
                    superClassFullQualifiedName = when (element.generalization.firstOrNull()?.declaredName){
                        "Part" -> null
                        "Anything" -> null
                        else -> element.generalization.firstOrNull()?.qualifiedName
                    }
                )
            }

            is PartUsageImplementation -> {
                when(element.type.first().declaredName){
                    "Part" -> { //Is a Part that does not instance a Class (e.g.: part Car{ .. } )
                        module = Module(
                            moduleName = element.escapedName() + "_CLASS",
                            fullQualifiedName = element.qualifiedName + "_CLASS",
                            superClassFullQualifiedName = null
                        )
                    }
                    else -> { //Is a Part instances a Class (e.g.: part Car : Vehicle{ .. } )
                        module = Module(
                            moduleName = element.escapedName() + "_CLASS",
                            fullQualifiedName = element.qualifiedName + "_CLASS",
                            superClassFullQualifiedName = element.generalization.firstOrNull()?.qualifiedName
                        )
                    }
                }
            }
        }

        if (module != null) {
            allModules[module.fullQualifiedName] = module
        }

        callOnChildren(element, ::setUpModules)
    }

    /**
     * Iterates all Modules and adds information about the SuperClass.
     */
    private fun HashMap<String, Module>.completeSuperclasses() {
        this.forEach { (_, module) ->
            module.superClassModule = this[module.superClassFullQualifiedName]
        }
    }

    private fun HashMap<String, Module>.adoptInheritance(){
        this.forEach { (_, module) ->

            if(module.superClassModule != null){ //If it has a proper SuperClass Module, check if it overrides it
                module.expressions.forEach { expression ->
                    if(!expression.isTransient or !module.superClassModule!!.expressions.containsByName(expression) or allPorts.contains(expression.qualifiedName)) {
                        // If this class introduces this expression, this Class overrides the superclass;
                        // therefore, we do not use the superClass in this case
                        module.useSuperClass = false
                    }else{
                        module.ignoredExpressions.add(expression) //This Expression is exactly the same as in SuperClass, therefore ignore it this SubClass
                    }
                }


                //A Class that was explicitly defined via a part def should always be directly used and not the super class.
                if(!module.moduleName.endsWith("_CLASS")) module.useSuperClass = false

                //Do no use superClass if ports between Sub- and SuperClass do not match
                if(module.let { return@let it.inputPorts.size + it.outputPorts.size + it.bidirectionalPorts.size } !=
                    module.superClassModule.let{ return@let it!!.inputPorts.size + it.outputPorts.size + it.bidirectionalPorts.size}) module.useSuperClass = false

            }else{
                //It cannot use a superClass if it has none! Therefor set flag to false
                module.useSuperClass = false
            }
        }
    }

    private fun expressionClassification(
        expressions: MutableList<Feature>,
        variables : MutableList<Variable>,
        variablesNoValues : MutableList<VariableNoValues>,
        constants : MutableList<Constant>,
        outputPorts : MutableList<Port>,
        inputPorts : MutableList<Port>,
        bidirectionalPorts : MutableList<Port>
    ) {

        expressions.forEach { expression ->
            assert(expression !is com.github.tukcps.sysmd.cspsolver.Variable)
            allPorts[expression.qualifiedName]?.let {
                when (it.portType){
                    PortType.SOURCE -> outputPorts.add(it.apply {  it.createdFromExpression = true})
                    PortType.TARGET-> inputPorts.add(it.apply {  it.createdFromExpression = true})
                    PortType.BIDIRECTIONAL -> bidirectionalPorts.add(it.apply {  it.createdFromExpression = true})
                }
            } ?: when {
                expression.model!!.repo.realType in (expression as Type).allSupertypes(true) -> {
                    if(isVariableWithoutValues(expression)){
                        variablesNoValues.add(VariableNoValues(expression, DataType.REAL))
                    }else if(isVariable(expression)){
                        variables.add(Variable(expression, DataType.REAL, ::dependencyStringToMinMax))
                    }else{
                        constants.add(Constant(expression, DataType.REAL, ::dependencyStringToMinMax))
                    }
                }
                expression.model!!.repo.integerType in (expression as Type).allSupertypes(true) -> {
                    if(isVariableWithoutValues(expression)){
                        variablesNoValues.add(VariableNoValues(expression, DataType.INT))
                    }else if(isVariable(expression)){
                        variables.add(Variable(expression, DataType.INT, ::dependencyStringToMinMax))
                    }else{
                        constants.add(Constant(expression, DataType.INT, ::dependencyStringToMinMax))
                    }
                }
                expression.model!!.repo.stringType in (expression as Type).allSupertypes(true) -> {
                    if(isVariableWithoutValues(expression)){
                        variablesNoValues.add(VariableNoValues(expression, DataType.STRING))
                    }else{
                        variables.add(Variable(expression, DataType.STRING, ::dependencyStringToMinMax))
                    }
                }
                expression.model!!.repo.booleanType in (expression as Type).allSupertypes(true) -> {
                    if(isVariableWithoutValues(expression)){
                        variablesNoValues.add(VariableNoValues(expression, DataType.BOOLEAN))
                    }else{
                        variables.add(Variable(expression, DataType.BOOLEAN, ::dependencyStringToMinMax))
                    }
                }
                else -> throw SysMDFatalInternalError("Unsupported type of expression: ${expression.declaredName}")
            }
        }
    }

    /**
     * Iterates all Modules and adds decides if an Expression is a variable, constant or a Port.
     */
    private fun HashMap<String, Module>.classifyModuleExpressions() {
        this.forEach { (_, module) ->
            module.expressions.removeAll(module.ignoredExpressions)

            expressionClassification(
                expressions = module.expressions,
                variables =  module.variables,
                variablesNoValues = module.variablesNoValues,
                constants = module.constants,
                inputPorts = module.inputPorts,
                outputPorts = module.outputPorts,
                bidirectionalPorts = module.bidirectionalPorts)
        }
    }



    /**
     * Sets up Channels and adds the Ports to it.
     * @param element The starting element from which the traversal should begin.
     */
    private fun setUpChannels(element: Element) {
        run {
            val signal = element.model?.global?.resolve<Element>("Signals::Signal")
            val complexSignal = element.model?.global?.resolve<Element>("Signals::ComplexSignal")
            val bus = element.model?.global?.resolve<Element>("Signals::Bus")
            when(element){
                is ConnectorImplementation -> {
                    //Create a Channel and add it to allChannels list
                    allChannels.add(
                        Channel(
                            channelName = element.escapedName().toString(),
                            channelType = when  {
                                signal in element.type.map { it }       -> ChannelType.PRIMITIVE
                                complexSignal in element.type.map { it } -> ChannelType.HIERARCHICAL
                                bus in element.type.map { it }-> ChannelType.TLM
                                else -> throw SysMDFatalInternalError("No matching channel type found for: ${element.declaredName}")
                            }
                        ).apply {
                            //Add the Ports to the Channel
                            element.from.forEach { outputPort ->
                                val unref = (outputPort as Feature).referencedFeature?:outputPort
                                if (allModules.contains(unref.owner?.qualifiedName)) return@run
                                addPortToChannel(unref, this.outputPorts, this, PortType.SOURCE)
                            }
                            element.to.forEach { inputPort ->
                                val unref = (inputPort as Feature).referencedFeature?:inputPort
                                if (allModules.contains(unref.owner?.qualifiedName)) return@run
                                addPortToChannel(unref, this.inputPorts, this, PortType.TARGET)
                            }

                            //Now all Ports are in Channel - Determine the Channel DataType
                            this.determineChannelDataType()



                            //Searches the Module where the Channel should be instantiated in and also checks that
                            (this.inputPorts + this.outputPorts).let { ports ->
                                ports.forEach { port ->
                                    val topModule = port.module!!.fullQualifiedName.substringBeforeLast("::")

                                    //Add Channel to Module and avoid adding duplicates
                                    allModules[topModule].let { mod1 ->
                                        mod1?.let { if(this !in mod1.channels) mod1.channels.add(this) }  ?: allModules[topModule + "_CLASS"].let { mod2 ->
                                            mod2?.let { if(this !in mod2.channels) mod2.channels.add(this) } ?: (
                                                    if(!mainChannels.contains(this))
                                                        mainChannels.add(this)
                                                    else {}
                                                    )
                                        }
                                    }
                                }
                            }

                        }
                    )
                }
                else -> {}
            }
        }

        callOnChildren(element, ::setUpChannels)
    }

    /**
     * Traverses down the Hierarchy and adds every found Expressions to the corresponding Module.
     * @param element The starting element from which the traversal should begin.
     */
    private fun assignExpressions(element: Element) {

        if(element is Feature
            && element.isFeatureWithValue()
            && element !is Multiplicity
            && element.referencedFeature == null
            && element.variable != null
            )  {

            assert(element.variable != null)

            allModules[element.path().substringBeforeLast("::")].let { mod1 ->
                mod1?.expressions?.add(element)
                    ?: allModules[element.owner?.qualifiedName + "_CLASS"].let { mod2 ->
                        mod2?.expressions?.add(element) ?: allRequirements[element.path().substringBeforeLast("::")].let { requirement ->
                            requirement?.constraints?.add(
                                    Constraint(
                                        constraintName = element.declaredName.toString(),
                                        attributeQUalifiedName = requirement.fullQualifiedName + "::" + element.variable!!.ast!!.leaves.toList()[0].feature!!.declaredName,
                                        unit = element.variable!!.ast!!.leaves.toList()[0].variable!!.unitSpec,
                                        statement = element.expression!!,
                                        operator = (element.variable!!.ast!!.dependency as AstBinOp).op.name,
                                        referenceValue = (element.variable!!.ast!!.dependency as AstBinOp).r.upQuantity.let { it.valuesIn(it.unitSpec)[0].asAadd().max },
                                        referenceUnit = (element.variable!!.ast!!.dependency as AstBinOp).r.upQuantity.unitSpec
                                    )
                            ) ?: mainExpressions.add(element)
                        }
                    }
            }
        }

        //Go down hierarchy (but not if we are at a Port)
        if(element !is PortUsageImplementation && element is Feature) callOnChildren(element,::assignExpressions)
    }

    private fun assignInvariants(element: Element) {

        if(element.javaClass.simpleName == "InvariantImplementation"){

            element as InvariantImplementation

            allRequirements[element.path().substringBeforeLast("::")].let { requirement ->
                requirement?.invariants?.add(
                    Invariant(
                        invariantName = element.escapedName().toString(),
                        invariantString = element.expression?:""
                    )
                ) ?: throw SysMDFatalInternalError("No Requirement found to add Invariant ${element.escapedName()} to.")
            }
        }

        callOnChildren(element, ::assignInvariants)
    }

    /**
     * Traverses down the Hierarchy and creates a Port for every PortUsageImplementation and adds it to the corresponding Module.
     * @param element The starting element from which the traversal should begin.
     */
    private fun assignPorts(element: Element) {

        if(element.javaClass.simpleName == "PortUsageImplementation"){
            element as PortUsageImplementation

            //Create Port Object
            Port(
                portName = element.declaredName.toString(),
                fullQualifiedName = element.path(),
                portType = translateToPortType(element.direction),
                dataType = DataType.REAL,
                isInherited = element.isTransient,
                module = allModules[element.path().substringBeforeLast("::")].let { it1 ->
                   it1 ?: allModules[element.path().substringBeforeLast("::") + "_CLASS"].let { it2 ->
                       it2 ?: throw SysMDFatalInternalError("No Module found for Port: ${element.declaredName}")
                   }
                }
            ).let {
                //Add the port to the corresponding port list of the module the Port belongs to
                when (it.portType){
                    PortType.SOURCE -> it.module!!.outputPorts.add(it)
                    PortType.TARGET -> it.module!!.inputPorts.add(it)
                    PortType.BIDIRECTIONAL -> it.module!!.bidirectionalPorts.add(it)
                }

                //Add Port to allPorts Map
                allPorts[it.fullQualifiedName] = it
           }
        }

        //Go down hierarchy (but not if we are at a Port)
        if (element !is PortUsageImplementation)
            callOnChildren(element,::assignPorts)
    }

    /**
     * Traverses down the Hierarchy and creates searches for PartUsages.
     * If a PartUsage is found it is added to the main usages or the subModules list of the corresponding Module.
     * @param element The starting element from which the traversal should begin.
     */
    private fun assignUsages(element: Element) {

        if(element is PartUsageImplementation) {

            val usage : Usage

            if ("Parts::Part" !in element.type.map {  it.qualifiedName }) {
                //Create Usage for Parts that instantiate a proper Class
                usage = Usage(
                    instanceName = element.escapedName().toString(),
                    className = "", //The class name is set down in the apply{} scope
                    amount = (element.multiplicityRange.max.toInt()),
                    module = allModules[element.qualifiedName + "_CLASS"].let { mod1 ->
                        (if(mod1?.useSuperClass == true) mod1.superClassModule else mod1) ?:allModules[element.type.first().qualifiedName].let { mod2 ->
                            mod2 ?: allModules[element.type.first().qualifiedName + "_CLASS"].let { mod3 ->
                                mod3 ?: throw SysMDFatalInternalError("No Module found for Usage: ${element.declaredName}")
                            }
                        }
                    }
                ).apply {
                    this.module.moduleUsages.add(this) //Add this Usage to the module
                    className = this.module.moduleName //Set the class name using the actual class module
                }
            }else{
                //Create Usage for Parts that have no Class defined in the SysMD model
                usage = Usage(
                    instanceName = element.escapedName().toString(),
                    className = element.name.toString() + "_CLASS",
                    amount = (element.multiplicityRange.max.toInt()),
                    module = allModules[element.qualifiedName + "_CLASS"].let { mod1 ->
                            mod1 ?: throw SysMDFatalInternalError("No Module found for Usage: ${element.declaredName}")
                        }
               ).apply { this.module.moduleUsages.add(this) } //Add this Usage to the module
            }

            if ( element.owner is PackageImplementation ) {
                mainUsages.add(usage.apply { instanceLocation = "MAIN" })
            } else {
                allModules[element.path().substringBeforeLast("::")].let { mod1 ->
                    mod1?.subModules?.add(usage.apply { instanceLocation = mod1.fullQualifiedName })
                        ?: allModules[element.path().substringBeforeLast("::") + "_CLASS"].let { mod2 ->
                            mod2?.subModules?.add(usage.apply { instanceLocation = mod2.fullQualifiedName })
                                ?: throw SysMDFatalInternalError("No Module found to add this Usage to: ${element.declaredName}")
                        }
                }
            }

        }

        //Go down hierarchy (but not if we are at a Port)
        if(element !is PortUsageImplementation)
            callOnChildren(element,::assignUsages)
    }

    /**
     * Takes an Element and a function and calls the function on every Child Element of the ownedElements list.
     * @param element The Element on whose children the function should be called on.
     * @param function The function that is called on every Child Element.
     */
    private fun callOnChildren(element: Element, function: KFunction1<Element, Unit>){
        if(element.ownedElement.isNotEmpty()){
            element.ownedElement.forEach {
                function(it)
            }
        }
    }



    /**
     * Adds a Port to a Channel list.
     * In addition, the Port is informed to which Channel it connects.
     * After this function, the Channel and the Port both bidirectionally know about its togetherness.
     * @param port The Port which should be added to the list.
     * @param portList The port list of the Channel the Port should be added to.
     * @param channel The Channel object - needed to tell the Port it connects to this Channel.
     */
    private fun addPortToChannel (
        port: Element,
        portList: MutableList<Port>,
        channel: Channel,
        portType: PortType
    ){
        when (port) {
            is PortUsageImplementation -> {
                //There already exists a Port Object - Retrieve it from the allPorts Map and add it to the portList
                portList.add(
                    allPorts[port.qualifiedName]?.apply{
                        associatedChannels.add(channel) //Inform this port about the Channel it connects to
                    } ?:
                    throw SysMDFatalInternalError("There was no Port Object found for: ${port.declaredName}")
                )
            }

            is Feature -> {
                //There exists no Port object yet - Create one and add it to the portList
                portList.add(
                    Port(
                        portName = port.escapedName().toString(),
                        fullQualifiedName = port.qualifiedName!!,
                        portType = portType,
                        dataType = port.ownedSpecialization.firstOrNull()!!.toDataType(),
                        isInherited = port.isImpliedIncluded,
                        module = allModules[port.owner?.qualifiedName].let { module1 ->
                            module1 ?: allModules[port.owner?.qualifiedName + "_CLASS"].let { module2 ->
                                module2
                                    ?: throw SysMDFatalInternalError("No Module found with Full Qualified Name \"${port.owner?.qualifiedName}\" " +
                                        "was found for Port \"${port.escapedName().toString()}\"")
                            }
                        }
                    ).apply {
                        this.associatedChannels.add(channel) //Inform the Port about the Channel it connects to
                        allPorts[this.fullQualifiedName] = this //Add this newly created Port to the allPorts Map
                    }
                )
            }

            else -> throw SysMDFatalInternalError("The Element ${port.escapedName()} was neither a PortUsage nor an ExpressionImplementation" +
                    "and therefor could not be added to Channel ${channel.channelName}.")
        }
    }



    /**
     * Writes the main file with the sc_main() function as mounting point for SystemC
     */
    private fun writeMain(path: String) {
        File("$path/main.cpp").printWriter().use { out ->

            //##### PRINT INCLUDES #################################################################
            out.println("#include <systemc>\n#include <systemc-ams>\n#include <string>\n\nusing namespace std;\n")

            mainUsages.forEach { usage->
                if(usage.amount > 0){
                    out.println("#include \"$modulesFolder${usage.className}.h\"")
                }
            }

            mainChannels.forEach {
                if (it.channelType == ChannelType.TLM) {
                    out.println("#include $modulesFolder\"TLM_${it.channelName}.h\"")
                }
                if (it.channelType == ChannelType.HIERARCHICAL) {
                    out.println("#include $modulesFolder\"${it.channelName}_class.h\"")
                }
            }
            out.println("\nint sc_main(int argc, char* argv[])\n{\n\n")


            mainConstants.forEachIndexed { idx, constant ->
                if(idx == 0) out.print("\n\n\t//\t### Constants ###")
                constant.write(out)
            }

            mainVariables.forEachIndexed { idx, variable ->
                if(idx == 0) out.print("\n\n\t//\t### Variables with values defined in SysMD ###")
                variable.writeForMain(out, ::printRangeOrValue)
            }

            mainVariablesNoValues.forEachIndexed {  idx, variableNoValues ->
                if(idx == 0) out.print("\n\n\t//\t### Variables with values NOT defined in SysMD ###")
                variableNoValues.writForMain(out)
            }

            //##### PRINT ALL CHANNELS #############################################################
            if(mainChannels.isNotEmpty()) out.println("\n\n\t//\t### Channels ###")
            mainChannels.forEach { ch ->
                when (ch.channelType) {
                    ChannelType.PRIMITIVE -> {
                        out.println("\tsca_tdf::sca_signal<${ch.channelDataType!!.toCPPDataType()}> ${ch.channelName}(\"${ch.channelName}\");")
                    }

                    ChannelType.TLM -> {
                        out.println(
                            "\tTLM_${ch.channelName}<${ch.inputPortsBindingLimit},${ch.outputPortsBindingLimit}> " +
                                    "${ch.channelName}(\"${ch.channelName}\");"
                        )
                    }

                    ChannelType.HIERARCHICAL -> {
                        out.println("\t${ch.channelName}_class ${ch.channelName}(\"${ch.channelName}\");")
                    }

                }
            }


            //##### INSTANTIATION OF MODULES ########################################################
            if (mainUsages.isNotEmpty()) out.println("\n\n\t//\t### Modules ###")
            mainUsages.forEach { usage ->

                //If the module has usages, instantiate it accordingly to them
                if (usage.amount > 0) {
                    out.print("\t${usage.className} ")

                    if (usage.amount == 1) {
                        out.print("${usage.instanceName}(\"${usage.instanceName}\");\n")
                    } else {
                        //usage.moduleUsages.forEach { u ->
                            for (i in 0 until usage.amount) {
                                out.print("${usage.instanceName}_${i}(\"${usage.instanceName}_${i}\")${(if (i == usage.amount - 1) ";\n" else ", ")}")
                            }
                        //}
                    }
                }
            }


            //##### PORT BINDING #################################################################
            if(mainChannels.isNotEmpty()) out.println("\n\n\t//\t### Port binding ###")
            mainChannels.forEach { channel ->
                channel.printPortBinding("MAIN", out)
            }


            out.print("\n\tsca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file(\"output\");\n")

            mainUsages.forEach { usage ->
                for(i in 0..< usage.amount) {
                    usage.module.channels.forEach { ch ->
                        if (ch.trace) {
                            if (usage.amount == 1) {
                                out.print("\n\tsca_trace(tr, ${usage.instanceName}.${ch.channelName} ,\"${ch.channelName}\");")
                            } else {
                                out.print("\n\tsca_trace(tr, ${usage.instanceName}_$i.${ch.channelName} ,\"${ch.channelName}\");")
                            }
                        }
                    }
                }
            }

            out.print("\n\n\tsca_close_vcd_trace_file(tr);")

            out.println("\n\n\tsc_core::sc_start();")

            out.println("\n\n\treturn 0;\n}")
        }
    }




    /**
     * Function to create the makefile with clean function
     */
    private fun writeMakefile(path: String) {
        File("$path/Makefile").printWriter().use { out ->
            //Print variables for SystemC and AMS Path
            out.print(
                "## Variable that points to SystemC installation path\n" +
                        "SYSTEMC = $(SYSTEMC_PATH)\n\n" +

                        "## Variable that points to SystemC-AMS installation path\n" +
                        "SYSTEMCAMS = $(SYSTEMC_AMS_PATH)\n\n"
            )

            //Print Target Arch
            out.print("ifndef TARGET_ARCH\n" +
                    "## select target architecture\n" +
                    "#TARGET_ARCH  = linux\n" +
                    "TARGET_ARCH = linux64\n" +
                    "#TARGET_ARCH = gccsparcOS5\n" +
                    "#TARGET_ARCH = cygwin\n" +
                    "#TARGET_ARCH = mingw32\n" +
                    "endif\n\n")


            //Print Compiler Options
            out.print(
                "#Compiler Options:\n" +
                        "CC = g++\n\n"
            )

            //Print the variables for the Include and Lib directory
            out.print(
                "INCDIR = -I. -I$(SYSTEMC)/include -I$(SYSTEMCAMS)/include\n" +
                        "LIBDIR = -L$(SYSTEMC)/lib-$(TARGET_ARCH) -L$(SYSTEMCAMS)/lib-$(TARGET_ARCH)\n" +
                        "LIBS   =   $(EXTRA_LIBS) -lsystemc-ams -lsystemc -lm\n\n"
            )

            //Print the final Output file creation
            out.print("final.exe: ")
            allModules.forEach { (_,module) ->
                out.print("$modulesFolder${module.moduleName}.o ")
            }
            out.print("main.o ")
            out.print(
                "\n" +
                        "\t\t$(CC) $(INCDIR) $(LIBDIR) "
            )
            allModules.forEach { (_,module) ->
                out.print("$modulesFolder${module.moduleName}.o ")
            }
            out.print("main.o ")
            out.print("-o final.exe $(LIBS) 2>&1 | c++filt\n")

            //Print Build line for every Requirement
            allRequirements.values.forEach { requirement ->
                out.print("\nbuild_${requirement.requirementName}_TB.exe: ")
                allModules.forEach { (_,module) ->
                    out.print("$modulesFolder${module.moduleName}.o ")
                }
                out.print("$testbenchesFolder${requirement.requirementName}_TB.o ")
                out.print(
                    "\n" +
                            "\t\t$(CC) $(INCDIR) $(LIBDIR) "
                )
                allModules.forEach { (_,module) ->
                    out.print("$modulesFolder${module.moduleName}.o ")
                }
                out.print("$testbenchesFolder${requirement.requirementName}_TB.o ")
                out.print("-o $testbenchesFolder${requirement.requirementName}_TB.exe $(LIBS) 2>&1 | c++filt\n")
            }

            //Print for the main.cpp
            out.print(
                "\n" +
                        "main.o: main.cpp\n" +
                        "\t\t $(CC) $(INCDIR) -c main.cpp -o main.o\n"
            )


            //Print creation of Object files from Source File for all Modules
            allModules.forEach { (_,module) ->
                out.print(
                    "\n" +
                            "$modulesFolder${module.moduleName}.o: $modulesFolder${module.moduleName}.cpp\n" +
                            "\t\t $(CC) $(INCDIR) -c $modulesFolder${module.moduleName}.cpp -o $modulesFolder${module.moduleName}.o\n"
                )
            }

            allRequirements.values.forEach { requirement ->
                out.print(
                    "\n" +
                            "$testbenchesFolder${requirement.requirementName}_TB.o: $testbenchesFolder${requirement.requirementName}_TB.cpp\n" +
                            "\t\t $(CC) $(INCDIR) -c $testbenchesFolder${requirement.requirementName}_TB.cpp -o $testbenchesFolder${requirement.requirementName}_TB.o\n"
                )
            }

            allRequirements.values.forEach { requirement ->
                out.print(
                    "\n" +
                            "run_${requirement.requirementName}_TB:\n" +
                            "\t\t./testbenches/${requirement.requirementName}_TB.exe\n"
                )
            }


            //Print the Clean Function
            out.print("\nclean:\n" +
                    "\trm -f *.o final.exe\n" +
                    "\trm -f modules/*.o")
        }
    }


    /**
     * Function to create the CMakeLists file
     */
    private fun writeCMakeLists(path: String, projectName: String) {
        File("$path/CMakeLists.txt").printWriter().use { out ->
            out.print(
                "cmake_minimum_required(VERSION 3.0)\n" +
                        "project($projectName)\n" +
                        $$"set (CMAKE_CXX_STANDARD ${SystemC_CXX_STANDARD})\n\n" +
                        "set (SystemC_include_path \"/usr/local/systemc-2.3.3/include/\")\n" +
                        "set (SystemC_AMS_include_path \"/usr/local/systemc-ams-2.3/include\")\n" +
                        "set (SystemC_lib_path \"/usr/local/systemc-2.3.3/lib-linux64/libsystemc.a\")\n" +
                        "set (SystemC_AMS_lib_path \"/usr/local/systemc-ams-2.3/lib-linux64/libsystemc-ams.a\")\n\n" +
                        "add_executable($projectName main.cpp "
            )
            allModules.forEach { (_,module) -> out.print("modules/${module.moduleName}.cpp ") }
            out.println(")")
            out.print(
                $$"target_include_directories($$packageName PRIVATE ${SystemC_include_path})\n" +
                        $$"target_include_directories($$packageName PRIVATE ${SystemC_AMS_include_path})\n" +
                        $$"target_link_libraries($$packageName ${SystemC_lib_path} ${SystemC_AMS_lib_path})"
            )
        }
    }

    /**Sets up the folder structure for a generated SystemC project.
     * Generates a folder with the name of the Package the SystemC files are generated from
     * and adds a modules and testbench folder inside.
     * @param systemCProjectPath Path of the directory in which the SystemC Project should be placed.
     * @param packageName The name for the folder in which the SystemC files for this project should be saved.
     * @return Returns the path to the SystemC project folder for this package.
     */
    private fun setUpFolder(systemCProjectPath : Path, packageName : String) : String{
        var path = systemCProjectPath

        try {
            //Set up Directory for generated SystemC Files
            if(Files.notExists(path)){
                println("SystemC Folder Setup - Creating SystemC Export folder: $path")
                Files.createDirectory(path)
                println("SystemC Folder Setup - Successfully created Export folder: $path")
            }else{
                println("SystemC Folder Setup - Already Exists: $path")
            }

            path = path.resolve(packageName)

            //Set up Directory for SysC Project
            if(Files.notExists(path)){
                println("SystemC Folder Setup - Creating project directory: $path")
                Files.createDirectory(path)
                println("SystemC Folder Setup - Successfully created project folder: $path")
            }else{
                println("SystemC Folder Setup - Already Exists: $path")
            }

            val modulesFolder = path.resolve("modules")

            //Set up Directory for Modules
            if(Files.notExists(modulesFolder)){
                println("SystemC Folder Setup - Creating module directory: $modulesFolder")
                Files.createDirectory(modulesFolder)
                println("SystemC Folder Setup - Successfully created module folder: $modulesFolder")
            }else{
                println("SystemC Folder Setup - Already Exists: $modulesFolder")
            }

            val testBenchFolder = path.resolve("testbenches")

            //Set up Directory for TestBenches
            if(Files.notExists(testBenchFolder)){
                println("SystemC Folder Setup - Creating test-bench directory: $testBenchFolder")
                Files.createDirectory(testBenchFolder)
                println("SystemC Folder Setup - Successfully created test-bench folder: $testBenchFolder")
            }else{
                println("SystemC Folder Setup - Already Exists: $testBenchFolder")
            }
        }
        catch (e : Exception){
            println("Problem when setting up folder structure for generated SystemC Templates.\n Exception: ${e.message}")
        }

        return path.toString()
    }

    private fun printRangeOrValue(
        min: String?,
        max: String?,
        singleValue: String?,
        unit: String,
        dataType: DataType
    ): String {
        return when(dataType) {
            DataType.REAL ->  "// Unit: ${(unit).padEnd(3)} ;SysMD Value: [$min .. $max]"
            DataType.INT ->  "// Unit: ${(unit).padEnd(3)} ;SysMD Value: [$min .. $max]"
            DataType.STRING, DataType.BOOLEAN  ->  "// SysMD Value: $singleValue"
        }
    }

}
