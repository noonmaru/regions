package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.*
import com.github.noonmaru.regions.toEnumList
import com.github.noonmaru.regions.toStringList
import com.google.common.collect.ImmutableList
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

abstract class AreaImpl(
    internal val manager: RegionManagerImpl,
    override val name: String
) : Area {
    protected abstract val file: File

    override val protections: Set<Protection>
        get() = Collections.unmodifiableSet(_protections)
    override val publicRole: Role
        get() = _publicRole
    override val roles: List<Role>
        get() = ImmutableList.copyOf(_rolesByName.values)
    override val members: List<Member>
        get() = ImmutableList.copyOf(_memberByUser.values)

    private val _protections = ProtectionSet()

    @Suppress("LeakingThis")
    private val _publicRole = RoleImpl(this, "@everyone", true)
    private val _rolesByName = TreeMap<String, RoleImpl>()
    private val _memberByUser = LinkedHashMap<UserImpl, MemberImpl>(0)

    protected val playerPermissions = WeakHashMap<Player, PermissionSet>(0)
    private var mustBeSave = false

    override fun hasProtection(protection: Protection): Boolean {
        return _protections.contains(protection)
    }

    override fun hasProtections(protections: Collection<Protection>): Boolean {
        return _protections.containsAll(protections)
    }

    override fun addProtections(protections: Collection<Protection>): Boolean {
        return _protections.addAll(protections).also {
            if (it) setMustBeSave()
        }
    }

    override fun removeProtections(protections: Collection<Protection>): Boolean {
        return _protections.removeAll(protections).also {
            if (it) setMustBeSave()
        }
    }

    override fun getRole(name: String): RoleImpl? {
        return _rolesByName[name]
    }

    override fun getOrRegisterNewRole(name: String): Role {
        return _rolesByName.computeIfAbsent(name) {
            setMustBeSave()
            RoleImpl(this, it)
        }
    }

    override fun registerNewRole(name: String): Role {
        require(name !in _rolesByName) { "Name is already in use" }

        return RoleImpl(this, name).also {
            _rolesByName[name] = it
            setMustBeSave()
        }
    }

    override fun removeRole(name: String): Role? {
        return _rolesByName.remove(name)?.also { role ->
            for ((user, member) in _memberByUser) {
                if (member.removeRole(role)) {
                    user.bukkitPlayer?.let { player ->
                        resetPlayerPermissions(player)
                    }
                }
            }

            role.destroy()
            setMustBeSave()
        }
    }

    private fun Node.check() {
        checkState()
        require(parent === this@AreaImpl) { "Other region node" }
    }

    override fun addPermissionsToRole(role: Role, permissions: Collection<Permission>): Boolean {
        role.check()

        return role.toImpl().addPermissions(permissions).also {
            if (it) {
                setMustBeSave()
                resetPlayerPermissions(role)
            }
        }
    }

    override fun removePermissionsFromRole(role: Role, permissions: Collection<Permission>): Boolean {
        role.check()

        return role.toImpl().removePermissions(permissions).also {
            if (it) {
                setMustBeSave()
                resetPlayerPermissions(role)
            }
        }
    }

    override fun getMember(user: User): MemberImpl? {
        return _memberByUser[user]
    }

    override fun getMember(name: String): Member? {
        return _memberByUser.entries.find { it.key.name == name }?.value
    }

    override fun getMember(uniqueId: UUID): Member? {
        return _memberByUser.entries.find { it.key.uniqueId == uniqueId }?.value
    }

    override fun getMembersWithRoles(roles: Collection<Role>): List<Member> {
        return _memberByUser.values.filter { it.hasRoles(roles) }.toList()
    }

    override fun addMember(user: User): Member {
        require(user !in _memberByUser) { "Already added user" }

        val impl = user.toImpl()

        return MemberImpl(this, impl).also { member ->
            _memberByUser[impl] = member
            impl.addMember(member)
            setMustBeSave()
        }
    }

    override fun addOrGetMember(user: User): Member {
        return _memberByUser.computeIfAbsent(user.toImpl()) {
            MemberImpl(this, it).also { member ->
                it.addMember(member)
                setMustBeSave()
            }
        }
    }

    override fun removeMember(user: User): Member? {
        return _memberByUser.remove(user)?.also { it.destroy() }
    }

    override fun addRoleToMember(member: Member, role: Role): Boolean {
        member.check()
        role.check()

        return member.toImpl().addRole(role).also {
            if (it) {
                setMustBeSave()

                member.user.bukkitPlayer?.let { player ->
                    resetPlayerPermissions(player)
                }
            }
        }
    }

    override fun removeRoleFromMember(member: Member, role: Role): Boolean {
        member.check()
        role.check()

        return member.toImpl().removeRole(role).also {
            if (it) {
                setMustBeSave()

                member.user.bukkitPlayer?.let { player ->
                    resetPlayerPermissions(player)
                }
            }
        }
    }

    override fun testPermission(player: Player, permission: Permission): Boolean {
        return getOrComputePlayerPermissions(player).contains(permission)
    }

    override fun testPermissions(player: Player, permissions: Collection<Permission>): Boolean {
        return getOrComputePlayerPermissions(player).containsAll(permissions)
    }

    private fun resetPlayerPermissions(player: Player) {
        playerPermissions.remove(player)
    }

    private fun resetPlayerPermissions(role: Role) {
        for ((user, member) in _memberByUser.entries) {
            if (member.hasRole(role)) {
                user.bukkitPlayer?.let { player ->
                    resetPlayerPermissions(player)
                }
            }
        }
    }

    override fun getPlayerPermissions(player: Player): Set<Permission> {
        return Collections.unmodifiableSet(getOrComputePlayerPermissions(player))
    }

    private fun getOrComputePlayerPermissions(player: Player): PermissionSet {
        return playerPermissions.computeIfAbsent(player) {
            computePlayerPermissions(it)
        }
    }

    companion object {
        private val emptyPermissions = PermissionSet()

        const val CFG_PROTECTIONS = "protections"
        const val CFG_PUBLIC_ROLE = "public-role"
        const val CFG_ROLES = "roles"
        const val CFG_MEMBERS = "members"
    }

    protected open fun computePlayerPermissions(player: Player): PermissionSet {
        return _memberByUser[(manager.getUser(player))]?._permissions ?: emptyPermissions
    }

    internal fun setMustBeSave() {
        mustBeSave = true
    }

    override fun save(): Boolean {
        if (!mustBeSave) return false

        val config = YamlConfiguration()
        save(config)

        val file = this.file
        val parentFile = file.parentFile
        val temp = File(parentFile, "{${file.name}.tmp")

        config.runCatching {
            parentFile.mkdirs()
            save(temp)
            file.delete()
            temp.renameTo(file)
        }.onSuccess {
            mustBeSave = false
            return true
        }

        return false
    }

    protected open fun save(config: ConfigurationSection) {
        //save protections
        config[CFG_PROTECTIONS] = _protections.toStringList()

        //save public role
        _publicRole.save(config.createSection(CFG_PUBLIC_ROLE))

        //save roles
        config.createSection(CFG_ROLES).let { rolesSection ->
            for ((name, role) in _rolesByName) {
                role.save(rolesSection.createSection(name))
            }
        }

        //save members
        config.createSection(CFG_MEMBERS).let { membersSection ->
            for ((user, member) in _memberByUser) {
                member.save(membersSection.createSection(user.uniqueId.toString()))
            }
        }
    }

    internal fun load(config: ConfigurationSection) {
        //load protections
        config.getStringList(CFG_PROTECTIONS).toEnumList({ Protection.getByKey(it) }) { name ->
            warning("Unknown protection [$name] at $type [$name]")
        }.let { _protections.addAll(it) }

        //load public role
        config.getConfigurationSection(CFG_PUBLIC_ROLE)?.let { publicRoleSection ->
            _publicRole.load(publicRoleSection)
        }

        //load roles
        config.getConfigurationSection(CFG_ROLES)?.let { rolesSection ->
            for ((name, roleSection) in rolesSection.getValues(false)) {
                if (roleSection is ConfigurationSection) {
                    RoleImpl(this, name).apply {
                        load(roleSection)
                        _rolesByName[name] = this
                    }
                } else {
                    warning("Invalid role format [$name] at $type [${this.name}]")
                }
            }
        }

        //load members
        config.getConfigurationSection(CFG_MEMBERS)?.let { membersSection ->
            for ((uuidString, memberSection) in membersSection.getValues(false)) {
                if (memberSection is ConfigurationSection) {
                    kotlin.runCatching { UUID.fromString(uuidString) }.onSuccess { uniqueId ->
                        manager.findUser(uniqueId)?.let { user ->
                            _memberByUser[user] = MemberImpl(this, user).apply {
                                load(memberSection)
                            }
                        }
                    }.onFailure {
                        warning("Invalid member uuid  [$uuidString] at $type [${this.name}]")
                    }
                } else {
                    warning("Invalid member format [$name] at $type [${this.name}]")
                }
            }
        }
    }

    internal fun linkMembers() {
        for ((user, member) in _memberByUser) {
            user.addMember(member)
        }
    }
}