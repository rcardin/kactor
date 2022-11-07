package `in`.rcard.kactor

sealed interface KBehavior<T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T>
}

internal object KBehaviorSame : KBehavior<Nothing> {
    override suspend fun receive(ctx: KActorContext<Nothing>, msg: Nothing): KBehavior<Nothing> {
        return this
    }
}

internal class KExtensibleBehavior<T>(private val receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receivedBehaviour(ctx, msg)
    }
}

// fun <T> setup(behavior: suspend (ctx: KActorContext<T>) -> KBehavior<T>): KBehavior<T> =
//    KExtensibleBehavior { ctx, _ ->
//        behavior(ctx)
//    }

fun <T> receive(receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior(receivedBehaviour)

fun <T> receiveMessage(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior { _, msg ->
        receivedBehaviour(msg)
    }

@Suppress("UNCHECKED_CAST")
fun <T> same(): KBehavior<T> = KBehaviorSame as KBehavior<T>
