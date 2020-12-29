package latiif.hotcorn.app.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStream
import java.util.*
import latiif.hotcorn.app.core.EpisodeParser.asEpisode
import org.jsoup.Jsoup

class HotCornClient(val write: (Any?) -> Unit = ::println) {
    private val epsilonFactor = 3600 // in seconds

    private fun InputStream.getAll() = bufferedReader().use { it.readText() }

    private fun parametrize(str: String) = str.replace("_", "%20", true).replace(" ", "%20", true)

    private fun getSeriesViaId(id: String): String {
        return try {
            Jsoup.connect(Backend.getSeriesInfoUrl() + id).ignoreContentType(true).execute().body()
        } catch (statusException: Exception) {
            "null"
        }
    }

    private fun getSeriesViaKeyword(keyword: String): String {
        val rawSeriesJson =
                Jsoup.connect(Backend.getSeriesSearchUrl() + parametrize(keyword))
                        .ignoreContentType(true)
                        .execute()
                        .body()
        if (rawSeriesJson == "null") return "null"
        val series = JsonParser().parse(rawSeriesJson)

        val matches = series.asJsonArray
        if (matches.size() == 0) return "null"
        return getSeriesViaId(matches[0].asJsonObject.get("imdb_id").asString)
    }

    private fun getLatestEpisode(rawShow: String): Episode? {
        if (rawShow == "null") return null
        val show = JsonParser().parse(rawShow).asJsonObject

        val showTitle = show["title"].asString
        val episodes = show.getAsJsonArray("episodes")

        val latestEpisode = episodes.maxByOrNull { it.asJsonObject.get("first_aired").asLong }
        latestEpisode?.asJsonObject?.addProperty("show_title", showTitle)

        return latestEpisode.toString().asEpisode()
    }

    private fun getLatestEpisodes(rawShow: String, epoch: Long, epsilon: Int): List<Episode> {
        if (rawShow == "null") return listOf()
        val show = JsonParser().parse(rawShow).asJsonObject

        val showTitle = show["title"].asString
        val episodes = show.getAsJsonArray("episodes")

        return episodes.mapNotNull {
            val episode = it.toString().asEpisode()
            if (isNew(episode, epoch, epsilon)) {
                Episode(
                        firstAired = episode.firstAired,
                        overview = episode.overview,
                        title = episode.title,
                        episode = episode.episode,
                        season = episode.season,
                        tvdbId = episode.tvdbId,
                        showTitle = showTitle,
                        firstAiredUTC = episode.firstAiredUTC,
                        torrents = episode.torrents,
                )
            } else {
                null
            }
        }
    }

    private fun isNew(episode: Episode, lastCheck: Long, epsilon: Int): Boolean {
        return (episode.firstAired + epsilon * epsilonFactor) > lastCheck
    }

    fun printEpisode(episode: Episode?, options: String = "A") {
        if (episode == null) {
            write("")
            return
        }

        val printAll = "A" in options
        val printEpisodeId = "e" in options || printAll
        val printCSV = "c" in options || printAll
        val getTorrent = "t" in options || printAll
        val printTorrents = "D" in options || printAll
        val printFirstAiredEpoch = "P" in options || printAll
        val printFirstAired = "F" in options || printAll
        val printOverview = "O" in options || printAll
        val printTitle = "T" in options || printAll
        val printEpisode = "E" in options || printAll
        val printSeason = "s" in options || printAll
        val printShow = "S" in options || printAll

        val episodeEpoch = episode.firstAired

        val netDate = Date(episodeEpoch * 1000)

        val episodeInfo = JsonObject()

        if (getTorrent) episodeInfo.addProperty("torrent", retrieveBestTorrent(episode.torrents))

        if (printTorrents) {
            val torrents = JsonObject()
            episode.torrents.forEach { torrents.addProperty(it.key, it.value) }
            episodeInfo.add("torrents", torrents)
        }
        if (printFirstAiredEpoch) episodeInfo.addProperty("first_aired", episode.firstAired)
        if (printFirstAired) episodeInfo.addProperty("first_aired_utc", netDate.toString())

        if (printShow) episodeInfo.addProperty("show_title", episode.showTitle)

        if (printOverview) episodeInfo.addProperty("overview", episode.overview)
        if (printTitle) episodeInfo.addProperty("title", episode.title)
        if (printEpisode) episodeInfo.addProperty("episode", episode.episode)
        if (printSeason) episodeInfo.addProperty("season", episode.season)
        if (printEpisodeId) episodeInfo.addProperty("episodeID", episode.tvdbId)

        if (printCSV) write(episodeInfo.toCSV()) else write(episodeInfo.toString())
    }

    fun checkForUpdates(
            lastCheck: Long,
            epsilon: Int,
            shows: List<String>,
            multipleEpisodes: Boolean = true,
            includeAll: Boolean = false,
            getLatest: Boolean = false
    ): List<Episode?> {
        val result = mutableListOf<Episode?>()
        shows.forEach {
            val seriesPage = getSeriesViaId(it) nullIfEqualTo "null" ?: getSeriesViaKeyword(it)
            val episodes =
                    if (multipleEpisodes) {
                        getLatestEpisodes(seriesPage, lastCheck, epsilon)
                    } else {
                        listOf(getLatestEpisode(seriesPage))
                    }
            episodes.filterNotNull().forEach { episode ->
                val isNewEpisode = isNew(episode, lastCheck, epsilon) || getLatest
                if (includeAll) {
                    result.add(if (isNewEpisode) episode else null)
                }
                if (isNewEpisode && !includeAll) result.add(episode)
            }
        }
        return result
    }

    private fun JsonObject.toCSV(): String {
        return this.keySet().map(this::get).map { it.asJsonPrimitive }.joinToString(", ")
    }

    private infix fun String.nullIfEqualTo(str: String) = if (this == str) null else this
}

fun retrieveBestTorrent(torrentsObject: Map<String, String>): String? {
    val options = listOf("1080p", "720p", "480p", "0")
    return options.fold(
            null as String?,
            { torrent, resolution ->
                torrent
                        ?: if (!torrentsObject[resolution].isNullOrEmpty()) {
                            torrentsObject[resolution]
                        } else {
                            null
                        }
            })
}
