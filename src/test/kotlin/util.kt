package llusx.hotcorn.test


import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.lang.StringBuilder
import llusx.hotcorn.app.Main

typealias Assessment = Pair<Boolean, String>

fun Assessment.succeeded() = this.first
fun Assessment.response() = this.second
data class TestCase(val flags: String, val afterEpoch: String, val shows: List<String>, val result: List<String>)

class InputSender(val items: List<String>) {
    private var i = 0
    fun readLine() = items.drop(i++).firstOrNull()
}

class OutputBucket() {
    private val sb = StringBuilder()
    fun capture(messages: Any?) {
        sb.append(messages)
    }

    fun flush(): String {
        return sb.toString()
    }
}

fun String.toEpoch(): String {
        val date = SimpleDateFormat("dd-MM-yyyy").parse(this)
        return "${date.time / 1000}" // get epoch in seconds
}

fun TestCase.run(): Assessment {
    val args = mutableListOf<String>()

    val flags = this.flags.fold("", { acc, c -> "$acc$c" })
    args.add(flags)
    args.add(this.afterEpoch)

    val inputSender = InputSender(shows)
    val bucket = OutputBucket()

    Main.executeCommand(args.toTypedArray(), inputSender::readLine, bucket::capture)

    bucket.flush().also { output ->
        return Assessment(this.result.all { episode ->
            episode in output
        }, output)
    }
}
