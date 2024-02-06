package `in`.rcard.kactor.examples

import `in`.rcard.kactor.KActorRef.KActorRefOps.`!`
import `in`.rcard.kactor.KBehavior
import `in`.rcard.kactor.blocking
import `in`.rcard.kactor.kactorSystem
import `in`.rcard.kactor.receive
import `in`.rcard.kactor.same
import `in`.rcard.kactor.spawn
import kotlinx.coroutines.coroutineScope

object BlockingKActorExample {
    suspend fun blockingKActorExample() =
        coroutineScope {
            val mainKActor = kactorSystem(MainActor.behavior())
            mainKActor `!` MainActor.Start
        }

    object MainActor {
        object Start

        fun behavior(): KBehavior<Start> =
            receive { ctx, _ ->
                val fileReader = ctx.spawn("fileOpener", blocking(FileReader.behavior))
                fileReader `!` FileReader.ReadFile("/file.txt")
                same()
            }
    }

    object FileReader {
        data class ReadFile(val path: String)

        val behavior: KBehavior<ReadFile> =
            receive { ctx, msg ->
                val fileContent = FileReader::class.java.getResource(msg.path)?.readText()
                ctx.log.info("File content: $fileContent")
                same()
            }
    }
}
