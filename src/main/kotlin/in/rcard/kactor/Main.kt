package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.runBlocking

// FIXME Just for testing purposes. Delete it in the near future
fun main() {
    runBlocking {
        val actorSystem: KActorRef<MainActor.Start> =
            KActorSystem(MainActor.behavior(), "myKActor")

        actorSystem `!` MainActor.Start
    }
}

object MainActor {

    object Start

    fun behavior(): KBehavior<Start> = KBehaviors.receive { ctx, msg ->
        for (i in 0..100) {
            val actorRef = ctx.spawn(HelloWorldActor.behavior(), "HelloWorldActor_$i")
            actorRef `!` HelloWorldActor.SayHello("Riccardo")
        }
        KBehaviors.same()
    }
}

object HelloWorldActor {
    data class SayHello(val name: String)

    fun behavior(): KBehavior<SayHello> = KBehaviors.receive { _, msg ->
        println("Hello ${msg.name}!")
        KBehaviors.same()
    }
}
