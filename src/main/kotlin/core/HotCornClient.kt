package latiif.hotcorn.app.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.github.opendevl.JFlat
import java.io.InputStream
import java.util.*
import latiif.hotcorn.app.core.EpisodeParser.asEpisode
import org.jsoup.Jsoup

class HotCornClient(val write: (Any?) -> Unit = ::println) {

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

    private fun getLatestEpisodes(rawShow: String, epoch: Long, epsilon: Int): Sequence<Episode> {
        if (rawShow == "null") return emptySequence()
        val show = JsonParser().parse(rawShow).asJsonObject

        val showTitle = show["title"].asString
        val episodes = show.getAsJsonArray("episodes")

        return episodes.asSequence().mapNotNull {
            val episode = it.toString().asEpisode()
            if (episode.isAiredAfter(epoch, epsilon)) {
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

    fun printEpisode(episode: Episode?, options: String = "A") {
        if (episode == null) {
            write("")
            return
        }

        val printAll = "A" in options
        val printEpisodeId = "e" in options || printAll
        val printCSV = "c" in options
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
    ): Sequence<Episode?> {
        return sequence {
            shows.distinct().forEach {
                val seriesPage = getSeriesViaId(it) nullIfEqualTo "null" ?: getSeriesViaKeyword(it)
                val episodes =
                    if (multipleEpisodes) {
                        getLatestEpisodes(seriesPage, lastCheck, epsilon)
                    } else {
                        sequenceOf(getLatestEpisode(seriesPage))
                    }
                episodes.filterNotNull().forEach { episode ->
                    val isNewEpisode = episode.isAiredAfter(lastCheck, epsilon) || getLatest
                    if (includeAll) {
                        yield(if (isNewEpisode) episode else null)
                    }
                    if (isNewEpisode && !includeAll) {
                        yield(episode)
                    }
                }
            }
        }
    }
}

private fun JsonObject.toCSV(delim: String = ", "): String {
    val rawJson = this.toString()
    val flattener = JFlat(rawJson)
    val items = flattener.json2Sheet().getJsonAsSheet()

    var comma: Boolean
    val sb = StringBuilder()
    items.forEach {
        comma = false
        it.forEach {
            if (it == null) {
                sb.append(if (comma) delim else "")
            } else {
                sb.append(
                        if (comma) "${delim}${it.toStringWithoutTrailingZeros()}"
                        else it.toStringWithoutTrailingZeros())
            }
            if (!comma) comma = true
        }
        sb.append("\n")
    }

    return sb.toString()
}


private fun Any?.toStringWithoutTrailingZeros(): String {
    val str = this.toString()
    val v = str.toDoubleOrNull()
    return when (v) {
        null -> str
        else -> "${v.toInt()}"
    }
}


private infix fun String.nullIfEqualTo(str: String) = if (this == str) null else this


private fun Episode.isAiredAfter(lastCheck: Long, epsilon: Int): Boolean {
    val epsilonFactor = 3600 // there are 3600 seconds in an hour; epsilon is given in hours
    return (firstAired + epsilon * epsilonFactor) > lastCheck
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
