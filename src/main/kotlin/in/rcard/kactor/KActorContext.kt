package `in`.rcard.kactor

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch

class KActorContext<out T> {
    suspend fun <U> spawn(behavior: KBehavior<U>, name: String): KActorRef<U> {
        val mailbox = Channel<U>(UNLIMITED)
        val actor = KActor(name, KActorContext(), mailbox)
        println("Creating actor '$name'")
        GlobalScope.launch {
            actor.run(behavior)
            println("Actor '$name' created")
        }
        return ChannelKActorRef(mailbox)
    }
}
