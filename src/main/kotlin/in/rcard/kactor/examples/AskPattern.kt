package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.ask
import `in`.rcard.kactor.kactor
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.same
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope

object AskPattern {

    suspend fun askPattern() = coroutineScope {
        val tellerActor = kactor("teller", TellerActor.behavior)

        val deferred: Deferred<Answer> = ask(tellerActor) { ref ->
            TellerActor.Question(ref)
        }

        println("The answer is: ${deferred.await().msg}")
    }

    data class Answer(val msg: String)

    object TellerActor {

        data class Question(val replyTo: KActorRef<Answer>)

        val behavior: KBehavior<Question> = receive { _, _, msg ->
            msg.replyTo `!` Answer("42")
            same()
        }
    }
}
