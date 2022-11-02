package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import kotlinx.coroutines.coroutineScope

object ReceiveMessagePattern {
    suspend fun receiveMessagePattern() = coroutineScope {
        val mainActorRef = kactor("main", MainActor.behavior)
        mainActorRef `!` MainActor.Start
    }

    object MainActor {

        object Start

        val behavior: KBehavior<Start> =
            receiveMessage { msg ->
                println("Received message: $msg")
                same()
            }
    }
}
