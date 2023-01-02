package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.same
import `in`.rcard.kactor.spawn
import kotlinx.coroutines.coroutineScope

object FinallyPattern {

    suspend fun finallyPattern() = coroutineScope {
        val mainActor = kactorSystem(MainActor.behavior)
        mainActor `!` MainActor.Start
    }

    class Resource(val name: String) : AutoCloseable {
        init {
            println("Resource $name created")
        }

        override fun close() {
            println("Resource $name closed")
        }
    }

    object MainActor {
        object Start

        val behavior: KBehavior<Start> = receive { ctx, _ ->
            val res = Resource("my-resource")
            val kRef = ctx.spawn(
                "resKactor",
                ResourceKActor.behavior(res),
                finally = { res.close() }
            )
            kRef `!` ResourceKActor.UseIt
            same()
        }
    }

    object ResourceKActor {
        object UseIt

        fun behavior(res: Resource): KBehavior<UseIt> = receive { ctx, _ ->
            ctx.log.info("Using resource ${res.name}")
            throw RuntimeException("Something went wrong")
        }
    }
}
