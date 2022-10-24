package `in`.rcard.kactor

object KBehaviors {
    fun <T> receive(ctxAndMessage: suspend (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> {
        return KExtensibleBehavior(ctxAndMessage)
    }

    fun <T> same(): KBehavior<T> {
        TODO("Not yet implemented")
    }
}
