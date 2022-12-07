package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration

class TimerScheduler<T>(private val scope: CoroutineScope, private val actorRef: KActorRef<T>) {

    private val timers = mutableMapOf<Any, Job>()
    suspend fun <K : Any> startSingleTimer(timerKey: K, msg: T, delayTime: Duration) {
        val flowJob = flow {
            while (true) {
                delay(delayTime)
                emit(msg)
            }
        }.onEach { actorRef `!` it }.cancellable().launchIn(scope)
        timers[timerKey] = flowJob
    }

    fun <K : Any> cancel(timerKey: K) {
        timers[timerKey]?.cancel("Timer with key $timerKey cancelled")
    }
}
