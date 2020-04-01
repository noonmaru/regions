package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.util.Indexable
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSortedMap
import java.util.*

enum class Permission(val key: String) : Indexable {
    OWNERSHIP("ownership"),
    ADMINISTRATION("administration"),
    ENTRANCE("entrance"),
    EXIT("exit"),
    INTERACTION("interaction"),
    ENTITY_ATTACK("entityAttack"),
    PLAYER_ATTACK("playerAttack"),
    PROJECTILE_LAUNCH("projectileLaunch"),
    BLOCK_BREAK("blockBreak"),
    BLOCK_PLACE("blockPlace"),
    BLOCK_IGNITING("blockIgniting"),
    BUCKET_FILL("bucketFill"),
    BUCKET_EMPTY("bucketEmpty"),
    HANGING_BREAK("hangingBreak"),
    HANGING_PLACE("hangingPlace"),
    DROP_ITEM("dropItem"),
    PICKUP_ITEM("pickupItem");

    override val offset: Int
        get() = ordinal

    override val raw: Int = super.raw

    override fun toString(): String {
        return key
    }

    companion object {
        private val byOffset = ImmutableList.copyOf(values())
        private val byKey: Map<String, Permission>

        init {
            val byKey = TreeMap<String, Permission>(String.CASE_INSENSITIVE_ORDER)

            for (value in values()) {
                byKey[value.key] = value
            }

            this.byKey = ImmutableSortedMap.copyOf(byKey)
        }

        fun getByOffset(offset: Int): Permission? {
            return byOffset.getOrNull(offset)
        }

        fun getByKey(key: String): Permission? {
            return byKey[key]
        }
    }
}

val Array<Permission>.rawPermissions: Int
    get() {
        var raw = 0

        forEach {
            raw = (raw or it.raw)
        }

        return raw
    }

val Iterable<Permission>.rawPermissions: Int
    get() {
        var raw = 0

        forEach {
            raw = (raw or it.raw)
        }

        return raw
    }