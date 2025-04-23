package org.cubewhy.celestial.bot.command.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.util.findEnumByNameIgnoreCase
import org.springframework.stereotype.Component

@Component
class RoleCommand(private val userRepository: UserRepository) : Command {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun trigger() = "role"
    override fun usage() = "<user> [<add/remove> <role name>]"
    override fun description() = "Manager roles"
    override fun roles() = listOf(Role.ADMIN)

    override suspend fun execute(
        user: User,
        args: List<String>
    ): String {
        if (args.size != 3 && args.size != 1) {
            return "Bad usage"
        }
        val username = args[0]
        // find user
        val user = userRepository.findByUsernameIgnoreCase(username).awaitFirstOrNull()
            ?: return "User with username $username not found"
        if (args.size == 1) {
            // just list roles the user has
            return "${user.username} has ${user.resolvedRoles} roles"
        }
        val method = args[1]
        val roleName = args[2]
        // parse role
        val role = findEnumByNameIgnoreCase<Role>(roleName) ?: return "Invalid role name"
        // TODO move to user service
        if (method == "add") {
            if (user.resolvedRoles.contains(role)) {
                return "Role already exists"
            }
            // grant role
            logger.info { "Grant role $role to user ${user.username}" }
            user.roles.add(role)
            // save user
            userRepository.save(user).awaitFirst()
            return "Success"
        } else if (method == "remove") {
            if (!user.resolvedRoles.contains(role)) {
                return "Role does not exist"
            }
            if (user.username.equals(username, ignoreCase = true) && role == Role.ADMIN) {
                return "You cannot remove the admin role for yourself"
            }
            if (role == Role.USER) {
                return "You cannot remove the default role"
            }
            logger.info { "Remove role $role for user ${user.username}" }
            user.roles.remove(role)
            userRepository.save(user).awaitFirst()
            return "Success"
        }
        // unknown command
        return help()
    }
}