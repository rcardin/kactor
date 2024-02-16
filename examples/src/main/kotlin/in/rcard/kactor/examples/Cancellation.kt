package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.spawn
import `in`.rcard.kactor.stopped
import kotlinx.coroutines.coroutineScope

object Cancellation {
    suspend fun cancellationExample() =
        coroutineScope {
            val mainKActor = kactorSystem(MainActor())
            mainKActor `!` MainActor.Start
        }

    object MainActor {
        object Start

        operator fun invoke(): KBehavior<Start> =
            setup { ctx ->
                for (i in 1..100) {
                    val ref =
                        ctx.spawn("child_$i", ChildActor(i), finally = { println("Child $i has been stopped") })
                    ref `!` ChildActor.Start
                }
                receiveMessage { msg ->
                    ctx.log.info("Received message: $msg")
                    stopped()
                }
            }
    }

    object ChildActor {
        object Start

        operator fun invoke(index: Int): KBehavior<Start> =
            setup { ctx ->
                ctx.log.info("Starting $index actor")
                receiveMessage { msg ->
                    ctx.log.info("Received message: $msg")
                    same()
                }
            }
    }
}
