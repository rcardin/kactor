package `in`.rcard.kactor

import kotlinx.coroutines.channels.SendChannel

class KActorRef<T> internal constructor(
    private val mailbox: SendChannel<T>
) {
    suspend fun tell(msg: T) {
        mailbox.send(msg)
    }

//    suspend fun <R: Any> ask(msg: T): CompletableDeferred<R> {
//        val deferred = CompletableDeferred<R>()
//
//        val behavior = receive<R> {
//            deferred.complete(it)
//            same()
//        }
//
//        coroutineScope {
//            kactor("kactor_${UUID.randomUUID()}", behavior)
//        }
//        return deferred
//    }

    companion object KActorRefOps {
        suspend infix fun <T> KActorRef<T>.`!`(msg: T) {
            tell(msg)
        }
    }
}
