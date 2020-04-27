package latiif.hotcorn.app.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStream
import java.util.*
import org.jsoup.Jsoup

class HotCornClient(val write: (Any?) -> Unit = ::println) {
    private val SERIES_INFO = "https://tv-v2.api-fetch.sh/show/"
    private val SERIES_SEARCH = "https://tv-v2.api-fetch.sh/shows/1?sort=rating&order=-1&genre=all&keywords="

    private val EPSILON_FACTOR = 3600 // hours to seconds

    private fun InputStream.getAll() = bufferedReader().use { it.readText() }

    private fun parametrize(str: String) = str.replace("_", "%20", true).replace(" ", "%20", true)

    private fun getSeriesViaId(id: String): String {
        return try {
            Jsoup.connect(SERIES_INFO + id).ignoreContentType(true).execute().body()
        } catch (statusException: Exception) {
            "null"
        }
    }

    private fun getSeriesViaKeyword(keyword: String): String {
        val json = Jsoup.connect(SERIES_SEARCH + parametrize(keyword)).ignoreContentType(true).execute().body()
        if (json.equals("null")) return "null"
        val jsonElement = JsonParser().parse(json)

        val jobject = jsonElement.asJsonArray
        if (jobject.size() == 0) return "null"
        return getSeriesViaId(jobject[0].asJsonObject.get("imdb_id").asString)
    }

    private fun getEpisodes(json: String): String {
        val jelement = JsonParser().parse(json)
        val jobject = jelement.asJsonObject

        val jarray = jobject.getAsJsonArray("episodes")
        return jarray.toString()
    }

    private fun getLatestEpisode(json: String): String {
        if (json.equals("null")) return "null"

        val jelement = JsonParser().parse(json)
        val jobject = jelement.getAsJsonObject()

        val jarray = jobject.getAsJsonArray("episodes")

        val latestEpisode = jarray.maxBy { it.asJsonObject.get("first_aired").asLong }
        latestEpisode?.asJsonObject?.addProperty("show_title", jobject["title"].asString)

        return latestEpisode.toString()
    }

    private fun getLatestEpisodes(json: String, epoch: Long): List<String> {
        if (json.equals("null")) return listOf()

        val jelement = JsonParser().parse(json)
        val jobject = jelement.getAsJsonObject()

        val jarray = jobject.getAsJsonArray("episodes")

        val latestEpisodes = mutableListOf<String>()
        jarray.forEach {
            val episodeEpoch: Long = it.asJsonObject.get("first_aired").asLong

            if (episodeEpoch > epoch) {
                it.asJsonObject.addProperty("show_title", jobject["title"].asString)
                latestEpisodes.add(it.toString())
            }
        }
        return latestEpisodes
    }

    private fun isNew(episode: String, lastCheck: Long, epsilon: Int): Boolean {
        episode.nullIfEqualTo("null") ?: return false

        val jelement = JsonParser().parse(episode)
        val jobject = jelement.asJsonObject
        val episodeEpoch = jobject["first_aired"].asLong

        return (episodeEpoch + epsilon * EPSILON_FACTOR) > lastCheck
    }

    public fun printEpisode(episode: String, options: String = "A") {
        if (episode == "null" || episode == "") {
            write(""); return
        }

        val printEpisodeId = "e" in options
        val printCSV = "c" in options
        val getTorrent = "t" in options
        val printAll = "A" in options
        val printTorrents = "D" in options
        val printFirstAiredEpoch = "P" in options
        val printFirstAired = "F" in options
        val printOverview = "O" in options
        val printTitle = "T" in options
        val printEpisode = "E" in options
        val printSeason = "s" in options
        val printShow = "S" in options

        val jelement = JsonParser().parse(episode)
        val jobject = jelement.asJsonObject
        val episodeEpoch = jobject["first_aired"].asLong

        val netDate = Date(episodeEpoch * 1000)

        val episodeInfo = JsonObject()

        if (getTorrent) episodeInfo.addProperty("torrent", retrieveBestTorrent(jobject["torrents"].asJsonObject))

        if (printAll) {
            jobject.addProperty("first_aired_utc", netDate.toString())

            write(jobject.toString())
            return
        }

        if (printTorrents) episodeInfo.add("torrents", jobject["torrents"])
        if (printFirstAiredEpoch) episodeInfo.add("first_aired", jobject["first_aired"])
        if (printFirstAired) episodeInfo.addProperty("first_aired_utc", netDate.toString())

        if (printShow) episodeInfo.add("show_title", jobject["show_title"])

        if (printOverview) episodeInfo.add("overview", jobject["overview"])
        if (printTitle) episodeInfo.add("title", jobject["title"])
        if (printEpisode) episodeInfo.add("episode", jobject["episode"])
        if (printSeason) episodeInfo.add("season", jobject["season"])
        if (printEpisodeId) episodeInfo.add("episodeID", jobject["tvdb_id"])

        if (printCSV)
            write(episodeInfo.toCSV())
        else
            write(episodeInfo.toString())
    }

    public fun checkForUpdates(lastCheck: Long, epsilon: Int, shows: List<String>, multipleEpisodes: Boolean = true, includeAll: Boolean = false, getLatest: Boolean = false): MutableList<String> {
        val result = mutableListOf<String>()

        shows.forEach {
            val seriesPage = getSeriesViaId(it) nullIfEqualTo "null" ?: getSeriesViaKeyword(it)

            val episodeStrings = if (multipleEpisodes) {
                getLatestEpisodes(seriesPage, lastCheck)
            } else {
                listOf(getLatestEpisode(seriesPage))
            }

            for (episodeString in episodeStrings) {
                val isNewEpisode = isNew(episodeString, lastCheck, epsilon) || getLatest
                if (includeAll) {
                    result.add(if (isNewEpisode) episodeString else "")
                }
                if (isNewEpisode && !includeAll) result.add(episodeString)
            }
        }
        return result
    }

    private fun JsonObject.toCSV(): String {
    return this.keySet()
        .map(this::get)
        .map { it.asJsonPrimitive }
        .joinToString(", ")
    }

    private fun retrieveBestTorrent(torrentsObject: JsonObject): String {
        val options = listOf("1080p", "720p", "480p", "0")

        for (resolution in options) {
            try {
                return torrentsObject[resolution].asJsonObject["url"].asString
            } catch (e: IllegalStateException) {
                continue
            }
        }
        return ""
    }

    private infix fun String.nullIfEqualTo(str: String) = if (this == str) null else this
}
