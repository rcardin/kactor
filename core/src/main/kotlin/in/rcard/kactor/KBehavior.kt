package `in`.rcard.kactor

sealed interface KBehavior<T> {
    val blocking: Boolean
        get() = false
}

internal object KBehaviorSame : KBehavior<Nothing>

internal object KBehaviorStop : KBehavior<Nothing>

internal class KBehaviorExtension<T>(private val receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receivedBehaviour(ctx, msg)
    }
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
    val strategy: SupervisorStrategy = SupervisorStrategy.ESCALATE
) : KBehaviorDecorator<T>(supervisedBehavior)

enum class SupervisorStrategy {
    STOP,
    ESCALATE
}

internal class KBehaviorBlocking<T>(decorated: KBehavior<T>) : KBehaviorDecorator<T>(decorated) {
    override val blocking: Boolean
        get() = true
}

fun <T> setup(behavior: suspend (ctx: KActorContext<T>) -> KBehavior<T>): KBehavior<T> =
    KBehaviorSetup { ctx ->
        behavior(ctx)
    }

fun <T> receive(receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KBehaviorExtension(receivedBehaviour)

fun <T> receiveMessage(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> =
    KBehaviorExtension { _, msg ->
        receivedBehaviour(msg)
    }

@Suppress("UNCHECKED_CAST")
fun <T> same(): KBehavior<T> = KBehaviorSame as KBehavior<T>

@Suppress("UNCHECKED_CAST")
fun <T> stopped(): KBehavior<T> = KBehaviorStop as KBehavior<T>

fun <T> supervise(
    supervisedBehavior: KBehavior<T>,
    withStrategy: SupervisorStrategy
): KBehavior<T> =
    KBehaviorSupervised(supervisedBehavior, withStrategy)

fun <T> blocking(behavior: KBehavior<T>): KBehavior<T> = KBehaviorBlocking(behavior)

fun <T> withTimers(timedBehavior: suspend (timer: TimerScheduler) -> KBehavior<T>): KBehavior<T> =
    TODO()
