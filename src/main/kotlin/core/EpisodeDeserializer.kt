package latiif.hotcorn.app.core

import com.google.gson.*
import llusx.hotcorn.app.core.Episode
import java.lang.reflect.Type

class EpisodeDeserializer : JsonDeserializer<Episode> {
    @ExperimentalStdlibApi
    override fun deserialize(jsonElement: JsonElement?, typeOfT: Type?, jdCtx: JsonDeserializationContext?): Episode {
        requireNotNull(jsonElement)
        requireNotNull(typeOfT)
        requireNotNull(jdCtx)

        val json = jsonElement.asJsonObject

        val torrents = buildMap<String, String> {
            json["torrents"].asJsonObject.entrySet().forEach {
                this[it.key] = it.value.asJsonObject["url"].asString
            }
        }
        return Episode(
            firstAired = json["first_aired"].asLong otherwise 0,
            overview = json["overview"].asString otherwise "",
            title = json["title"].asString otherwise "",
            episode = json["episode"].asInt otherwise 0,
            season = json["season"].asInt otherwise 0,
            tvdbId = json["tvdb_id"].asLong otherwise 0,
            showTitle = json["show_title"].asString otherwise "",
            firstAiredUTC = json["first_aired_utc"].asString otherwise "",
            torrents = torrents otherwise mapOf()
        )
    }

    infix fun <T> T?.otherwise(default: T): T = this ?: default
}