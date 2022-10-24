package `in`.rcard.kactor

import kotlinx.coroutines.channels.ReceiveChannel

// FIXME Do we need this interface?
class KActor<T>(
    private val name: String,
    private val context: KActorContext<T>,
    private val mailbox: ReceiveChannel<T>
) {

    suspend fun run(behavior: KBehavior<T>) {
        val message = mailbox.receive()
        when (val newBehavior = behavior.receive(context, message)) {
            KBehaviorSame -> run(behavior)
            is KExtensibleBehavior -> run(newBehavior)
        }
    }
}
