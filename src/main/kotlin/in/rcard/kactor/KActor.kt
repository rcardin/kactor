package `in`.rcard.kactor

import kotlinx.coroutines.channels.ReceiveChannel

// FIXME Do we need this interface?
internal class KActor<T>(
    private val behavior: KBehavior<T>,
    private val context: KActorContext<T>,
    private val mailbox: ReceiveChannel<T>
) {

    suspend fun run() {
        while (true) {
            val message = mailbox.receive()
            behavior.receive(context, message)
        }
    }
}
