package `in`.rcard.kactor

import `in`.rcard.kactor.examples.ExceptionHandling.exceptionHandling
import kotlinx.coroutines.coroutineScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main(): Unit = coroutineScope {
//    spawningActor()
//    requestResponsePattern()
//    askPattern()
//    receiveMessagePattern()
    exceptionHandling()
}