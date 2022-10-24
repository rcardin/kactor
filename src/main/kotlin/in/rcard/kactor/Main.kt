package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.runBlocking

// FIXME Just for testing purposes. Delete it in the near future
fun main() {
    runBlocking {
        val actorSystem: KActorRef<HelloWorldMain.SayHello> =
            KActorSystem(HelloWorldMain.behavior(), "myKActor")

        actorSystem `!` HelloWorldMain.SayHello("Riccardo")
    }
}

object HelloWorldMain {
    data class SayHello(val name: String)

    fun behavior(): KBehavior<SayHello> =
        KBehaviors.receive { ctx, msg ->
            println("Hello ${msg.name}!")
            KBehaviors.same()
        }
}
