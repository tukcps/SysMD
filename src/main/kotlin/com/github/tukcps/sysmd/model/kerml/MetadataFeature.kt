package com.github.tukcps.sysmd.model.kerml

interface MetadataFeature: Feature, AnnotatingElement {
    override fun clone(): MetadataFeature
}