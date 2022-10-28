package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope {
    TODO()
}

object v2 {

    object MainActor {

        object Start

//        suspend fun behavior(): KBehavior<Start> = kactor {
//            coroutineScope {
//                for (i in 0..100) {
//                    println("Spawning actor $i")
//                    val actorRef = spawn(HelloWorldActor.behavior(), "HelloWorldActor_$i")
//                    println("Spawned actor $i")
//                    actorRef `!` HelloWorldActor.SayHello("Riccardo")
//                }
//                KBehaviors.same()
//            }
//        }
    }

    // TODO Maybe here we need something different, moving the behavior to the actor?
    fun <T> CoroutineScope.receive(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> {
        TODO()
    }

    fun <T> CoroutineScope.kactor(behavior: KBehavior<T>): KActorRef<T> {
        val mailbox = Channel<T>()
        launch {
            val msg = mailbox.receive()
            behavior.receive(msg)
        }
        return KActorRef(mailbox)
    }

    class KActorRef<T> internal constructor(private val mailbox: SendChannel<T>) {
        suspend fun tell(msg: T) {
            mailbox.send(msg)
        }

        companion object KActorRefOps {
            suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
                tell(msg)
            }
        }
    }

    class KBehavior<T>(private val receivedBehaviour: suspend (msg: T) -> KBehavior<T>) {
        suspend fun receive(msg: T): KBehavior<T> {
            return receivedBehaviour(msg)
        }
    }
}
