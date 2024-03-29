package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

internal class KActor<T>(
    name: String,
    private val receiveChannel: Channel<T>,
    job: Job,
    scope: CoroutineScope,
) {
    private val ctx: KActorContext<T> =
        KActorContext(KActorRef(receiveChannel), name, job, scope)

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
                ctx.job.cancelAndJoin()
            }

            is KBehaviorDecorator -> {
                run(behavior.decorated)
            }

            is KBehaviorWithTimers -> {
                val timerScheduler = TimerScheduler(ctx.scope, ctx.self)
                val newBehavior = behavior.timedBehavior(timerScheduler)
                run(newBehavior)
            }
        }
    }

    private suspend fun nextBehavior(
        newBehavior: KBehavior<T>,
        behavior: KBehavior<T>,
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

class KActorContext<T> internal constructor(
    val self: KActorRef<T>,
    name: String,
    internal val job: Job,
    internal val scope: CoroutineScope,
) {
    val log: Logger = LoggerFactory.getLogger(name)
}

fun <T> KActorContext<*>.spawn(
    name: String,
    behavior: KBehavior<T>,
    finally: ((ex: Throwable?) -> Unit)? = null,
): KActorRef<T> {
    return spawnKActor(
        name,
        behavior,
        scope,
        buildContext(name, job, behavior),
        finally,
    )
}

private fun buildContext(
    name: String,
    parentJob: Job?,
    behavior: KBehavior<*>,
): CoroutineContext {
    val job = resolveJob(behavior, parentJob)
    val dispatcher = resolveDispatcher(behavior)
    return CoroutineName("kactor-$name") + job + dispatcher + MDCContext(mapOf("kactor" to name))
}

private fun resolveJob(
    behavior: KBehavior<*>,
    parentJob: Job?,
): CoroutineContext =
    when (behavior) {
        is KBehaviorSupervised -> {
            when (behavior.strategy) {
                SupervisorStrategy.STOP -> SupervisorJob(parentJob)
                SupervisorStrategy.ESCALATE -> Job(parentJob)
            }
        }

        else -> Job(parentJob)
    }

fun <T> resolveDispatcher(behavior: KBehavior<T>): CoroutineContext =
    if (behavior.blocking) {
        Dispatchers.IO
    } else {
        Dispatchers.Default
    }

fun <T> CoroutineScope.kactorSystem(behavior: KBehavior<T>): KActorRef<T> {
    return spawnKActor(
        "kactor-system",
        behavior,
        this,
        CoroutineName("kactor-system") + MDCContext(mapOf("kactor" to "kactor-system")),
    )
}

private fun <T> spawnKActor(
    name: String,
    behavior: KBehavior<T>,
    scope: CoroutineScope,
    context: CoroutineContext,
    finally: ((ex: Throwable?) -> Unit)? = null,
): KActorRef<T> {
    val mailbox = Channel<T>(capacity = Channel.UNLIMITED)
    val job =
        scope.launch(context) {
            val actor = KActor(name, mailbox, coroutineContext.job, scope)
            actor.run(behavior)
        }
    finally?.apply { job.invokeOnCompletion(this) }
    return KActorRef(mailbox)
}

// FIXME: Design could be improved

/**
 * FIFO queue of messages.
 */
fun <T> KActorContext<*>.router(
    name: String,
    poolSize: Int,
    behavior: KBehavior<T>,
): KActorRef<T> {
    val job = resolveJob(behavior, job)
    val mailbox = Channel<T>(capacity = Channel.UNLIMITED)

    repeat(poolSize) {
        val context =
            CoroutineName("kactor-routee-$name-$it") +
                job +
                MDCContext(mapOf("kactor" to "kactor-routee-$name-$it"))
        scope.launch(context) {
            val actor = KActor(name, mailbox, coroutineContext.job, this)
            actor.run(behavior)
        }
    }

    // TODO Does it work with the children?
    return KActorRef(mailbox)
}

fun <T, R> CoroutineScope.ask(
    toKActorRef: KActorRef<T>,
    timeoutInMillis: Long = 1000L,
    msgFactory: (ref: KActorRef<R>) -> T,
): Deferred<R> {
    val mailbox = Channel<R>(capacity = Channel.RENDEZVOUS)
    val result =
        async {
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
