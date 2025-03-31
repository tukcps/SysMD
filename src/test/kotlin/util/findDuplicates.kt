package util

import com.github.tukcps.sysmd.model.kerml.Element


/**
 * Computes the elements that are in both lists, by using the elementId for identification.
 * @param elements1 first list of elements
 * @param elements2 second list of elements
 * @return list of elements that are both in elements1 and elements2
 */
fun findDuplicatesById(elements1: List<Element>, elements2: List<Element>): MutableList<Element> {

    val map = elements1.map { it.elementId to it }.toMap()

    val result = mutableListOf<Element>()
    elements2.forEach {
        if (it.elementId in map.keys)
            result.add(it)
    }
    return result
}


/**
 * Computes the elements that are in both lists, by using the elementId for identification.
 * @param elements1 first list of elements
 * @param elements2 second list of elements
 * @return list of elements that are both in elements1 and elements2
 */
fun findDifferenceById(elements1: Collection<Element>, elements2: Collection<Element>): MutableList<Element> {

    val map = elements1.map { it.elementId to it }.toMap()

    val result = mutableListOf<Element>()
    elements2.forEach {
        if (it.elementId !in map.keys)
            result.add(it)
    }
    return result
}