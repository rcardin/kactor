package `in`.rcard.kactor

interface KBehavior<T> {
    fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T>
}

object KBehaviorSame : KBehavior<Nothing> {
    override fun receive(ctx: KActorContext<Nothing>, msg: Nothing): KBehavior<Nothing> {
        return this
    }
}

class KExtensibleBehavior<T>(receive: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override fun receive(ctx: KActorContext<T>, msg: T): KBehavior<T> {
        return receive(ctx, msg)
    }
}
