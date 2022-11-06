package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal class KActor<T>(
    name: String,
    private val receiveChannel: Channel<T>,
    scope: CoroutineScope
) {

    private val ctx: KActorContext<T> =
        KActorContext(KActorRef(receiveChannel), name, scope)

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

class KActorContext<T>(
    val actorRef: KActorRef<T>,
    val name: String,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob())
)

fun <T> KActorContext<*>.kactor(name: String, behavior: KBehavior<T>): KActorRef<T> {
    val mailbox = Channel<T>()
    scope.launch {
        val actor = KActor(name, mailbox, this)
        actor.run(behavior)
    }
    return KActorRef(mailbox)
}

fun <T, R> CoroutineScope.ask(
    toKActorRef: KActorRef<T>,
    timeoutInMillis: Long = 1000L,
    msgFactory: (ref: KActorRef<R>) -> T
): Deferred<R> {
    val mailbox = Channel<R>()
    val result = async {
        try {
            withTimeout(timeoutInMillis) {
                toKActorRef.tell(msgFactory.invoke(KActorRef(mailbox)))
                val msgReceived = mailbox.receive()
                msgReceived
            }
        } finally {
            mailbox.close()
        }
    }
    return result
}

fun <T> CoroutineScope.kactorSystem(behavior: KBehavior<T>): KActorRef<T> {
    val mailbox = Channel<T>()
    launch {
        val actor = KActor("main", mailbox, this)
        actor.run(behavior)
    }
    return KActorRef(mailbox)
}
