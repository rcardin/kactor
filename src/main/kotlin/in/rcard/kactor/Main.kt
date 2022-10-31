package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.coroutineScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main() = coroutineScope {
//    val mainActorRef = kactor("main", MainActor.behavior)
//    mainActorRef `!` MainActor.Start

    val tellerActor = kactor("teller", TellerActor.behavior)
    val askerActor = kactor("asker", AskerActor.behavior(tellerActor))

    askerActor `!` AskerActor.Start
}

object TellerActor {

    data class Question(val replyTo: KActorRef<AskerActor.Command>)

    val behavior: KBehavior<Question> = receive { ctx, msg ->
        msg.replyTo `!` AskerActor.Answer("42")
        same()
    }
}

object AskerActor {

    sealed interface Command
    object Start : Command
    data class Answer(val msg: String) : Command

    fun behavior(teller: KActorRef<TellerActor.Question>): KBehavior<Command> =
        receive { ctx, msg ->
            when (msg) {
                is Start -> {
                    teller `!` TellerActor.Question(ctx.actorRef)
                }

                is Answer -> {
                    println("The question is: ${msg.msg}")
                }
            }
            same()
        }
}

object HelloWorldActor {
    data class SayHello(val name: String)

    val behavior: KBehavior<SayHello> =
        receive { ctx, msg ->
            println("Hello ${msg.name}!")
            same()
        }
}

object MainActor {

    object Start

    val behavior: KBehavior<Start> =
        receive { _, _ ->
            coroutineScope {
                for (i in 0..100) {
                    val helloWorldActorRef = kactor("kactor_$i", HelloWorldActor.behavior)
                    helloWorldActorRef `!` HelloWorldActor.SayHello("Actor $i")
                }
            }
            same()
        }
}
