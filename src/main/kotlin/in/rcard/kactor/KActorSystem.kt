package `in`.rcard.kactor

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope

class KActorSystem<T>(
    private val mainActorBehavior: KBehavior<T>,
    mainActorName: String
) : KActorRef<T> {

    private val mainActor: KActor<T>
    private val mainActorMailbox: SendChannel<T>

    init {
        println("Creating main actor '$mainActorName'")
        val context = KActorContext<T>()
        mainActorMailbox = Channel<T>(UNLIMITED)
        mainActor = KActor(mainActorName, context, mainActorMailbox)
        println("Main actor '$mainActorName' created")
    }

    override suspend fun tell(msg: T) {
        mainActorMailbox.send(msg)
        println("Main actor '${mainActor.name}' received message $msg")
        coroutineScope {
            async {
                mainActor.run(mainActorBehavior)
            }
        }
        println("Main actor '${mainActor.name}' processed message $msg")
    }
}
