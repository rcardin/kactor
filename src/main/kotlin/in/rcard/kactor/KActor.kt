package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

internal class KActor<T>(private val name: String, private val receiveChannel: ReceiveChannel<T>) {
    suspend fun run(behavior: KBehavior<T>) {
        val msg = receiveChannel.receive()
        when (val newBehavior = behavior.receive(msg)) {
            is KBehaviorSame -> {
                run(behavior)
            }

            is KExtensibleBehavior -> {
                run(newBehavior)
            }
        }
    }
}

fun <T> CoroutineScope.kactor(name: String, behavior: KBehavior<T>): KActorRef<T> {
    val mailbox = Channel<T>()
    launch {
        val actor = KActor(name, mailbox)
        actor.run(behavior)
    }
    return KActorRef(mailbox)
}
