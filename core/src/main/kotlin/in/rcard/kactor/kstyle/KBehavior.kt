package `in`.rcard.kactor.kstyle

import `in`.rcard.kactor.KActorContext
import `in`.rcard.kactor.SupervisorStrategy
import `in`.rcard.kactor.TimerScheduler

sealed interface KkBehavior<T> {
    val blocking: Boolean
        get() = false
}

internal object KkBehaviorSame : KkBehavior<Nothing>

internal object KkBehaviorStop : KkBehavior<Nothing>

internal class KkBehaviorExtension<T>(private val receivedBehaviour: suspend KkBehaviorScope<T>.() -> KkBehavior<T>) :
    KkBehavior<T> {
    suspend fun receive(
        ctx: KActorContext<T>,
        msg: T,
    ): KkBehavior<T> = KkBehaviorScope(ctx, msg).receivedBehaviour()
}

internal class KkBehaviorSetup<T>(private val setupBehavior: suspend (ctx: KActorContext<T>) -> KkBehavior<T>) :
    KkBehavior<T> {
    suspend fun setup(ctx: KActorContext<T>): KkBehavior<T> {
        return setupBehavior(ctx)
    }
}

internal abstract class KkBehaviorDecorator<T>(private val decorated: KkBehavior<T>) : KkBehavior<T> {
    override val blocking: Boolean
        get() = decorated.blocking
}

internal class KkBehaviorSupervised<T>(
    supervisedBehavior: KkBehavior<T>,
    val strategy: SupervisorStrategy = SupervisorStrategy.ESCALATE,
) : KkBehaviorDecorator<T>(supervisedBehavior)

enum class SupervisorStrategy {
    STOP,
    ESCALATE,
}

internal class KkBehaviorBlocking<T>(decorated: KkBehavior<T>) : KkBehaviorDecorator<T>(decorated) {
    override val blocking: Boolean
        get() = true
}

internal class KkBehaviorWithTimers<T>(internal val timedBehavior: suspend (timer: TimerScheduler<T>) -> KkBehavior<T>) :
    KkBehavior<T>

fun <T> setup(behavior: suspend KkBehaviorScopeOnlyCtx<T>.() -> KkBehavior<T>): KkBehavior<T> =
    KkBehaviorSetup { ctx ->
        KkBehaviorScopeOnlyCtx(ctx).behavior()
    }

fun <T> receive(receivedBehaviour: suspend KkBehaviorScope<T>.() -> KkBehavior<T>): KkBehavior<T> = KkBehaviorExtension(receivedBehaviour)

// fun <T> receiveMessage(receivedBehaviour: suspend KkBehaviorScope.(msg: T) -> KkBehavior<T>): KkBehavior<T> =
//    KkBehaviorExtension { _, msg ->
//        receivedBehaviour(msg)
//    }

class KkBehaviorScopeOnlyCtx<T> internal constructor(val ctx: KActorContext<T>)

class KkBehaviorScope<T> internal constructor(val ctx: KActorContext<T>, val msg: T)

@Suppress("UNCHECKED_CAST", "UnusedReceiverParameter")
fun <T> KkBehaviorScope<T>.same(): KkBehavior<T> = KkBehaviorSame as KkBehavior<T>

@Suppress("UNCHECKED_CAST", "UnusedReceiverParameter")
fun <T> KkBehaviorScope<T>.stopped(): KkBehavior<T> = KkBehaviorStop as KkBehavior<T>

@Suppress("UnusedReceiverParameter")
fun <T> KkBehaviorScope<T>.supervise(
    supervisedBehavior: KkBehavior<T>,
    withStrategy: SupervisorStrategy,
): KkBehavior<T> = KkBehaviorSupervised(supervisedBehavior, withStrategy)

fun <T> blocking(behavior: KkBehavior<T>): KkBehavior<T> = KkBehaviorBlocking(behavior)

fun <T> withTimers(timedBehavior: suspend (timer: TimerScheduler<T>) -> KkBehavior<T>): KkBehavior<T> = KkBehaviorWithTimers(timedBehavior)
