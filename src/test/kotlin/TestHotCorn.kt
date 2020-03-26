import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHotCorn {

    @Test
    fun `Get All Episodes of Vikings After January 1st 2020`() {
        TestCase(
            flags = "sTEM",
            shows = listOf("Vikings"),
            afterEpoch = "01-01-2020".toEpoch(),
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
            afterEpoch = "15-10-1994".toEpoch(),
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
            afterEpoch = "21-02-1994".toEpoch(),
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
            afterEpoch = "30-10-2019".toEpoch(),
            result = listOf()
        ).run().apply {
            assert(succeeded() and response().isEmpty())
        }
    }

    @Test
    fun `Check CSV Format`() {
        TestCase(
            flags = "MSecs",
            shows = listOf("Vikings"),
            afterEpoch = "0",
            result = listOf(
                "\"Vikings\", 6, 7395843",
                "\"Vikings\", 3, 5138072"
            )
        ).run().apply {
            assert(succeeded())
        }
    }
}
