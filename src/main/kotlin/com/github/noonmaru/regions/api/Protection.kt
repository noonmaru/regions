package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.util.Indexable
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSortedMap
import java.util.*


enum class Protection(val key: String) : Indexable {
    DAMAGE("damage"),
    POTION("potion"),
    IGNITION("ignition"),
    BLOCK_MELT("melt"),
    GROWTH("growth"),
    FORM("form"),
    SPREAD("blockSpread"),
    REDSTONE("redstone"),
    OVERFLOW("overflow"),
    FLOOD("flood"),
    ENTITY_CHANGE_BLOCK("entityChangeBlock"),
    EXPLOSION("explosion");

    override val offset: Int
        get() = ordinal

    override val raw: Int = super.raw

    companion object {
        private val byOffset = ImmutableList.copyOf(values())
        private val byKey: Map<String, Protection>

        init {
            val byKey = TreeMap<String, Protection>(String.CASE_INSENSITIVE_ORDER)

            for (value in values()) {
                byKey[value.key] = value
            }

            this.byKey = ImmutableSortedMap.copyOf(byKey)
        }

        fun getByOffset(offset: Int): Protection? {
            return byOffset.getOrNull(offset)
        }

        @JvmStatic
        fun getByKey(key: String): Protection? {
            return byKey[key]
        }
    }

    override fun toString(): String {
        return key
    }
}

interface Protectable {
    val protections: Set<Protection>

    fun hasProtections(protection: Protection): Boolean

    fun addProtections(vararg protections: Protection) {
        addProtections(protections.asList())
    }

    fun addProtections(protections: Collection<Protection>)

    fun removeProtections(vararg protections: Protection) {
        removeProtections(protections.asList())
    }

    fun removeProtections(protections: Collection<Protection>)
}