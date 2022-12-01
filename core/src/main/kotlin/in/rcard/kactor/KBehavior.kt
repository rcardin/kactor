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

internal class KBehaviorSupervised<T>(
    val supervisedBehavior: KBehavior<T>,
    val strategy: SupervisorStrategy = SupervisorStrategy.ESCALATE
) : KBehavior<T>

enum class SupervisorStrategy {
    STOP,
    ESCALATE
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

fun <T> supervise(supervisedBehavior: KBehavior<T>, withStrategy: SupervisorStrategy): KBehavior<T> =
    KBehaviorSupervised(supervisedBehavior, withStrategy)

