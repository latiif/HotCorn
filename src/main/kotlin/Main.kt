package llusx.hotcorn.app

import llusx.hotcorn.app.core.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val options = if (args.size > 1)
            args[0]
        else {
            println("ERROR: Arguments expected")
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

        var shift = 2
        var epsilon = 0
        val epsilonIndex = args.lastIndexOf("-epsilon");
        if (epsilonIndex != -1) {
            epsilon = args[epsilonIndex+1].toInt()
            shift = 4
        }

        val shows = Array(args.size - shift) { args[it + shift] }


        val result = checkForUpdates(lastCheck,epsilon, shows, getMultipleEpisodes, includeAll, getLatest)

        result.forEach { printEpisode(it, options) }
    }
}
