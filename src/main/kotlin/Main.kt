package llusx.hotcorn.app

import llusx.hotcorn.app.core.*


object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val options = if (args.size>1)
             args[0]
        else {
            println("ERROR: Arguments expected")
            return
        }


        val includeAll  = args[0].contains("i")
        val getLatest   = args[0].contains("l")

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

        val shows = Array(args.size-2){ args[it+2] }


        val result = checkForUpdates(lastCheck, shows, includeAll,getLatest)

        result.forEach {printEpisode(it,options)}

    }
}
