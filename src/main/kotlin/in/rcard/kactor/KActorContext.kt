package `in`.rcard.kactor

class KActorContext<T> {
    suspend fun spawn(behavior: KBehavior<T>, name: String): ChannelKActorRef<T> {
        TODO()
    }
}
