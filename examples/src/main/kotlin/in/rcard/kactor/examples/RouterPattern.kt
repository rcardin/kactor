package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.router
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.stopped
import kotlinx.coroutines.coroutineScope

object RouterPattern {
    suspend fun routerPattern() =
        coroutineScope {
            val mainKActorRef = kactorSystem(MainActor.behavior())
            mainKActorRef `!` MainActor.Start
        }

    object MainActor {
        object Start

        fun behavior(): KBehavior<Start> =
            setup { ctx ->
                val routeRef = ctx.router("greeter", 1000, Greeter.behavior)
                repeat(1000) {
                    routeRef `!` Greeter.Greet(it)
                }
                stopped()
            }
    }

    object Greeter {
        data class Greet(val count: Int)

        val behavior: KBehavior<Greet> =
            receive { ctx, msg ->
                ctx.log.info("Greeting for the ${msg.count} time")
                same()
            }
    }
}
