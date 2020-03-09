package llusx.hotcorn.app

import llusx.hotcorn.app.core.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val options = if (args.size > 1)
            args[0]
        else {
            println("ERROR: Arguments expected.")
            return
        }

        val includeAll = args[0].contains("i")
        val getLatest = args[0].contains("l")
        val getMultipleEpisodes = args[0].contains("M")

        if (args.size < 2) {
            println("ERROR: A last check timestamp is required")
            return
        }

        val lastCheck: Long
        try {
            lastCheck = java.lang.Long.valueOf(args[1])
            if (lastCheck < 0) throw IllegalArgumentException()
        } catch (e: Exception) {
            println("ERROR: Last check timestamp is invalid: " + args[0])
            return
        }

        val epsilonIndex = args.lastIndexOf("-epsilon");
        val epsilon = if (epsilonIndex != -1) {
            args[epsilonIndex + 1].toInt()
        } else {
            0
        }

        val shows = generateSequence(::readLine).toList()
        val result = checkForUpdates(lastCheck, epsilon, shows, getMultipleEpisodes, includeAll, getLatest)

        result.forEach { printEpisode(it, options) }
    }
}
