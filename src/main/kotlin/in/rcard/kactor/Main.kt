package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.runBlocking

// FIXME Just for testing purposes. Delete it in the near future
fun main() {
    runBlocking {
        val actorSystem: KActorRef<HelloWorldActor.SayHello> =
            KActorSystem(HelloWorldActor.behavior(), "myKActor")

        actorSystem `!` HelloWorldActor.SayHello("Riccardo")
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
