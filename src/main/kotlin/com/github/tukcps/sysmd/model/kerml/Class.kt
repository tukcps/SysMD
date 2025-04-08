package com.github.tukcps.sysmd.model.kerml


interface Class: Classifier {

    /**
     * A Class specializes "Occurrences::Occurrence
     */
    override fun checkConstraints() {
        super.checkConstraints()
        val occurrence = model?.repo?.occurrence
        var ok = false
        allSupertypes().forEach {
            if (it.specializes(occurrence))
                ok = true
        }
        if (!ok)
            model?.status?.info("Class '${this.qualifiedName}' must specialize Occurrences::Occurrence", element = this)
    }
}