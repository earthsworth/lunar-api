package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User

interface CommandService {
    /**
     * Process command
     *
     * @param message command message
     * @param user issuer
     * @return response message
     * */
    suspend fun process(message: String, user: User): Message?
}