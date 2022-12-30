package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.TimerScheduler
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.setup
import `in`.rcard.kactor.stopped
import `in`.rcard.kactor.withTimers
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.seconds

object TimersExample {

    suspend fun timersExample() = coroutineScope {
        kactorSystem(MainActor.behavior())
    }

    object MainActor {

        object TimerKey
        object Tick

        fun behavior(): KBehavior<Tick> = setup { _ ->
            withTimers { timers ->
                timers.startSingleTimer(TimerKey, Tick, 1.seconds)
                processTick(0, timers)
            }
        }

        private fun processTick(counter: Int, timers: TimerScheduler<Tick>): KBehavior<Tick> =
            receive { ctx, _ ->
                ctx.log.info("Another second passed")
                if (counter == 10) {
                    timers.cancel(TimerKey)
                    stopped()
                } else {
                    processTick(counter + 1, timers)
                }
            }
    }
}
