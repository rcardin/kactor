package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext

internal class KActor<T>(
    name: String,
    private val receiveChannel: Channel<T>,
    scope: CoroutineScope
) {

    private val ctx: KActorContext<T> =
        KActorContext(KActorRef(receiveChannel), name, scope)

    suspend fun run(behavior: KBehavior<T>) {
        when (behavior) {
            is KBehaviorSetup -> {
                val newBehavior = behavior.setup(ctx)
                nextBehavior(newBehavior, behavior)
            }

            is KBehaviorExtension -> {
                val msg = receiveChannel.receive()
                val newBehavior = behavior.receive(ctx, msg)
                nextBehavior(newBehavior, behavior)
            }

            is KBehaviorSame -> {
                throw IllegalStateException("The use of the behavior 'KBehaviorSame' is not supported as the first behavior")
            }

            is KBehaviorStop -> {
                receiveChannel.close()
                // TODO We should stop also all the children actors
            }

            is KBehaviorSupervised -> {
                // FIXME: Maybe, it's possible to use the KBehaviorExtension to handle the supervision
                run(behavior.supervisedBehavior)
            }
        }
    }

    private suspend fun nextBehavior(
        newBehavior: KBehavior<T>,
        behavior: KBehavior<T>
    ) {
        when (newBehavior) {
            is KBehaviorSame -> {
                run(behavior)
            }

            else -> {
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

fun <T> KActorContext<*>.spawn(name: String, behavior: KBehavior<T>): KActorRef<T> {
    val mailbox = Channel<T>(capacity = Channel.UNLIMITED)
    // FIXME This prevent a child actor to stop the parent actor
    //       Make it configurable
    val job = resolveJob(behavior)
    scope.launch(job) {
        val actor = KActor(name, mailbox, this)
        actor.run(behavior)
    }
    return KActorRef(mailbox)
}

private fun <T> resolveJob(behavior: KBehavior<T>): CoroutineContext =
    when (behavior) {
        is KBehaviorSupervised -> {
            when (behavior.strategy) {
                SupervisorStrategy.STOP -> SupervisorJob()
                SupervisorStrategy.ESCALATE -> Job()
            }
        }

        else -> Job()
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
