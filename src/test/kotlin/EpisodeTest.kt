package latiif.hotcorn.test

import latiif.hotcorn.app.core.EpisodeParser.asEpisode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEpisode {
    @Test
    fun `Parse String to Episode with Defaults for Missing Fields`() {
        val raw = """{
        "first_aired": 1530869075,
        "overview": "The Pilot Episode",
        "title": "New Classes",
        "episode": 1,
        "tvdb_id": 7788999,
        "show_title": "The HotCorn Show",
        "first_aired_utc": "Fri, 06 Jul 2018 09:24:35 +0000"
        }
""".trimIndent()

        val episode = raw.asEpisode()
        with(episode) {
            assert(this.firstAired == 1530869075L)
            assert(this.overview == "The Pilot Episode")
            assert(this.title == "New Classes")
            assert(this.episode == 1)
            assert(this.season == 0)
            assert(this.tvdbId == 7788999L)
            assert(this.showTitle == "The HotCorn Show")
            assert(this.firstAiredUTC == "Fri, 06 Jul 2018 09:24:35 +0000")
            assert(this.torrents.isEmpty())
        }
    }

}
