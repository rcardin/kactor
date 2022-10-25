package `in`.rcard.kactor

object KBehaviors {
    fun <T> receive(ctxAndMessage: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> {
        return KExtensibleBehavior(ctxAndMessage)
    }

    fun same(): KBehavior<Any> = KBehaviorSame
}
