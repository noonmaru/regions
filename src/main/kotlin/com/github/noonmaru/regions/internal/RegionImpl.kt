package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.util.IntBitSet
import com.google.common.collect.ImmutableList
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashSet

class RegionImpl(
    val manager: RegionManager,
    override val name: String,
    world: RegionWorldImpl,
    box: RegionBox
) : Region {
    override var world: RegionWorldImpl = world
        private set
    override var box: RegionBox = box
        private set
    override val parents: List<Region>
        get() = _parents.toList()
    override val children: List<Region>
        get() = _children.toList()
    override val roles: List<Role>
        get() = _roles.values.toList()
    override val publicRole: Role
        get() = _publicRole
    override val members: List<Member>
        get() = ImmutableList.copyOf(_members.values)
    override val protections: Set<Protection>
        get() = _protections.clone()
    override var valid: Boolean = true

    private val _parents: MutableSet<RegionImpl> = HashSet(0)
    private val _children: MutableSet<RegionImpl> = HashSet(0)
    private val _roles: MutableMap<String, RoleImpl> = TreeMap()
    private val _publicRole = RoleImpl(this, "@everyone", true)
    private val _members: MutableMap<UserImpl, MemberImpl> = HashMap()
    private val _protections: IntBitSet<Protection> = IntBitSet { Protection.getByOffset(it) }
    private val playerPermissions: MutableMap<Player, IntBitSet<Permission>> = WeakHashMap(0)

    override fun relocate(newWorld: RegionWorld, newBox: RegionBox) {
        checkState()
        val newWorldImpl = newWorld as RegionWorldImpl

        newWorldImpl.checkOverlap(newBox, this)
        this.world.removeRegion(this)
        newWorldImpl.placeRegion(this, newBox)

        this.world = newWorldImpl
        this.box = newBox
    }

    private fun getAllLineage(get: RegionImpl.() -> Collection<RegionImpl>): Set<RegionImpl> {
        return HashSet<RegionImpl>().also { set ->
            getAllLineage(set, get)
        }
    }

    private fun getAllLineage(set: MutableSet<RegionImpl>, get: RegionImpl.() -> Collection<RegionImpl>) {
        get().forEach { other ->
            if (set.add(other)) {
                other.getAllLineage(set, get)
            }
        }
    }

    private fun getEssentialLineage(get: RegionImpl.() -> Collection<RegionImpl>): List<RegionImpl> {
        val map = IdentityHashMap<RegionImpl, Boolean>()
        computeLineage(map, get)

        return ImmutableList.copyOf(map.filterValues { it }.keys)
    }

    private fun computeLineage(map: MutableMap<RegionImpl, Boolean>, get: RegionImpl.() -> Collection<RegionImpl>) {
        for (other in get()) {
            val result = map.compute(other) { _, value ->
                value == null
            }!!

            if (result) {
                other.computeLineage(map, get)
            }
        }
    }

    override fun addParent(region: Region): Boolean {
        checkState()

        val children = getAllLineage { _children }
        require(region !in children) { "Circular relationship" }

        val regionImpl = region as RegionImpl

        if (_parents.add(regionImpl)) {
            regionImpl._children.add(this)
            clearAllPermissions(children)
            return true
        }

        return false
    }

    override fun removeParent(region: Region): Boolean {
        checkState()

        if (_parents.remove(region)) {
            val regionImpl = region as RegionImpl
            regionImpl._children.remove(this)
            clearAllPermissions()
            return true
        }

        return false
    }

    private fun clearAllPermissions(set: Set<RegionImpl> = getAllLineage { _children }) {
        justClearAllPermissions()
        for (region in set) {
            region.justClearAllPermissions()
        }
    }

    private fun justClearAllPermissions() {
        playerPermissions.clear()
    }

    private fun clearPermission(player: Player, set: Set<RegionImpl> = getAllLineage { _children }) {
        justClearPermission(player)
        for (region in set) {
            region.justClearPermission(player)
        }
    }

    private fun justClearPermission(player: Player) {
        playerPermissions.remove(player)
    }

    override fun createRole(name: String): RoleImpl {
        checkState()

        val roles = _roles
        require(name !in roles) { "Name is already in use" }

        return RoleImpl(this, name).also {
            roles[name] = it
        }
    }

    override fun removeRole(name: String): RoleImpl? {
        checkState()

        return _roles.remove(name)?.also { role ->
            role.destroy()

            for (member in _members.values) {
                if (member.removeRole(role)) {
                    member.user.bukkitPlayer?.let { player ->
                        clearPermission(player)
                    }
                }
            }
        }
    }

    override fun getRole(name: String): RoleImpl? {
        return _roles[name]
    }

    override fun addMember(user: User): MemberImpl {
        checkState()

        val members = _members
        require(user !in members) { "User is already member" }

        val userImpl = user as UserImpl

        return MemberImpl(this, userImpl).also { member ->
            members[userImpl] = member
            userImpl.addRegionMember(this, member)
            userImpl.bukkitPlayer?.let {
                clearPermission(it)
            }
        }
    }

    override fun removeMember(user: User): Member? {
        checkState()

        val userImpl = user as UserImpl

        return _members.remove(userImpl)?.also { member ->
            userImpl.removeRegionMember(this)
            member.destroy()
            userImpl.bukkitPlayer?.let {
                clearPermission(it)
            }
        }
    }

    override fun getMember(user: User): Member? {
        return _members[user]
    }

    override fun getMembersWithRoles(roles: Collection<Role>): List<Member> {
        return _members.values.filter { it.hasRoles(roles) }
    }

    private fun Member.check(): MemberImpl {
        checkState()
        require(region == this@RegionImpl) { "Other region member" }

        return this as MemberImpl
    }

    private fun Role.check(): RoleImpl {
        checkState()
        require(region == this@RegionImpl) { "Other region role" }

        return this as RoleImpl
    }

    override fun addRoleToMember(member: Member, role: Role): Boolean {
        checkState()

        return member.check().addRole(role.check()).also { result ->
            if (result) {
                member.user.bukkitPlayer?.let {
                    clearPermission(it)
                }
            }
        }
    }

    override fun removeRoleFromMember(member: Member, role: Role): Boolean {
        checkState()

        return member.check().removeRole(role.check()).also { result ->
            if (result) {
                member.user.bukkitPlayer?.let {
                    clearPermission(it)
                }
            }
        }
    }

    override fun hasProtection(protection: Protection): Boolean {
        return protections.contains(protection)
    }

    override fun addProtection(protections: Collection<Protection>) {
        _protections.addAll(protections)
    }

    override fun removeProtection(protections: Collection<Protection>) {
        _protections.removeAll(protections)
    }

    override fun getPermissions(player: Player): Set<Permission> {
        return playerPermissions.computeIfAbsent(player) {
            computePermissions(it, it.user)
        }
    }

    private fun computePermissions(player: Player, user: User): IntBitSet<Permission> {
        val set = IntBitSet { Permission.getByOffset(it) }

        getEssentialLineage { _parents }.forEachIndexed { index, parent ->
            val parentPermissions = parent.getOrComputePermissions(player, user)

            if (index == 0)
                set.or(parentPermissions)
            else
                set.and(parentPermissions)
        }

        return set
    }

    private fun getOrComputePermissions(player: Player, user: User): IntBitSet<Permission> {
        return playerPermissions.computeIfAbsent(player) {
            computePermissions(it, user)
        }
    }

    override fun delete() {
        checkState()

        manager.removeRegion(this.name)
    }

    fun destroy() {
        valid = false
        _parents.forEach { it._children.remove(this) }
        _children.forEach { it._parents.remove(this) }
        //TODO delete file
    }
}