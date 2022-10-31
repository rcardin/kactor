package `in`.rcard.kactor

sealed interface KBehavior<in T> {
    suspend fun receive(msg: T): KBehavior<T>
}

// FIXME: This should be `in`.rcard.kactor.KBehavior<Nothing>
internal object KBehaviorSame : KBehavior<Any> {
    override suspend fun receive(msg: Any): KBehavior<Any> {
        return this
    }
}

internal class KExtensibleBehavior<in T>(private val receivedBehaviour: suspend (msg: T) -> KBehavior<T>) :
    KBehavior<T> {
    override suspend fun receive(msg: T): KBehavior<T> {
        return receivedBehaviour(msg)
    }
}

fun <T> receive(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> =
    KExtensibleBehavior(receivedBehaviour)
fun same(): KBehavior<Any> = KBehaviorSame
