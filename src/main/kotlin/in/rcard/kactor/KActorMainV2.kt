package `in`.rcard.kactor

import `in`.rcard.kactor.v2.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.v2.kactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main(): Unit = coroutineScope {
    val mainActorRef = kactor(v2.MainActor.behavior())
    mainActorRef `!` v2.MainActor.Start
}

object v2 {
    object HelloWorldActor {
        data class SayHello(val name: String)

        suspend fun behavior(): KBehavior<SayHello> = coroutineScope {
            receive {
                println("Hello ${it.name}!")
                KBehaviors.same()
            }
        }
    }

    object MainActor {

        object Start

        suspend fun behavior(): KBehavior<Start> = coroutineScope {
            receive {
                for (i in 0..100) {
                    println("Spawning actor $i")
                    val helloWorldActorRef = kactor(HelloWorldActor.behavior())
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Riccardo")
                }
                KBehaviors.same()
            }
        }
    }

    // TODO Maybe here we need something different, moving the behavior to the actor?
    fun <T> receive(receivedBehaviour: suspend (msg: T) -> KBehavior<T>): KBehavior<T> {
        return KExtensibleBehavior(receivedBehaviour)
    }

    fun <T> CoroutineScope.kactor(behavior: KBehavior<T>): KActorRef<T> {
        val mailbox = Channel<T>()
        launch {
            val actor = KActor(mailbox)
            actor.run(behavior)
        }
        return KActorRef(mailbox)
    }

    class KActor<T>(private val receiveChannel: ReceiveChannel<T>) {
        suspend fun run(behavior: KBehavior<T>) {
            val msg = receiveChannel.receive()
            when (val newBehavior = behavior.receive(msg)) {
                is KBehaviorSame -> run(behavior)
                is KExtensibleBehavior -> run(newBehavior)
            }
        }
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

    interface KBehavior<in T> {
        suspend fun receive(msg: T): KBehavior<T>
    }

    // FIXME: This should be KBehavior<Nothing>
    object KBehaviorSame : KBehavior<Any> {
        override suspend fun receive(msg: Any): KBehavior<Any> {
            return this
        }
    }

    class KExtensibleBehavior<in T>(private val receivedBehaviour: suspend (msg: T) -> KBehavior<T>) :
        KBehavior<T> {
        override suspend fun receive(msg: T): KBehavior<T> {
            return receivedBehaviour(msg)
        }
    }

    object KBehaviors {
        fun <T> receive(msgToBehavior: suspend (msg: T) -> KBehavior<T>): KBehavior<T> {
            return KExtensibleBehavior(msgToBehavior)
        }

        fun same(): KBehavior<Any> = KBehaviorSame
    }
}
