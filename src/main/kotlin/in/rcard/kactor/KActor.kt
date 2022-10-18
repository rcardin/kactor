package `in`.rcard.kactor

// FIXME Do we need this interface?
interface KActor<T> {
    fun behavior(): KBehavior<T>
}
