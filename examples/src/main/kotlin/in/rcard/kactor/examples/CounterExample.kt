package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef
import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.receiveMessage
import `in`.rcard.kactor.same

class CounterExample {

    object Counter {
        sealed interface Command
        data class Increment(val by: Int) : Command
        data class Decrement(val by: Int) : Command
        object Reset : Command
        data class GetValue(val replyTo: KActorRef<Int>) : Command

        fun behavior(currentValue: Int): KBehavior<Command> = receiveMessage { msg ->
            when (msg) {
                is Counter.Increment -> behavior(currentValue + msg.by)
                is Counter.Decrement -> behavior(currentValue - msg.by)
                is Counter.Reset -> behavior(0)
                is Counter.GetValue -> {
                    msg.replyTo `!` currentValue
                    same()
                }
            }
        }
    }
}
