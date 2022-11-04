package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import kotlinx.coroutines.supervisorScope

object ExceptionHandling {

    suspend fun exceptionHandling() = supervisorScope {
        val mainKActorRef = kactor("main", MainActor.behavior())
        mainKActorRef `!` MainActor.Start
    }

    object MainActor {

        object Start

        suspend fun behavior(): KBehavior<Start> =
            receive { scope, _, _ ->
                repeat(1000) {
                    val ref = scope.kactor("kactor_$it", PrintCount.behavior)
                    ref `!` PrintCount.Count(it)
                }
                same()
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
