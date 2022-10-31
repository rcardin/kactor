package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class KActor<T>(private val name: String, private val receiveChannel: ReceiveChannel<T>) {
    suspend fun run(behavior: KBehavior<T>) {
        println("Actor '$name' waiting for new message")
        val msg = receiveChannel.receive()
        when (val newBehavior = behavior.receive(msg)) {
            is KBehaviorSame -> {
                println("Actor '$name' continuing with the same behavior")
                run(behavior)
            }

            is KExtensibleBehavior -> {
                println("Actor '$name' continuing with a new behavior")
                run(newBehavior)
            }
        }
    }
}

fun <T> CoroutineScope.kactor(name: String, behavior: KBehavior<T>): KActorRef<T> {
    println("Creating actor '$name'")
    val mailbox = Channel<T>(capacity = Channel.UNLIMITED)
    launch {
        val actor = KActor(name, mailbox)
        actor.run(behavior)
    }
    println("Created actor '$name'")
    return KActorRef(name, mailbox)
}
