package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.finally
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.spawn
import `in`.rcard.kactor.stopped
import kotlinx.coroutines.coroutineScope

object ResourcePattern {

    suspend fun kactorFromResourceExample() = coroutineScope {
        val mainKActor = kactorSystem(MainActor.behavior())
        mainKActor `!` MainActor.Start
    }

    class Resource(val name: String) : AutoCloseable {
        override fun close() {
            println("Closing resource '$name'")
        }
    }

    object MainActor {
        object Start

        fun behavior(): KBehavior<Start> = receive { ctx, _ ->
            val resourceKActorRef = Resource("MyResource").run {
                ctx.spawn(
                    "resourceKactor",
                    ResourceActor.behavior(this).finally { close() }
                )
            }
            resourceKActorRef `!` ResourceActor.UseIt
            same()
        }
    }

    object ResourceActor {
        object UseIt

        fun behavior(res: Resource): KBehavior<UseIt> = receiveMessage { _ ->
            println("Using resource '${res.name}'")
            // throw RuntimeException("Letting crash the kactor")
            stopped()
        }
    }
}
