package com.github.tukcps.sysmd.model.kerml

/**
 * An import that imports all members of another namespace.
 */
interface NamespaceImport: Import {
    var importedNamespace: Namespace
}