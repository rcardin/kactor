package `in`.rcard.kactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

class KActorContext<T> internal constructor(
    val self: KActorRef<T>,
    name: String,
    private val _children: LinkedBlockingQueue<KActorRef<*>> = LinkedBlockingQueue(),
    internal val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    val log: Logger = LoggerFactory.getLogger(name)

    val children: List<KActorRef<*>>
        get() = _children.toList()

    fun addChild(child: KActorRef<*>) {
        _children.add(child)
    }
}
