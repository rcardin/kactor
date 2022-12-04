package `in`.rcard.kactor.examples

import `in`.rcard.kactor.examples.SpawningActor.spawningActor
import kotlinx.coroutines.coroutineScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main(): Unit = coroutineScope {
    spawningActor()
//    requestResponsePattern()
//    askPattern()
//    receiveMessagePattern()
//    exceptionHandling()
//    routerPattern()
//    counterExample()
//    blockingKActorExample()
}
