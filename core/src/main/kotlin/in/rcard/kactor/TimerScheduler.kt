package `in`.rcard.kactor

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

class TimerScheduler<K, T>(private val scope: CoroutineScope, private val actorRef: KActorRef<T>) {

    private val timers = mutableMapOf<K, Job>()
    suspend fun startSingleTimer(timerKey: K, msg: T, delayTime: Duration) {
        val flowJob = scope.launch {
            flow {
                while (true) {
                    delay(delayTime)
                    emit(msg)
                }
            }.onEach { actorRef `!` it }.cancellable().collect {}
        }
        timers[timerKey] = flowJob
    }

    fun cancel(timerKey: K) {
        timers[timerKey]?.cancel("Timer with key $timerKey cancelled")
    }
}
