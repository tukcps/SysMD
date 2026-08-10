package com.github.tukcps.sysmd.model.util

inline fun<T> MutableList<T>.mapInPlace(mutator: (T) -> T?)
{
    val iter = listIterator()

    while(iter.hasNext())
    {
        val old = iter.next()
        val new = mutator(old)

        when {
            new === old -> continue
            new === null -> iter.remove()
            else -> iter.set(new)
        }
    }
}