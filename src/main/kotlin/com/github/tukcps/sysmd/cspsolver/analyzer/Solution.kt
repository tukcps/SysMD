package com.github.tukcps.sysmd.cspsolver.analyzer

@JvmInline
value class Solution(val map: HashMap<Int, Boolean> = hashMapOf()) {
    val entries get() = map.entries
    val keys get() = map.keys
    fun clone(): Solution = Solution(HashMap(map))
    fun containsKey(key: Int) = map.containsKey(key)
    operator fun get(key: Int): Boolean? = map[key]
    operator fun set(key: Int, value: Boolean) { map[key] = value}
    fun isEmpty(): Boolean = map.isEmpty()
    fun isNotEmpty(): Boolean = map.isNotEmpty()
    fun remove(key: Int) = map.remove(key)
}


@JvmInline
value class SetOfSolutions(private val hashSet: HashSet<Solution> = hashSetOf()) {
    fun add(solution: Solution) = hashSet.add(solution)
    fun addAll(solutions: SetOfSolutions) = hashSet.addAll(solutions.hashSet)
    fun clone(): SetOfSolutions = SetOfSolutions( HashSet(hashSet) )
    operator fun iterator() = hashSet.iterator()
    fun isEmpty(): Boolean = hashSet.isEmpty()
    fun isNotEmpty(): Boolean = hashSet.isNotEmpty()
    fun size(): Int = hashSet.size
}