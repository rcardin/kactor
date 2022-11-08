package `in`.rcard.kactor

sealed interface KBehavior<T>

internal object KBehaviorSame : KBehavior<Nothing>

internal object KBehaviorStop : KBehavior<Nothing>

internal class KExtensibleBehavior<T>(private val receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receivedBehaviour(ctx, msg)
    }
}

internal class KSetupBehavior<T>(private val setupBehavior: suspend (ctx: KActorContext<T>) -> KBehavior<T>) :
    KBehavior<T> {
    suspend fun setup(ctx: KActorContext<T>): KBehavior<T> {
        return setupBehavior(ctx)
    }
}

fun <T> setup(behavior: suspend (ctx: KActorContext<T>) -> KBehavior<T>): KBehavior<T> =
    KSetupBehavior { ctx ->
        behavior(ctx)
    }

fun <T> receive(receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior(receivedBehaviour)

fun <T> receiveMessage(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior { _, msg ->
        receivedBehaviour(msg)
    }

@Suppress("UNCHECKED_CAST")
fun <T> same(): KBehavior<T> = KBehaviorSame as KBehavior<T>

@Suppress("UNCHECKED_CAST")
fun <T> stop(): KBehavior<T> = KBehaviorStop as KBehavior<T>
