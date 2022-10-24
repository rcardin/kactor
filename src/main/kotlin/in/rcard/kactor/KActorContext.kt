package `in`.rcard.kactor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class KActorContext<T> {
    suspend fun <U> spawn(behavior: KBehavior<U>, name: String): KActorRef<U> {
        val mailbox = Channel<U>()
        val actor = KActor(name, KActorContext(), mailbox)
        coroutineScope {
            launch {
                actor.run(behavior)
            }
        }
        return ChannelKActorRef(mailbox)
    }
}
