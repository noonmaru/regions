package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.getSection
import com.github.noonmaru.regions.getStringValue
import com.github.noonmaru.regions.toEnumList
import com.github.noonmaru.regions.toStringList
import com.github.noonmaru.regions.util.IntBitSet
import com.google.common.collect.ImmutableList
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.collections.HashSet

class RegionImpl internal constructor(
    private val manager: RegionManagerImpl,
    override val name: String,
    world: RegionWorldImpl,
    box: RegionBox
) : Region {
    override var world: RegionWorldImpl = world
        private set
    override var box: RegionBox = box
        private set
    override val parents: List<Region>
        get() = ImmutableList.copyOf(_parents)
    override val children: List<Region>
        get() = ImmutableList.copyOf(_children)
    override val protections: Set<Protection>
        get() = Collections.unmodifiableSet(_protections)
    override val roles: List<Role>
        get() = ImmutableList.copyOf(_roles.values)
    override val publicRole: Role
        get() = _publicRole
    override val members: List<Member>
        get() = ImmutableList.copyOf(_members.values)
    override var valid: Boolean = true

    private val _parents: MutableSet<RegionImpl> = HashSet(0)
    private val _children: MutableSet<RegionImpl> = HashSet(0)
    private val _roles: MutableMap<String, RoleImpl> = TreeMap()
    private val _publicRole = RoleImpl(this, "@everyone", true)
    private val _members: MutableMap<UserImpl, MemberImpl> = HashMap()
    private val _protections: IntBitSet<Protection> = IntBitSet { Protection.getByOffset(it) }
    private val playerPermissions: MutableMap<Player, IntBitSet<Permission>> = WeakHashMap(0)

    private val file: File
        get() = File(manager.regionsFolder, "$name.yml")
    private var mustBeSave = false

    override fun relocate(newWorld: RegionWorld, newBox: RegionBox) {
        checkState()
        val newWorldImpl = newWorld as RegionWorldImpl

        newWorldImpl.checkOverlap(newBox, this)
        this.world.removeRegion(this)
        newWorldImpl.placeRegion(this, newBox)

        this.world = newWorldImpl
        this.box = newBox
        this.mustBeSave = true
        setMustBeSave()
    }

    private fun getAllLineage(get: RegionImpl.() -> Collection<RegionImpl>): Set<RegionImpl> {
        return HashSet<RegionImpl>().also { set ->
            getAllLineage(set, get)
        }
    }

    private fun getAllLineage(set: MutableSet<RegionImpl>, get: RegionImpl.() -> Collection<RegionImpl>) {
        this.get().forEach { other ->
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
        for (other in this.get()) {
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
            setMustBeSave()
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
            setMustBeSave()
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
            setMustBeSave()
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

            setMustBeSave()
        }
    }

    override fun getRole(name: String): RoleImpl? {
        return _roles[name]
    }

    override fun addPermissionToRole(role: Role, permissions: Collection<Permission>): Boolean {
        checkState()

        return role.check().addPermissions(permissions).also {
            if (it) setMustBeSave()
        }
    }

    override fun removePermissionFromRole(role: Role, permissions: Collection<Permission>): Boolean {
        checkState()

        return role.check().removePermissions(permissions).also {
            if (it) setMustBeSave()
        }
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
            setMustBeSave()
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
            setMustBeSave()
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
                setMustBeSave()
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
                setMustBeSave()
            }
        }
    }

    override fun hasProtection(protection: Protection): Boolean {
        return protections.contains(protection).also {
            if (it) setMustBeSave()
        }
    }

    override fun addProtection(protections: Collection<Protection>) {
        _protections.addAll(protections)
        setMustBeSave()
    }

    override fun removeProtection(protections: Collection<Protection>) {
        _protections.removeAll(protections).also {
            if (it) setMustBeSave()
        }
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

    internal fun destroy() {
        valid = false
        _parents.forEach { it._children.remove(this) }
        _children.forEach { it._parents.remove(this) }
        file.delete()
    }

    companion object {
        const val CFG_WORLD = "world"
        const val CFG_BOX = "box"
        const val CFG_PARENTS = "parents"
        const val CFG_PROTECTIONS = "protections"
        const val CFG_ROLES = "roles"
        const val CFG_PUBLIC_ROLE = "public-role"
        const val CFG_MEMBERS = "members"
        const val CFG_NAME = "name"
        const val CFG_PERMISSIONS = "permissions"

        internal fun load(file: File, manager: RegionManagerImpl): RegionLoader {
            val config = YamlConfiguration.loadConfiguration(file)

            //필수요소
            val region = run {
                val name = file.name.removeSuffix(".yml")
                val world = manager.getOrCreateRegionWorld(config.getStringValue(CFG_WORLD))
                val box = config.loadRegionBox(CFG_BOX)

                RegionImpl(manager, name, world, box)
            }

            //부가 기본요소
            region.apply {
                //load protection
                val protections =
                    config.getStringList(CFG_PROTECTIONS).toEnumList({ Protection.getByKey(it) }) { name ->
                        Regions.logger.warning("Unknown protection '$name' in region file ${file.name}")
                    }
                _protections.addAll(protections)

                //load roles
                config.getConfigurationSection(CFG_ROLES)?.getValues(false)?.let { roleConfigs ->
                    for ((name, roleConfig) in roleConfigs) {
                        if (roleConfig is ConfigurationSection) {
                            val permissions =
                                roleConfig.getStringList(CFG_PERMISSIONS)
                                    .toEnumList({ Permission.getByKey(it) }) { perm ->
                                        Regions.logger.warning("Unknown permission '$name.$perm' in region file ${file.name}")
                                    }

                            val role = RoleImpl(region, name)
                            role._permissions.addAll(permissions)

                            _roles[name] = role
                        } else {
                            Regions.logger.warning("Unknown format role '$name' in region file ${file.name} ")
                        }
                    }
                }

                //load public role
                config.getConfigurationSection(CFG_PUBLIC_ROLE)?.let { publicRoleConfig ->
                    val permissions =
                        publicRoleConfig.getStringList(CFG_PERMISSIONS)
                            .toEnumList({ Permission.getByKey(it) }) { perm ->
                                Regions.logger.warning("Unknown permission public role '$perm' in region file ${file.name}")
                            }
                    _publicRole._permissions.addAll(permissions)
                }
            }

            val parents = config.getStringList(CFG_PARENTS)
            val members = ArrayList<Pair<UserImpl, List<RoleImpl>>>(0)

            config.getConfigurationSection(CFG_MEMBERS)?.getValues(false)?.let { memberConfigs ->
                members.ensureCapacity(memberConfigs.count())

                for ((memberUUIDString, memberConfig) in memberConfigs) {
                    if (memberConfig is ConfigurationSection) {
                        try {
                            val uniqueId = UUID.fromString(memberUUIDString)
                            val user = manager.getOrCreateUser(uniqueId)

                            if (user == null) {
                                Regions.logger.warning("Not found user '$memberUUIDString' in region file ${file.name} ")
                                continue
                            }

                            val roleNames = memberConfig.getStringList(CFG_ROLES)
                            val roles = ArrayList<RoleImpl>(roleNames.count())

                            for (roleName in roleNames) {
                                val role = region.getRole(roleName)

                                if (role == null) {
                                    Regions.logger.warning("Not found role $memberUUIDString.$roleName in region file ${file.name}")
                                } else {
                                    roles += role
                                }
                            }

                            members += Pair(user, roles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Regions.logger.warning("Failed to load member $memberUUIDString in region file ${file.name}")
                        }
                    } else {
                        Regions.logger.warning("Unknown format member $memberUUIDString in region file ${file.name}")
                    }
                }
            }

            return RegionLoader(region, parents, members)
        }
    }

    internal fun load(loader: RegionLoader) {
        val children = getAllLineage { _children }

        for (parentName in loader.parents) {
            manager.getRegion(parentName)?.let { parent ->
                if (parent in children) {
                    Regions.logger.warning("Circular relationship $parentName -> $name -> $parentName")
                } else {
                    _parents.add(parent)
                    parent._children.add(this)
                }
            }
        }

        for ((user, roles) in loader.members) {
            _members[user] = MemberImpl(this, user).apply {
                loadRoles(roles)
            }
        }
    }

    internal fun setMustBeSave() {
        this.mustBeSave = true
    }

    override fun save(): Boolean {
        checkState()

        if (!mustBeSave) return false

        val config = YamlConfiguration()
        config[CFG_WORLD] = world.name
        config.save(CFG_BOX, box)
        config[CFG_PARENTS] = _parents.map { it.name }
        config[CFG_PROTECTIONS] = protections.toStringList()
        config.createSection(CFG_ROLES).apply {
            _roles.values.forEach { role ->
                save(role.name, role)
            }
        }
        config.save(CFG_PUBLIC_ROLE, _publicRole)
        config.createSection(CFG_MEMBERS).apply {
            for ((user, member) in _members) {
                save(user.name, member)
            }
        }

        val file = file
        file.parentFile.mkdirs()
        val tempFile = File(file.parent, "${file.name}.tmp")
        config.save(tempFile)
        file.delete()
        tempFile.renameTo(file)
        mustBeSave = false
        return true
    }
}

private fun ConfigurationSection.loadRegionBox(path: String): RegionBox {
    val section = getSection(path)
    val min = section.getSection("min")
    val max = section.getSection("max")

    return RegionBox(
        min.getInt("x"),
        min.getInt("y"),
        min.getInt("z"),
        max.getInt("x"),
        max.getInt("y"),
        max.getInt("z")
    )
}

private fun ConfigurationSection.save(path: String, box: RegionBox) {
    val section = createSection(path)

    section.createSection("min").let { min ->
        min["x"] = box.minX
        min["y"] = box.minY
        min["z"] = box.minZ
    }
    section.createSection("max").let { max ->
        max["x"] = box.maxX
        max["y"] = box.maxY
        max["z"] = box.maxZ
    }
}

private fun ConfigurationSection.save(path: String, role: RoleImpl) {
    createSection(path)[RegionImpl.CFG_PERMISSIONS] = role.permissions.toStringList()
}

private fun ConfigurationSection.save(path: String, member: MemberImpl) {
    val section = createSection(path)

    section[RegionImpl.CFG_NAME] = member.user.name
    section[RegionImpl.CFG_ROLES] = member.roles.map { it.name }
}