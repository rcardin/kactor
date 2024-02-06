package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.examples.SpawningActor.MainActor.ReplyReceived
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.spawn
import kotlinx.coroutines.coroutineScope

/**
 * This example shows how to spawn an actor from another actor.
 */
object SpawningActor {
    suspend fun spawningActor() =
        coroutineScope {
            kactorSystem(MainActor.behavior)
        }

    object HelloWorldActor {
        data class SayHello(val name: String, val replyTo: KActorRef<ReplyReceived>)

        val behavior: KBehavior<SayHello> =
            receive { ctx, msg ->
                ctx.log.info("Hello ${msg.name}!")
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
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Actor $i", ctx.self)
                    ctx.log.info("Sent message to actor $i")
                }
                receiveAndCount(0)
            }
    }

    private fun receiveAndCount(counted: Int): KBehavior<ReplyReceived> =
        receive { ctx, _ ->
            ctx.log.info("Received message: $counted")
            receiveAndCount(counted + 1)
        }
}
