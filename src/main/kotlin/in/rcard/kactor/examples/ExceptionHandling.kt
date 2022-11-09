package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.stop
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
                    val ref = ctx.kactor("kactor_$it", PrintCount.behavior)
                    ref `!` PrintCount.Count(it)
                }
                stop()
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
