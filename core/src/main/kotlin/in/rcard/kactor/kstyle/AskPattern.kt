package `in`.rcard.kactor.kstyle

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.ask
import `in`.rcard.kactor.kactorSystem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope

object AskPattern {
    suspend fun askPattern() =
        coroutineScope {
            val tellerActor = kactorSystem(TellerActor.behavior)

            // FIXME: What if the ask is inside the behavior of an actor?
            val deferred: Deferred<Answer> =
                ask(tellerActor) { ref ->
                    TellerActor.Question(ref)
                }

            println("The answer is: ${deferred.await().msg}")
        }

    data class Answer(val msg: String)

    object TellerActor {
        data class Question(val replyTo: KActorRef<Answer>)

        val behavior: KkBehavior<Question> =
            receive {
                msg.replyTo `!` Answer("42")
                same()
            }
    }
}
