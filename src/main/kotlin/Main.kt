package latiif.hotcorn.app

import java.text.SimpleDateFormat
import latiif.hotcorn.app.core.*

fun String.toEpoch(): Long {
        val date = SimpleDateFormat("dd-MM-yyyy").parse(this)
        return date.time / 1000 // get epoch in seconds
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        executeCommand(args, ::readLine, ::println)
    }

    fun executeCommand(args: Array<String>, inputReader: () -> String?, outputWriter: (Any?) -> Unit) {

        val options = if (args.size > 1)
            args[0]
        else {
            println("ERROR: Arguments expected.")
            return
        }

        val includeAll = "i" in args[0]
        val getLatest = "l" in args[0]
        val getMultipleEpisodes = "M" in args[0]

        if (args.size < 2) {
            println("ERROR: A last check timestamp is required")
            return
        }

        val lastCheck: Long = when {
            args[1] matches Regex("[0-9]+") -> args[1].toLong()
            args[1].isNotEmpty() -> args[1].toEpoch()
            else -> {
                println("ERROR: Last check timestamp is invalid: " + args[1])
                return
            }
        }
        if (lastCheck < 0) throw IllegalArgumentException()

        val epsilonIndex = args.lastIndexOf("-epsilon")
        val epsilon = if (epsilonIndex != -1) {
            args[epsilonIndex + 1].toInt()
        } else {
            0
        }

        val shows = generateSequence(inputReader).filter(String::isNotBlank).toList()
        val hotcorn = HotCornClient(outputWriter)
        val result = hotcorn.checkForUpdates(lastCheck, epsilon, shows, getMultipleEpisodes, includeAll, getLatest)

        result.forEach { hotcorn.printEpisode(it, options) }
    }
}
