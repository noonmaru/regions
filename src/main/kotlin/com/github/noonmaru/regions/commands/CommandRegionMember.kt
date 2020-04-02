package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Member
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.Regions
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.tabComplete
import com.github.noonmaru.tap.mojang.getProfile
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class CommandRegionMember : CommandComponentRegion {
    override val argsCount: Int
        get() = super.argsCount + 1

    //region member <Region> add <User>
    //region member <Region> remove <User>
    //region member <Region> role <User> add <Role>
    //region member <Region> role <User> remove <Role>
    //region member <Region> list [User]
    override fun onCommand(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region,
        args: ArgumentList
    ): Boolean {
        val sub = args.next()

        if (sub.equals("add", true)) {
            if (!args.hasNext()) {
                sender.sendMessage("/$label $componentLabel <Region> $sub add <User>")
                return true
            }

            val userName = args.next()
            val profile = getProfile(userName)

            if (profile == null) {
                sender.sendMessage("[$userName] 유저를 찾지 못했습니다.")
                return true
            }

            val user = Regions.getUser(profile)

            kotlin.runCatching {
                region.addMember(user)
                region.save()
                sender.sendMessage("[[${region.name}] 구역에 ${profile.name}] 유저를 등록했습니다.")
            }.onFailure {
                sender.sendMessage("[${region.name}] 구역에 [${profile.name}] 유저는 이미 등록되어있습니다.")
            }
        } else if (sub.equals("remove", true)) {
            if (!args.hasNext()) {
                sender.sendMessage("/$label $componentLabel <Region> $sub remove <User>")
                return true
            }

            val userName = args.next()
            val profile = getProfile(userName)

            if (profile == null) {
                sender.sendMessage("[$userName] 유저를 찾지 못했습니다.")
                return true
            }

            val user = Regions.getUser(profile)
            val removed = region.removeMember(user)

            if (removed == null) {
                sender.sendMessage("[${region.name}] 구역에 [${profile.name}] 유저는 등록되지 않았습니다.")
                return true
            }

            region.save()
            sender.sendMessage("[${region.name}] 구역에서 [${profile.name}] 유저를 제거했습니다.")
        } else if (sub.equals("role", true)) {
            if (args.remain() < 3) {
                sender.sendMessage("/$label $componentLabel <Region> $sub role <User> (add | remove) <Role>")
                return true
            }

            val userName = args.next()
            val profile = getProfile(userName)

            if (profile == null) {
                sender.sendMessage("[$userName] 유저를 찾지 못했습니다.")
                return true
            }

            val op = args.next()

            if (op.equals("add", true) || op.equals("remove", true)) {
                val user = Regions.getUser(profile)
                val member = region.getMember(user)

                if (member == null) {
                    sender.sendMessage("[${region.name}] 구역에 [${profile.name}] 유저는 등록되지 않았습니다.")
                    return true
                }

                val roleName = args.next()
                val role = region.getRole(roleName)

                if (role == null) {
                    sender.sendMessage("[${region.name}] 구역에 [${roleName}] 역할은 등록되지 않았습니다.")
                    return true
                }

                if (op.equals("add", true)) {
                    region.addRoleToMember(member, role)
                    sender.sendMessage("[${region.name}] 구역의 [${user.name}] 멤버에게 [${role.name}] 역할을 추가했습니다.")
                } else /*remove*/ {
                    region.removeRoleFromMember(member, role)
                    sender.sendMessage("[${region.name}] 구역의 [${user.name}] 멤버에게 [${role.name}] 역할을 제거했습니다.")
                }

                region.save()

            } else {
                sender.sendMessage("알 수 없는 명령입니다.")
            }
        } else if (sub.equals("list", true)) {
            if (args.hasNext()) {
                val userName = args.next()
                val profile = getProfile(userName)

                if (profile == null) {
                    sender.sendMessage("[$userName] 유저를 찾지 못했습니다.")
                    return true
                }

                val user = Regions.getUser(profile)
                val member = region.getMember(user)

                if (member == null) {
                    sender.sendMessage("[${region.name}] 구역에 [${profile.name}] 유저는 등록되지 않았습니다.")
                    return true
                }

                val builder = StringBuilder().apply {
                    val roles = member.roles

                    append("[").append(region.name).append("]").append(" 구역 [").append(user.name)
                        .append("] 멤버의 역할 목록 (").append(roles.count()).append("): ")

                    if (roles.isNotEmpty()) {
                        val iter = roles.iterator()

                        while (true) {
                            append("[").append(iter.next().name).append("]")

                            if (!iter.hasNext()) break

                            append(", ")
                        }
                    }
                }

                sender.sendMessage(builder.toString())
            } else {
                val builder = StringBuilder().apply {
                    val members = region.members

                    append("[").append(region.name).append("]").append(" 구역의 유저 목록 (").append(members.count())
                        .append("): ")

                    if (members.isNotEmpty()) {
                        val iter = members.iterator()

                        while (true) {
                            append("[").append(iter.next().user.name).append("]")

                            if (!iter.hasNext()) break

                            append(", ")
                        }
                    }
                }

                sender.sendMessage(builder.toString())
            }
        } else {
            sender.sendMessage("알 수 없는 명령입니다.")
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region?,
        args: ArgumentList
    ): List<String> {
        val last = args.last()

        return when (args.remain()) {
            1 -> listOf("add", "remove", "role", "list").tabComplete(last)
            2 -> {
                val sub = args.next()

                if (sub.equals("add", true)) {
                    Bukkit.getOnlinePlayers().tabComplete(last) { it.name }
                } else if (sub.equals("remove", true)
                    || sub.equals("role", true)
                    || sub.equals("list", true)
                ) {
                    region?.members?.tabComplete(last) { it.user.name } ?: emptyList()
                } else emptyList()
            }
            3 -> {
                val sub = args.next()

                if (sub.equals("role", true)) {
                    listOf("add", "remove").tabComplete(last)
                } else
                    emptyList()
            }
            4 -> {
                val sub = args.next()

                if (sub.equals("add", true)) {
                    Bukkit.getOnlinePlayers().tabComplete(last) { it.name }
                } else if (region != null) {
                    if (sub.equals("remove", true)) {
                        region.roles.tabComplete(last) { it.name }
                    } else if (sub.equals("role", true)) {
                        val userName = args.next()
                        val member = region.getMember(userName)
                        val op = args.next()
                        if (member != null) {
                            when {
                                op.equals("add", true) -> {
                                    val memberRoles = member.roles
                                    region.roles.filter { it !in memberRoles }.tabComplete(last) { it.name }
                                }
                                op.equals("remove", true) -> {
                                    return member.roles.tabComplete(last) { it.name }
                                }
                                else -> emptyList()
                            }
                        } else
                            emptyList()

                    } else emptyList()
                } else
                    emptyList()
            }
            else ->
                emptyList()
        }
    }
}

private fun Region.getMember(name: String): Member? {
    return getProfile(name)?.let { profile ->
        Regions.getCachedUser(profile.uniqueId)?.let {
            getMember(it)
        }
    }
}