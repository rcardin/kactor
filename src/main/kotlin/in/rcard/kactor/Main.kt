package `in`.rcard.kactor

// FIXME Just for testing purposes. Delete it in the near future
fun main() {
    val actorSystem: KActorSystem<HelloWorldMain.SayHello> =
        KActorSystem(HelloWorldMain.behavior(), "myKActor")
}

object HelloWorldMain {
    data class SayHello(val name: String)

    fun behavior(): KBehavior<SayHello> =
        KBehaviors.receive { ctx, msg ->
            println("Hello ${msg.name}!")
            KBehaviors.same()
        }
}
