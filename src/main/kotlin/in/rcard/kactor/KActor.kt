package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

internal class KActor<T>(name: String, private val receiveChannel: ReceiveChannel<T>) {

    private val ctx: KActorContext<T> =
        KActorContext(KActorRef(receiveChannel as Channel<T>), name)

    suspend fun run(behavior: KBehavior<T>) {
        val msg = receiveChannel.receive()
        when (val newBehavior = behavior.receive(ctx, msg)) {
            is KBehaviorSame -> {
                run(behavior)
            }

            is KExtensibleBehavior -> {
                run(newBehavior)
            }
        }
    }
}

class KActorContext<T>(val actorRef: KActorRef<T>, val name: String)

fun <T> CoroutineScope.kactor(name: String, behavior: KBehavior<T>): KActorRef<T> {
    val mailbox = Channel<T>()
    launch {
        val actor = KActor(name, mailbox)
        actor.run(behavior)
    }
    return KActorRef(mailbox)
}

fun <T, R> CoroutineScope.ask(toKActorRef: KActorRef<T>, msgFactory: (ref: KActorRef<R>) -> T): Deferred<R> {
    val mailbox = Channel<R>()
    val result = async {
        toKActorRef.tell(msgFactory.invoke(KActorRef(mailbox)))
        val msgReceived = mailbox.receive()
        mailbox.close()
        msgReceived
    }
    return result
}
