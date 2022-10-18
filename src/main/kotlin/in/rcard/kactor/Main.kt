package `in`.rcard.kactor

// FIXME Just for testing purposes. Delete it in the near future
fun main() {
    val actorSystem: KActors<HelloWorldMain.SayHello> =
        KActors(HelloWorldMain.behavior(), "myKActor")
}

object HelloWorldMain : KActor<HelloWorldMain.SayHello> {
    data class SayHello(val name: String)

    override fun behavior(): KBehavior<SayHello> {
        TODO("Not yet implemented")
    }
}
