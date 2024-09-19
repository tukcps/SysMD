package com.github.tukcps.sysmd.ui.tableview

import androidx.compose.runtime.snapshots.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.*
import kotlin.contracts.*
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.reflect.KProperty

/**
 * returns `this == null` and uses a contract to relay this information to smart cast
 */
@OptIn(ExperimentalContracts::class)
fun <T> T.isNull(): Boolean {
    contract {
        returns(true) implies (this@isNull == null)
        returns(false) implies (this@isNull != null)
    }
    return this == null
}

/**
 * returns `this != null` and uses a contract to relay this information to smart cast
 */
@OptIn(ExperimentalContracts::class)
fun <T> T.notNull(): Boolean {
    contract {
        returns(false) implies (this@notNull == null)
        returns(true) implies (this@notNull != null)
    }
    return this != null
}

/**
 * Returns null if any of its arguments are null or calls the specified function [block] with [values][x] as its argument and returns its result.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("UNCHECKED_CAST")
inline fun <reified T, R> noneNullLet(vararg x: T?, block: (Array<out T>) -> R): R? {
    contract {
        callsInPlace(block, EXACTLY_ONCE)
    }
    return if (x.none(Any?::isNull)) block(x as Array<out T>) else null
}

/**
 * Enables Negation of function types with [Boolean] return value with the [!][not] operator by negating the resulting [Boolean] on invocation
 *
 * `!((`[T][T]`) -> `[Boolean][Boolean]`)`
 */
operator fun <T> ((T) -> Boolean).not(): (T) -> Boolean = { x: T -> !this(x) }

/**
 * Shorthand for substituting null with an empty string: [someString][String]` ?: `[""][String]
 *
 * Converts a nullable [String?][String] to a non-nullable [String] by returning an empty string ([""][String]) instead of [null][Nothing] if [String?][String]` == `[null][Nothing]
 * */
val String?.e: String get() = this ?: ""

/**
 * Returns a [String] containing an amount of [\t] corresponding to the [Integer] it is called on
 *
 * Example:
 * ```
 * 3.toTabs
 * ```
 * returns
 * ```
 * "            "
 * ```
 * */
val Int.toTabs: String get() = buildString { repeat(this@toTabs) { append("\t") } }


/**
 * Returns an [Array] of [length] filled with the caller instance
 *
 * Example:
 * ```
 * 15.arr(2)
 * ```
 * returns `{15, 15}`
 */
inline fun <reified T> T.arr(length: Int): Array<T> = Array(length) { this }

/**
 * Shorthand for the [Offset] constructor
 *
 * Example:
 * ```
 * 12 X 13
 * ```
 * returns
 * ```
 * Offset(x = 12.toFloat, y = 13.toFloat)
 * ```
 */
infix fun Number.X(y: Number): Offset = Offset(this.toFloat(), y.toFloat())

/**
 * Shorthand for [DpOffset] constructor
 *
 * Example:
 * ```
 * 12.dp X 13.dp
 * ```
 * returns
 * ```
 * DpOffset(x = 12.dp, y = 13.dp)
 * ```
 */
infix fun Dp.X(y: Dp): DpOffset = DpOffset(this, y)

/**
 * Returns a [LinkedHashSet] filled with vararg [elements] in given order
 */
fun <T> linkedHashSetOf(vararg elements: T): LinkedHashSet<T> = LinkedHashSet<T>().apply { addAll(elements.toList()) }

/**
 * Wrapper for [SnapshotStateList] that inherits from [List] instead of [MutableList]
 *
 * Prevents use of modifying functions from outside without reference to the original [SnapshotStateList],
 *
 * Example:
 * ```
 *  private val _values: SnapshotStateList<String> = values.toMutableStateList()
 *      val values: ReadOnlyStateList<String> get() = ReadOnlyStateList(_values)
 * ```
 * _values can only be accessed from within the scope it is declared in, which limits modification to said scope
 */
class ReadOnlyStateList<T>(stateList: SnapshotStateList<T>): StateObject, List<T>, RandomAccess {
    private val internalList: SnapshotStateList<T> = stateList
    override val firstStateRecord: StateRecord get() = internalList.firstStateRecord
    override val size: Int get() = internalList.size
    override fun prependStateRecord(value: StateRecord) = internalList.prependStateRecord(value)
    override fun get(index: Int): T = internalList[index]
    override fun isEmpty(): Boolean = internalList.isEmpty()
    override fun iterator(): Iterator<T> = internalList.iterator()
    override fun listIterator(): ListIterator<T> = internalList.listIterator()
    override fun listIterator(index: Int): ListIterator<T> = internalList.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<T> = internalList.subList(fromIndex, toIndex)
    override fun lastIndexOf(element: T): Int = internalList.lastIndexOf(element)
    override fun indexOf(element: T): Int = internalList.indexOf(element)
    override fun containsAll(elements: Collection<T>): Boolean = internalList.containsAll(elements)
    override fun contains(element: T): Boolean = internalList.contains(element)
    operator fun getValue(anyObject: Any?, property: KProperty<*>): ReadOnlyStateList<T> = this
}