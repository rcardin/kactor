package `in`.rcard.kactor

import kotlinx.coroutines.channels.ReceiveChannel

// FIXME Do we need this interface?
class KActor<T>(
    val name: String,
    private val context: KActorContext<T>,
    private val mailbox: ReceiveChannel<T>
) {

    suspend fun run(behavior: KBehavior<T>) {
        val message = mailbox.receive()
        println("Actor '$name' processing message $message")
        val newBehavior = behavior.receive(context, message)
        println("Actor '$name' processed message $message")
        when (newBehavior) {
            KBehaviorSame -> run(behavior)
            is KExtensibleBehavior -> run(newBehavior)
        }
    }
}
