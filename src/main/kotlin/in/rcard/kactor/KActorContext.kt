package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class KActorContext<out T> {
    suspend fun <U> spawn(behavior: KBehavior<U>, name: String): KActorRef<U> {
        val mailbox = Channel<U>()
        coroutineScope {
            launch {
                val actor = KActor(name, KActorContext(), mailbox)
                println("Creating actor '$name'")
                actor.run(behavior)
                println("Actor '$name' created")
            }
        }
        return ChannelKActorRef(mailbox)
    }
}

fun <U> CoroutineScope.spawn(behavior: KBehavior<U>, name: String): KActorRef<U> {
    val mailbox = Channel<U>()
    launch {
        val actor = KActor(name, KActorContext(), mailbox)
        println("Creating actor '$name'")
        actor.run(behavior)
        println("Actor '$name' created")
    }
    return ChannelKActorRef(mailbox)
}
