package `in`.rcard.kactor

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope

class KActorContext<out T> {
    suspend fun <U> spawn(behavior: KBehavior<U>, name: String): KActorRef<U> {
        val mailbox = Channel<U>(UNLIMITED)
        val actor = KActor(name, KActorContext(), mailbox)
        println("Creating actor '$name'")
        coroutineScope {
            async {
                actor.run(behavior)
            }
        }
        println("Actor '$name' created")
        return ChannelKActorRef(mailbox)
    }
}
