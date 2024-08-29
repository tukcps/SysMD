package com.github.tukcps.sysmd.services

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.services.session.Session


/**
 * Updates the elementId if the element is a library resp. standard element.
 * Then, it is replaced by a UUID v5 computed from its qualified name.
 * @param isLibrary Boolean that is true if Namespace is part of a library
 * @param isStandard Boolean that is true if the element is part of the SysML v2 standard
 * @param ownerQualifiedName the name of the owning Namespace
 */
fun <T: Element> T.ifLibraryElement(isLibrary: Boolean, ownerQualifiedName: QualifiedName, isStandard: Boolean = false): T {
    if (isLibrary) {
        if (this is Namespace) {
            this.isLibraryElement = true
            this.isStandard = isStandard
        }
        this.declaredName = declaredName?:elementType // What if we have no name, e.g., Specialization?
        elementId = Generators.nameBasedGenerator().generate("$ownerQualifiedName::$declaredName")
    }
    updated = false
    return this
}

/**
 * The reflective library KerML::Root, Core, Kernel provides the KerML element classes.
 * As such, they cannot be instantiated directly by model, but are hard-wired into the session.
 */
fun Session.createKerMLIntrospectionLibrary() {

    val kerMLpackage = create(PackageImplementation(declaredName = "KerML", isStandard = true), global)
    val rootPackage = create(PackageImplementation(declaredName = "Root", owner = Resolved(kerMLpackage), isStandard = true), kerMLpackage)
    val corePackage = create(PackageImplementation(declaredName = "Core", owner = Resolved(kerMLpackage), isStandard = true), kerMLpackage)
    val kernelPackage = create(PackageImplementation(declaredName = "Kernel", owner = Resolved(kerMLpackage), isStandard = true), kerMLpackage)

    val relationship = RelationshipImplementation().ifLibraryElement(true, "KerML::Root", true)
    val annotation = AnnotatingElementImplementation().ifLibraryElement(true, "KerML::Root", true)
    val textualRepresentation = TextualRepresentationImplementation().ifLibraryElement(true, "KerML::Root", true)
    val comment = CommentImplementation().ifLibraryElement(true, "KerML::Root", true)
    val documentation = DocumentationImplementation().ifLibraryElement(true, "KerML::Root", true)
    val element = ElementImplementation().ifLibraryElement(true, "KerML::Root", true)
    val namespace = NamespaceImplementation().ifLibraryElement(true, "KerML::Root", true)
    create(relationship, rootPackage)
    create(annotation, rootPackage)
    create(documentation, rootPackage)
    create(textualRepresentation, rootPackage)
    create(comment, rootPackage)
    create(namespace, rootPackage)
    create(element, rootPackage)

    val type = TypeImplementation().ifLibraryElement(true, "KerML::Core", true)
    val klassifier = ClassifierImplementation().ifLibraryElement(true, "KerML::Core", true)
    val specialization = SpecializationImplementation().ifLibraryElement(true, "KerML::Core", true)
    specialization.general = Resolved(any)
    specialization.specific = Resolved(type)
    val feature = FeatureImplementation().ifLibraryElement(true, "KerML::Core", true)
    val featureTyping = FeatureTypingImplementation().ifLibraryElement(true, "KerML::Core", true)
    create(specialization, corePackage)
    create(type, corePackage)
    create(feature, corePackage)
    create(featureTyping, corePackage )
    create(klassifier, corePackage)

    val dataType = DataTypeImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    create(dataType, kernelPackage)
    create(SpecializationImplementation(owner = Resolved(dataType), general = Resolved(any), specific = Resolved(dataType)), dataType)

    val klass = ClassImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    // val multiplicity = MultiplicityImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    val association = AssociationImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    // val expression = ExpressionImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    val pkg = PackageImplementation().ifLibraryElement(true, "KerML::Kernel", true)
    create(klass, kernelPackage)
    // create(multiplicity, kernelPackage)
    create(association, kernelPackage)
    // create(expression, kernelPackage)
    create(pkg, kernelPackage)
}