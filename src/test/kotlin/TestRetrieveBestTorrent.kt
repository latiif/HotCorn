package latiif.hotcorn.test

import latiif.hotcorn.app.core.retrieveBestTorrent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRetrieveBestTorrent {

    @Test
    fun `Get 1080p Torrent`() {
        TorrentTest(
            torrents = mapOf(
                "720p" to "720p Torrent",
                "1080p" to "1080p Torrent",
                "320p" to "320p Torrent"
            ),
            expected = "1080p Torrent"
        ).run().apply { assert(succeeded()) }
    }

    @Test
    fun `Get 720p Torrent`() {
        TorrentTest(
            torrents = mapOf(
                "720" to "Invalid key",
                "320p" to "320p Torrent",
                "720p" to "720p Torrent"
                ),
            expected = "720p Torrent"
        ).run().apply {
            assert(succeeded())
        }
    }

    @Test
    fun `No Torrent Found`() {
        TorrentTest(
            torrents = mapOf(
                "720" to "Invalid key",
                "720" to "Invalid key",
                "320" to "Invalid key"
            ),
            expected = null
        ).run().apply { assert(succeeded()) }
    }
}

data class TorrentTest(val torrents: Map<String, String>, val expected: String?)

fun TorrentTest.run(): Assessment {
    val got = retrieveBestTorrent(this.torrents)
    return Assessment(
        got == this.expected,
        got ?: "Nothing"
    )
}
