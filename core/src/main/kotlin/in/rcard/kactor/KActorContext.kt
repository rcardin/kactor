package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KActorContext<T> internal constructor(
    val self: KActorRef<T>,
    name: String,
    val children: List<KActorRef<*>> = emptyList(),
    internal val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    val log: Logger = LoggerFactory.getLogger(name)
}
