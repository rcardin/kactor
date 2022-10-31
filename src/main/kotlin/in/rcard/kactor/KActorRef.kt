package `in`.rcard.kactor

import kotlinx.coroutines.channels.SendChannel

class KActorRef<T> internal constructor(
    private val mailbox: SendChannel<T>
) {
    suspend fun tell(msg: T) {
        mailbox.send(msg)
    }

    companion object KActorRefOps {
        suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
            tell(msg)
        }
    }
}
