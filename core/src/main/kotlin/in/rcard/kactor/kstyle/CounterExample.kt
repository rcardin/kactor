package `in`.rcard.kactor.kstyle

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.spawn
import kotlinx.coroutines.coroutineScope

object CounterExample {
    suspend fun counterExample() =
        coroutineScope {
            kactorSystem(MainActor.behavior)
        }

    object MainActor {
        val behavior: KkBehavior<Int> =
            setup {
                val counterRef = ctx.spawn("counter", Counter.behavior(0))

                counterRef `!` Counter.Increment(40)
                counterRef `!` Counter.Increment(2)
                ctx.log.info("Getting the value of the counter")
                counterRef `!` Counter.GetValue(ctx.self)
                counterRef `!` Counter.Reset
                counterRef `!` Counter.GetValue(ctx.self)

                receive {
                    ctx.log.info("The counter value is $msg")
                    same()
                }
            }
    }

    object Counter {
        sealed interface Command

        data class Increment(val by: Int) : Command

        object Reset : Command

        data class GetValue(val replyTo: KActorRef<Int>) : Command

        fun behavior(currentValue: Int): KkBehavior<Command> =
            receive {
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
