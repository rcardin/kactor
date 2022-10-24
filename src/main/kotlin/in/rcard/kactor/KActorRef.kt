package `in`.rcard.kactor

import kotlinx.coroutines.channels.SendChannel

interface KActorRef<T> {
    suspend fun tell(msg: T)

    companion object KActorRefOps {
        suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
            tell(msg)
        }
    }
}

class ChannelKActorRef<T>(private val actorMailbox: SendChannel<T>) : KActorRef<T> {
    override suspend fun tell(msg: T) {
        actorMailbox.send(msg)
    }
}
