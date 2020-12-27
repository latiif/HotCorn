package latiif.hotcorn.app.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class EpisodeDeserializer : JsonDeserializer<Episode> {
    @ExperimentalStdlibApi
    override fun deserialize(jsonElement: JsonElement?, typeOfT: Type?, jdCtx: JsonDeserializationContext?): Episode {
        requireNotNull(jsonElement)
        requireNotNull(typeOfT)
        requireNotNull(jdCtx)

        val json = jsonElement.asJsonObject

        val torrents = buildMap<String, String> {
            ((json get "torrents")?.asJsonObject ?: JsonObject()).entrySet().forEach {
                 try {
                    this[it.key] = it.value.asJsonObject["url"].asString
                }catch (e: Exception){
                    return@forEach
                }
            }
        }
        return Episode(
            firstAired = (json get "first_aired")?.asLong ?: 0,
            overview = (json get "overview")?.asString ?: "",
            title = (json get "title")?.asString ?: "",
            episode = (json get "episode")?.asInt ?: 0,
            season = (json get "season")?.asInt ?: 0,
            tvdbId = (json get "tvdb_id")?.asLong ?: 0,
            showTitle = (json get "show_title")?.asString ?: "",
            firstAiredUTC = (json get "first_aired_utc")?.asString ?: "",
            torrents = torrents
        )
    }

    infix fun JsonObject.get(memberName: String): JsonElement? = try {
        this[memberName]
    } catch (e: Exception) {
        null
    }
}
