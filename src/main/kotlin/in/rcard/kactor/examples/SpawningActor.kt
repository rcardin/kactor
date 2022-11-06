package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import kotlinx.coroutines.coroutineScope

/**
 * This example shows how to spawn an actor from another actor.
 */
object SpawningActor {
    suspend fun spawningActor() = coroutineScope {
        val mainActor = kactorSystem(MainActor.behavior)
        mainActor `!` MainActor.Start
    }

    object HelloWorldActor {
        data class SayHello(val name: String)

        val behavior: KBehavior<SayHello> =
            receiveMessage { msg ->
                println("Hello ${msg.name}!")
                same()
            }
    }

    object MainActor {

        object Start

        val behavior: KBehavior<Start> =
            receive { ctx, _ ->
                for (i in 0..100) {
                    val helloWorldActorRef = ctx.kactor("kactor_$i", HelloWorldActor.behavior)
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Actor $i")
                }
                same()
            }
    }
}
