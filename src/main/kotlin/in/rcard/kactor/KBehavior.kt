package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope

sealed interface KBehavior<T> {
    suspend fun receive(scope: CoroutineScope, ctx: KActorContext<T>, msg: T): KBehavior<T>
}

internal object KBehaviorSame : KBehavior<Nothing> {
    override suspend fun receive(scope: CoroutineScope, ctx: KActorContext<Nothing>, msg: Nothing): KBehavior<Nothing> {
        return this
    }
}

internal class KExtensibleBehavior<T>(private val receivedBehaviour: suspend (scope: CoroutineScope, ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override suspend fun receive(scope: CoroutineScope, ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receivedBehaviour(scope, ctx, msg)
    }
}

fun <T> receive(receivedBehaviour: suspend (scope: CoroutineScope, ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior(receivedBehaviour)

fun <T> receiveMessage(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior { _, _, msg ->
        receivedBehaviour(msg)
    }

@Suppress("UNCHECKED_CAST")
fun <T> same(): KBehavior<T> = KBehaviorSame as KBehavior<T>
