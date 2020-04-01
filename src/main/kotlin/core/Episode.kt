package llusx.hotcorn.app.core

import com.google.gson.annotations.SerializedName

data class Episode(
    @SerializedName("first_aired")
    val firstAired: Long,
    val overview: String,
    val title: String,
    val episode: Int,
    val season: Int,
    @SerializedName("tvdb_id")
    val tvdbId: Long,
    @SerializedName("show_title")
    val showTitle: String,
    @SerializedName("first_aired_utc")
    val firstAiredUTC: String
)
