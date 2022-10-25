package `in`.rcard.kactor

interface KBehavior<in T> {
    fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T>
}

// FIXME: This should be KBehavior<Nothing>
object KBehaviorSame : KBehavior<Any> {
    override fun receive(ctx: KActorContext<Any>, msg: Any): KBehavior<Any> {
        return this
    }
}

class KExtensibleBehavior<T>(receive: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receive(ctx, msg)
    }
}
