package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Member
import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Role
import com.github.noonmaru.regions.api.warning
import com.github.noonmaru.regions.util.UpstreamReference
import com.google.common.collect.ImmutableList
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class MemberImpl(
    parent: AreaImpl,
    user: UserImpl
) : Member {
    private val parentRef = UpstreamReference(parent)
    override val parent: AreaImpl
        get() = parentRef.get()

    private val userRef = UpstreamReference(user)
    override val user: UserImpl
        get() = userRef.get()

    override val roles: List<RoleImpl>
        get() = ImmutableList.copyOf(_roles)

    override val permissions: Set<Permission>
        get() = Collections.unmodifiableSet(_permissions.clone())

    override var valid: Boolean = true
        private set

    private val _roles = TreeSet<RoleImpl> { o1, o2 -> o1.name.compareTo(o2.name) }

    private var _mustBeUpdatePermissions = false
    internal val _permissions = PermissionSet()
        get() {
            if (_mustBeUpdatePermissions) {
                field.clear()

                _roles.forEachIndexed { index, role ->
                    if (index == 0)
                        field.or(role._permissions)
                    else
                        field.and(role._permissions)
                }
            }

            return field
        }


    override fun hasRole(role: Role): Boolean {
        return _roles.contains(role)
    }

    override fun hasRoles(roles: Collection<Role>): Boolean {
        return _roles.containsAll(roles)
    }

    internal fun addRole(role: Role): Boolean {
        return _roles.add(role.toImpl()).also {
            if (it) {
                _mustBeUpdatePermissions = true
            }
        }
    }

    internal fun removeRole(role: Role): Boolean {
        return _roles.remove(role).also {
            if (it) {
                _mustBeUpdatePermissions = true
            }
        }
    }

    override fun hasPermission(permission: Permission): Boolean {
        return _permissions.contains(permission)
    }

    override fun hasPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.containsAll(permissions)
    }

    companion object {
        private const val CFG_ROLES = "roles"
    }

    internal fun load(config: ConfigurationSection) {
        val roleNames = config.getStringList(CFG_ROLES)

        for (roleName in roleNames) {
            parent.getRole(roleName)?.also { role ->
                _roles.add(role)
            } ?: warning("Unknown role in [${user.name}] at ${parent.type} [${parent.name}]")
        }
    }

    internal fun save(config: ConfigurationSection) {
        config[CFG_ROLES] = _roles.map { it.name }
    }

    override fun delete() {
        checkState()

        parent.removeMember(user)
    }

    internal fun destroy() {
        valid = false
    }
}

internal fun Member.toImpl(): MemberImpl {
    return this as MemberImpl
}