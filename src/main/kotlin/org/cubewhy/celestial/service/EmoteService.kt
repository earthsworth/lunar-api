package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import org.cubewhy.celestial.entity.User

interface EmoteService : PacketProcessor {
    suspend fun processLogin(user: User):GeneratedMessage
//    suspend fun processStopEmotePush(user: User):GeneratedMessage
}