package `in`.rcard.kactor

import kotlinx.coroutines.channels.SendChannel

class KActorRef<T> internal constructor(
    private val actorName: String,
    private val mailbox: SendChannel<T>
) {
    suspend fun tell(msg: T) {
        println("Sending message to actor '$actorName'")
        mailbox.send(msg)
        println("Sent message to actor '$actorName'")
    }

    companion object KActorRefOps {
        suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
            tell(msg)
        }
    }
}
