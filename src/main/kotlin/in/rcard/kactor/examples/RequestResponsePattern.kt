package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.same
import kotlinx.coroutines.coroutineScope

/**
 * This example shows how to implement the request/response pattern using the tell pattern.
 */
object RequestResponsePattern {
    suspend fun requestResponsePattern() = coroutineScope {
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
}
