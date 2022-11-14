package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.SupervisorStrategy
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.spawn
import `in`.rcard.kactor.stopped
import `in`.rcard.kactor.supervise
import kotlinx.coroutines.coroutineScope

object ExceptionHandling {

    suspend fun exceptionHandling() = coroutineScope {
        val mainKActorRef = kactorSystem(MainActor.behavior())
        mainKActorRef `!` MainActor.Start
    }

    object MainActor {

        object Start

        suspend fun behavior(): KBehavior<Start> =
            setup { ctx ->
                repeat(1000) {
                    val ref = ctx.spawn(
                        "kactor_$it",
                        supervise(PrintCount.behavior, withStrategy = SupervisorStrategy.STOP)
                    )
                    ref `!` PrintCount.Count(it)
                }
                stopped()
            }
    }

    object PrintCount {
        data class Count(val value: Int)

        val behavior: KBehavior<Count> = receiveMessage { msg ->
            if (msg.value % 1000 == 0) {
                throw RuntimeException("Boom!")
            }
            println("Received message: ${msg.value}")
            same()
        }
    }
}
