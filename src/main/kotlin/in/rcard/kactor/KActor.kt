package `in`.rcard.kactor

interface KActor<T> {
    fun behavior(): KBehavior<T>
}
