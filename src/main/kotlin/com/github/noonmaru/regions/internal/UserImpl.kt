package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Member
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.User
import com.google.common.collect.ImmutableList
import org.bukkit.entity.Player
import java.util.*

class UserImpl(
    override val uniqueId: UUID,
    override val name: String
) : User {
    override var bukkitPlayer: Player? = null
    override val regionMembers: Collection<Member>
        get() = ImmutableList.copyOf(membersByRegion.values)

    private val membersByRegion: MutableMap<RegionImpl, MemberImpl> = WeakHashMap(0)

    override fun getMemberByRegion(region: Region): Member? {
        return membersByRegion[region]
    }

    internal fun addRegionMember(region: RegionImpl, member: MemberImpl) {
        membersByRegion[region] = member
    }

    internal fun removeRegionMember(region: RegionImpl) {
        membersByRegion.remove(region)
    }
}