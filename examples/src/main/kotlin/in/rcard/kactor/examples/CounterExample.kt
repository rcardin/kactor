package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same
import `in`.rcard.kactor.spawn

object CounterExample {
    suspend fun counterExample() =
        kactorSystem { ctx ->
            val counterRef = ctx.spawn("counter", Counter.behavior(0))

            counterRef `!` Counter.Increment(40)
            counterRef `!` Counter.Increment(2)
            ctx.log.info("Getting the value of the counter")
            counterRef `!` Counter.GetValue(ctx.self)
            counterRef `!` Counter.Reset
            counterRef `!` Counter.GetValue(ctx.self)

            receiveMessage { msg ->
                ctx.log.info("The counter value is $msg")
                same()
            }
        }

    object MainActor

    object Counter {
        sealed interface Command

        data class Increment(val by: Int) : Command

        object Reset : Command

        data class GetValue(val replyTo: KActorRef<Int>) : Command

        fun behavior(currentValue: Int): KBehavior<Command> =
            receiveMessage { msg ->
                when (msg) {
                    is Increment -> behavior(currentValue + msg.by)
                    is Reset -> behavior(0)
                    is GetValue -> {
                        msg.replyTo `!` currentValue
                        same()
                    }
                }
            }
    }
}
