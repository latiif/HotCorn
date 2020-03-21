import llusx.hotcorn.app.Main
import llusx.hotcorn.app.core.write
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.StringBuilder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHotCorn {
    class InputSender(val items: List<String>) {
        var i = 0
        fun readLine(): String? {
            if (i >= items.size) return null
            return items[i++]
        }
    }

    class OutputBucket() {
        val sb = StringBuilder()
        fun capture(messages: Any?) {
            sb.append(messages)
        }

        fun flush(): String {
            return sb.toString()
        }
    }

    data class TestCase(val flags: String, val afterEpoch: String, val shows: List<String>, val result: List<String>)

    fun TestCase.run(): Boolean {
        val args = mutableListOf<String>()

        val flags = this.flags.fold("", { acc, c -> "$acc$c" })
        args.add(flags)
        args.add(this.afterEpoch)

        val inputSender = InputSender(shows)
        val bucket = OutputBucket()
        write = bucket::capture
        Main.executeCommand(args.toTypedArray(), inputSender::readLine)

        bucket.flush().also { output ->
            return this.result.all { episode ->
                episode in output
            }
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
        ).run().also(::assert)
    }

    @Test
    fun `Get Information on Multiple Series`(){
        TestCase(
            flags = "S",
            shows = listOf("vikings","rick and morty"),
            afterEpoch = "0",
            result = listOf(
                "Vikings",
                "Rick and Morty"
            )
        ).run().also(::assert)
    }
}
