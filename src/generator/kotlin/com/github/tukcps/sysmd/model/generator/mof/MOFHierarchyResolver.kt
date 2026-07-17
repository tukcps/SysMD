package com.github.tukcps.sysmd.model.generator.mof

/**
 * Resolves MOF inheritance hierarchies and inherited members.
 */
class MOFHierarchyResolver(
    private val model: MOFMetaModel
) {

    /**
     * Returns the direct superclasses of a metaclass.
     *
     * @param clazz Metaclass.
     * @return Direct superclasses.
     */
    fun superClasses(clazz: MOFClass): List<MOFClass> =
        clazz.superClassIds.mapNotNull(model::findClassById)

    /**
     * Returns all transitive superclasses in depth-first inheritance order.
     *
     * @param clazz Metaclass.
     * @return Transitive superclasses.
     */
    fun allSuperClasses(clazz: MOFClass): List<MOFClass> {
        val result = linkedMapOf<String, MOFClass>()

        fun collect(current: MOFClass) {
            superClasses(current).forEach { superClass ->
                if (result.putIfAbsent(superClass.id, superClass) == null)
                    collect(superClass)
            }
        }

        collect(clazz)
        return result.values.toList()
    }

    /**
     * Returns all transitive superclasses, base classes first.
     *
     * @param clazz Metaclass.
     * @return Transitive superclasses.
     */
    fun baseFirstSuperClasses(clazz: MOFClass): List<MOFClass> {
        val result = linkedMapOf<String, MOFClass>()

        fun collect(current: MOFClass) {
            superClasses(current).forEach { superClass ->
                collect(superClass)
                result.putIfAbsent(superClass.id, superClass)
            }
        }

        collect(clazz)
        return result.values.toList()
    }

    /**
     * Returns all transitive superclasses ordered by inheritance distance.
     *
     * Classes with the same distance retain metamodel order.
     *
     * @param clazz Metaclass.
     * @return Transitive superclasses, nearest classes first.
     */
    fun nearestFirstSuperClasses(clazz: MOFClass): List<MOFClass> {
        val result = mutableListOf<MOFClass>()
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque(superClasses(clazz))

        while (queue.isNotEmpty()) {
            val superClass = queue.removeFirst()

            if (!visited.add(superClass.id))
                continue

            result += superClass
            queue.addAll(superClasses(superClass))
        }

        return result
    }

    /**
     * Returns the complete hierarchy, base classes first.
     *
     * @param clazz Metaclass.
     * @return Hierarchy including the metaclass itself.
     */
    fun hierarchy(clazz: MOFClass): List<MOFClass> =
        baseFirstSuperClasses(clazz) + clazz

    /**
     * Returns all inherited attributes of a metaclass.
     *
     * @param clazz Metaclass.
     * @return Inherited attributes.
     */
    fun inheritedAttributes(clazz: MOFClass): List<MOFAttribute> =
        baseFirstSuperClasses(clazz)
            .flatMap { it.attributes }
            .distinctBy { it.id }

    /**
     * Returns all inherited operations of a metaclass.
     *
     * @param clazz Metaclass.
     * @return Inherited operations.
     */
    fun inheritedOperations(clazz: MOFClass): List<MOFOperation> =
        baseFirstSuperClasses(clazz)
            .flatMap { it.operations }
            .distinctBy { it.id }
}