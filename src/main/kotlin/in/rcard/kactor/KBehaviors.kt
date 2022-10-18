package `in`.rcard.kactor

object KBehaviors {
    fun <T> receive(ctxAndMessage: (ctx: KActorContext<T>, msg: T) -> KBehavior<T>): KBehavior<T> {
        TODO("Not yet implemented")
    }

    fun <T> same(): KBehavior<T> {
        TODO("Not yet implemented")
    }
}
