package `in`.rcard.kactor

import `in`.rcard.kactor.examples.ExceptionHandling.exceptionHandling
import kotlinx.coroutines.supervisorScope

// FIXME Just for testing purposes. Delete it in the near future
suspend fun main() = supervisorScope {
//    spawningActor()
//    requestResponsePattern()
//    askPattern()
//    receiveMessagePattern()
    exceptionHandling()
}
