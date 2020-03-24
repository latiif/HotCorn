import llusx.hotcorn.app.Main
import llusx.hotcorn.app.core.write
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.StringBuilder

typealias Assessment = Pair<Boolean, String>

fun Assessment.succeeded() = this.first
fun Assessment.response() = this.second

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHotCorn {

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

    data class TestCase(val flags: String, val afterEpoch: String, val shows: List<String>, val result: List<String>)


    fun TestCase.run(): Assessment {
        val args = mutableListOf<String>()

        val flags = this.flags.fold("", { acc, c -> "$acc$c" })
        args.add(flags)
        args.add(this.afterEpoch)

        val inputSender = InputSender(shows)
        val bucket = OutputBucket()
        write = bucket::capture
        Main.executeCommand(args.toTypedArray(), inputSender::readLine)

        bucket.flush().also { output ->
            return Assessment(this.result.all { episode ->
                episode in output
            }, output)
        }
    }

    @Test
    fun `Get All Episodes of Vikings After January 1st 2020`() {
        TestCase(
            flags = "sTEM",
            shows = listOf("Vikings"),
            afterEpoch = "1577842429",
            result = listOf(
                "{\"title\":\"The Key\",\"episode\":5,\"season\":6}",
                "{\"title\":\"The Best Laid Plans\",\"episode\":10,\"season\":6}"
            )
        ).run().apply { assert(succeeded()) }
    }

    @Test
    fun `Get Information on Multiple Series`() {
        TestCase(
            flags = "S",
            shows = listOf("vikings", "rick and morty"),
            afterEpoch = "0",
            result = listOf(
                "Vikings",
                "Rick and Morty"
            )
        ).run().apply { assert(succeeded()) }
    }

    @Test
    fun `Invalid ID Returns Empty Response`() {
        TestCase(
            flags = "S",
            shows = listOf("wqeqweqweqwrq"), // random gibberish keyword
            afterEpoch = "0",
            result = listOf()
        ).run().apply {
            assert(succeeded() and response().isEmpty())
        }
    }

    @Test
    fun `Empty Show Keyword Returns Empty Response`() {
        TestCase(
            flags = "S",
            shows = listOf("", " ", "" + "\t"), // Empty strings
            afterEpoch = "0",
            result = listOf()
        ).run().apply {
            assert(succeeded() and response().isEmpty())
        }
    }
}
