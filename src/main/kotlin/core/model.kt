package llusx.hotcorn.app.core

import com.google.gson.JsonObject
import org.jsoup.Jsoup
import java.io.InputStream
import com.google.gson.JsonParser
import java.util.*
import kotlin.math.E


const val SERIES_INFO = "https://tv-v2.api-fetch.website/show/"
const val SERIES_SEARCH = "https://tv-v2.api-fetch.website/shows/1?sort=rating&order=-1&genre=all&keywords="

const val EPSILON_FACTOR = 3600 //hours to seconds


fun InputStream.getAll() = bufferedReader().use { it.readText() }

fun parametrize(str: String) = str.replace("_", "%20", true)


fun getSeriesViaId(id: String) = Jsoup.connect(SERIES_INFO + id).ignoreContentType(true).execute().body()

fun getSeriesViaKeyword(keyword: String): String {
    val json = Jsoup.connect(SERIES_SEARCH + parametrize(keyword)).ignoreContentType(true).execute().body()
    if (json.equals("null")) return "null"
    val jsonElement = JsonParser().parse(json)

    var jobject = jsonElement.asJsonArray
    if (jobject.size() == 0) return "null"
    return getSeriesViaId(jobject[0].asJsonObject.get("imdb_id").asString)
}


fun getEpisodes(json: String): String {
    val jelement = JsonParser().parse(json)
    var jobject = jelement.asJsonObject

    val jarray = jobject.getAsJsonArray("episodes")
    return jarray.toString()
}


fun getLatestEpisode(json: String): String {
    if (json.equals("null")) return "null"

    val jelement = JsonParser().parse(json)
    var jobject = jelement.getAsJsonObject()

    val jarray = jobject.getAsJsonArray("episodes")

    var maxEpoch: Long = 0
    var latestEpisode: String = ""
    jarray.forEach {
        val episodeEpoch: Long = it.asJsonObject.get("first_aired").asLong

        if (episodeEpoch > maxEpoch) {
            maxEpoch = episodeEpoch
            latestEpisode = it.toString()
        }
    }

    return latestEpisode
}


fun isNew(episode: String, lastCheck: Long, epsilon: Int): Boolean {

    if (episode.equals("null")) return false


    val jelement = JsonParser().parse(episode)
    val jobject = jelement.getAsJsonObject()

    val episodeEpoch = jobject.get("first_aired").asLong

    return (episodeEpoch + epsilon * EPSILON_FACTOR) > lastCheck
}


fun printEpisode(episode: String, options: String = "A") {
    if (episode.equals("null") || episode.equals("")) {
        println(""); return
    }

    val getTorrent = options.contains("t")
    val printAll = options.contains("A")
    val printTorrents = options.contains("D")
    val printFirstAiredEpoch = options.contains("P")
    val printFirstAired = options.contains("F")
    val printOverview = options.contains("O")
    val printTitle = options.contains("T")
    val printEpisode = options.contains("E")
    val printSeason = options.contains("S")


    val jelement = JsonParser().parse(episode)
    val jobject = jelement.getAsJsonObject()
    val episodeEpoch = jobject.get("first_aired").asLong

    val netDate = Date(episodeEpoch * 1000)

    if (getTorrent) {
        println(jobject.get("torrents").asJsonObject.get("0").asJsonObject.get("url").asString)
        return
    }

    if (printAll) {
        jobject.addProperty("first_aired_utc", netDate.toString());

        println(jobject.toString())
        return
    }

    val newObject = JsonObject()

    if (printTorrents) newObject.add("torrents", jobject.get("torrents"))
    if (printFirstAiredEpoch) newObject.add("first_aired", jobject.get("first_aired"))
    if (printFirstAired) newObject.addProperty("first_aired_utc", netDate.toString())

    if (printOverview) newObject.add("overview", jobject.get("overview"))
    if (printTitle) newObject.add("title", jobject.get("title"))
    if (printEpisode) newObject.add("episode", jobject.get("episode"))
    if (printSeason) newObject.add("season", jobject.get("season"))


    println(newObject.toString())
}

fun checkForUpdates(lastCheck: Long, epsilon: Int, shows: Array<String>, includeAll: Boolean = false, getLatest: Boolean = false): MutableList<String> {
    val result = mutableListOf<String>()

    shows.forEach {
        var seriesPage = getSeriesViaId(it)
        if (seriesPage.equals("null")) seriesPage = getSeriesViaKeyword(it)

        val episodeString = getLatestEpisode(seriesPage)

        val isNewEpisode = isNew(episodeString, lastCheck, epsilon) || getLatest

        if (includeAll) {
            result.add(if (isNewEpisode) episodeString else "")
            return@forEach
        }

        if (isNewEpisode && !includeAll) result.add(episodeString)
    }

    return result
}