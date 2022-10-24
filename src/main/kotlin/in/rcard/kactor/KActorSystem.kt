package `in`.rcard.kactor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class KActorSystem<T>(
    private val mainActorBehavior: KBehavior<T>,
    mainActorName: String
) : KActorRef<T> {

    val mainActor: KActor<T>

    init {
        val context = KActorContext<T>()
        val mailbox = Channel<T>()
        mainActor = KActor(mainActorName, context, mailbox)
    }

    override suspend fun tell(msg: T) {
        coroutineScope {
            launch {
                mainActor.run(mainActorBehavior)
            }
        }
    }
}
