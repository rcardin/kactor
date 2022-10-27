package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.coroutineScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main() = coroutineScope {
    val mainActorRef = spawn(MainActor.behavior(), "myKActor")
    mainActorRef `!` MainActor.Start
}

object MainActor {

    object Start

    suspend fun behavior(): KBehavior<Start> = KBehaviors.receive { ctx, msg ->
        coroutineScope {
            for (i in 0..100) {
                println("Spawning actor $i")
                val actorRef = spawn(HelloWorldActor.behavior(), "HelloWorldActor_$i")
                println("Spawned actor $i")
                actorRef `!` HelloWorldActor.SayHello("Riccardo")
            }
            KBehaviors.same()
        }
    }
}

object HelloWorldActor {
    data class SayHello(val name: String)

    suspend fun behavior(): KBehavior<SayHello> = KBehaviors.receive { _, msg ->
        println("Hello ${msg.name}!")
        KBehaviors.same()
    }
}
