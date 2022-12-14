package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.spawn
import `in`.rcard.kactor.stopped
import kotlinx.coroutines.coroutineScope

/**
 * This example shows how to implement the request/response pattern using the tell pattern.
 */
object RequestResponsePattern {
    suspend fun requestResponsePattern() = coroutineScope {
        val mainActorRef = kactorSystem(MainActor.behavior)

        mainActorRef `!` MainActor.Start
    }

    object MainActor {

        object Start

        val behavior: KBehavior<Start> =
            setup { ctx ->
                val tellerActor = ctx.spawn("teller", TellerActor.behavior)
                val askerActor = ctx.spawn("asker", AskerActor.behavior(tellerActor))

                askerActor `!` AskerActor.Start
                stopped()
            }
    }

    object TellerActor {

        data class Question(val replyTo: KActorRef<AskerActor.Command>)

        val behavior: KBehavior<Question> = receiveMessage { msg ->
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
                        teller `!` TellerActor.Question(ctx.self)
                    }

                    is Answer -> {
                        println("The answer is: ${msg.msg}")
                    }
                }
                same()
            }
    }
}
