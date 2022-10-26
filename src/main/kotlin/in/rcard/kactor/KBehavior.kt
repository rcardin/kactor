package `in`.rcard.kactor

interface KBehavior<in T> {
    suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T>
}

// FIXME: This should be KBehavior<Nothing>
object KBehaviorSame : KBehavior<Any> {
    override suspend fun receive(ctx: KActorContext<Any>, msg: Any): KBehavior<Any> {
        return this
    }
}

class KExtensibleBehavior<T>(private val receivedBehaviour: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override suspend fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receivedBehaviour(ctx, msg)
    }
}
