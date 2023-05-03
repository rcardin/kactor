package `in`.rcard.kactor

sealed interface KBehavior<T> {
    val blocking: Boolean
        get() = false
}

internal object KBehaviorSame : KBehavior<Nothing>

internal object KBehaviorStop : KBehavior<Nothing>

internal class KBehaviorExtension<T>(private val receivedBehaviour: suspend KBehaviorScope.(ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> =
        KBehaviorScope().receivedBehaviour(ctx, msg)
}

internal class KBehaviorSetup<T>(private val setupBehavior: suspend (ctx: KActorContext<T>) -> KBehavior<T>) :
    KBehavior<T> {
    suspend fun setup(ctx: KActorContext<T>): KBehavior<T> {
        return setupBehavior(ctx)
    }
}

internal abstract class KBehaviorDecorator<T>(internal val decorated: KBehavior<T>) : KBehavior<T> {
    override val blocking: Boolean
        get() = decorated.blocking
}

internal class KBehaviorSupervised<T>(
    supervisedBehavior: KBehavior<T>,
    val strategy: SupervisorStrategy = SupervisorStrategy.ESCALATE,
) : KBehaviorDecorator<T>(supervisedBehavior)

enum class SupervisorStrategy {
    STOP,
    ESCALATE,
}

internal class KBehaviorBlocking<T>(decorated: KBehavior<T>) : KBehaviorDecorator<T>(decorated) {
    override val blocking: Boolean
        get() = true
}

internal class KBehaviorWithTimers<T>(internal val timedBehavior: suspend (timer: TimerScheduler<T>) -> KBehavior<T>) :
    KBehavior<T>

fun <T> setup(behavior: suspend KBehaviorScope.(ctx: KActorContext<T>) -> KBehavior<T>): KBehavior<T> =
    KBehaviorSetup { ctx ->
        KBehaviorScope().behavior(ctx)
    }

fun <T> receive(receivedBehaviour: suspend KBehaviorScope.(ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KBehaviorExtension(receivedBehaviour)

fun <T> receiveMessage(receivedBehaviour: suspend KBehaviorScope.(msg: T) -> KBehavior<T>): KBehavior<T> =
    KBehaviorExtension { _, msg ->
        receivedBehaviour(msg)
    }

class KBehaviorScope internal constructor()

@Suppress("UNCHECKED_CAST", "UnusedReceiverParameter")
fun <T> KBehaviorScope.same(): KBehavior<T> = KBehaviorSame as KBehavior<T>

@Suppress("UNCHECKED_CAST", "UnusedReceiverParameter")
fun <T> KBehaviorScope.stopped(): KBehavior<T> = KBehaviorStop as KBehavior<T>

@Suppress("UnusedReceiverParameter")
fun <T> KBehaviorScope.supervise(
    supervisedBehavior: KBehavior<T>,
    withStrategy: SupervisorStrategy,
): KBehavior<T> =
    KBehaviorSupervised(supervisedBehavior, withStrategy)

fun <T> blocking(behavior: KBehavior<T>): KBehavior<T> = KBehaviorBlocking(behavior)

fun <T> withTimers(timedBehavior: suspend (timer: TimerScheduler<T>) -> KBehavior<T>): KBehavior<T> =
    KBehaviorWithTimers(timedBehavior)
