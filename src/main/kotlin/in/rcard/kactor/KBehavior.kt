package `in`.rcard.kactor

sealed interface KBehavior<T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T>
}

// FIXME: This should be `in`.rcard.kactor.KBehavior<Nothing>
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

fun <T> receive(receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior(receivedBehaviour)

@Suppress("UNCHECKED_CAST")
fun <T> same(): KBehavior<T> = KBehaviorSame as KBehavior<T>
