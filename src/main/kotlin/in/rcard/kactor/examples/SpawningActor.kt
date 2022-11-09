package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.examples.SpawningActor.MainActor.ReplyReceived
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.spawn
import kotlinx.coroutines.coroutineScope

/**
 * This example shows how to spawn an actor from another actor.
 */
object SpawningActor {
    suspend fun spawningActor() = coroutineScope {
        kactorSystem(MainActor.behavior)
    }

    object HelloWorldActor {
        data class SayHello(val name: String, val replyTo: KActorRef<ReplyReceived>)

        val behavior: KBehavior<SayHello> =
            receiveMessage { msg ->
                println("Hello ${msg.name}!")
                msg.replyTo `!` ReplyReceived
                same()
            }
    }

    object MainActor {
        object ReplyReceived

        val behavior: KBehavior<ReplyReceived> =
            setup { ctx ->
                for (i in 0..100) {
                    val helloWorldActorRef = ctx.spawn("kactor_$i", HelloWorldActor.behavior)
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Actor $i", ctx.actorRef)
                }
                receiveAndCount(0)
            }
    }

    private fun receiveAndCount(counted: Int): KBehavior<ReplyReceived> =
        receiveMessage {
            println("Received message: $counted")
            receiveAndCount(counted + 1)
        }
}
