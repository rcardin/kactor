package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.coroutineScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main() = coroutineScope {
    val mainActorRef = kactor("main", MainActor.behavior())
    mainActorRef `!` MainActor.Start
}

object HelloWorldActor {
    data class SayHello(val name: String)

    suspend fun behavior(): KBehavior<SayHello> = coroutineScope {
        receive {
            println("Hello ${it.name}!")
            same()
        }
    }
}

object MainActor {

    object Start

    suspend fun behavior(): KBehavior<Start> =
        receive {
            coroutineScope {
                for (i in 0..100) {
                    println("Spawning actor $i")
                    val helloWorldActorRef = kactor("kactor_$i", HelloWorldActor.behavior())
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Riccardo")
                }
            }
            same()
        }
}
