package `in`.rcard.kactor

import kotlinx.coroutines.channels.SendChannel

/**
 * A reference to an actor.
 *
 * @param T The type of the messages that the actor can receive.
 */
class KActorRef<T> internal constructor(
    private val mailbox: SendChannel<T>,
) {
    suspend fun tell(msg: T) {
        mailbox.send(msg)
    }

    internal fun stop() {
        // FIXME ???
    }

    companion object KActorRefOps {
        suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
            tell(msg)
        }
    }
}
